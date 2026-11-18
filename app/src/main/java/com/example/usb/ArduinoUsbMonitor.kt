package com.example.usb

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ArduinoUsbMonitor(private val context: Context) {

    companion object {
        const val ACTION_USB_PERMISSION = "com.aistudio.gasrescue.USB_PERMISSION"
        // Arduino Uno, Mega, CH340, FTDI, CP210x vendor IDs
        private val ARDUINO_VENDOR_IDS = setOf(0x2341, 0x2A03, 0x1A86, 0x0403, 0x10C4)
    }

    private var readJob: Job? = null

    sealed class UsbScanResult {
        data class Connected(val deviceName: String) : UsbScanResult()
        data class PermissionRequested(val deviceName: String) : UsbScanResult()
        data object NoDeviceFound : UsbScanResult()
        data object NotSupported : UsbScanResult()
    }

    fun scanAndConnect(
        scope: CoroutineScope,
        onPpmReceived: (Int) -> Unit,
        onError: (String) -> Unit
    ): UsbScanResult {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
            ?: return UsbScanResult.NotSupported

        val devices = usbManager.deviceList.values.toList()
        if (devices.isEmpty()) return UsbScanResult.NoDeviceFound

        val target = devices.firstOrNull { it.vendorId in ARDUINO_VENDOR_IDS } ?: devices.first()
        val label = target.productName ?: target.deviceName

        if (!usbManager.hasPermission(target)) {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE
            } else {
                0
            }
            val intent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(ACTION_USB_PERMISSION).setPackage(context.packageName),
                flags
            )
            usbManager.requestPermission(target, intent)
            return UsbScanResult.PermissionRequested(label)
        }

        startReading(usbManager, target, scope, onPpmReceived, onError)
        return UsbScanResult.Connected(label)
    }

    private fun startReading(
        usbManager: UsbManager,
        device: UsbDevice,
        scope: CoroutineScope,
        onPpmReceived: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        readJob?.cancel()
        readJob = scope.launch(Dispatchers.IO) {
            val connection = usbManager.openDevice(device)
            if (connection == null) {
                onError("Unable to open USB device connection.")
                return@launch
            }
            try {
                var targetInterface = (0 until device.interfaceCount)
                    .map { device.getInterface(it) }
                    .firstOrNull { intf ->
                        (0 until intf.endpointCount).any { idx ->
                            val ep = intf.getEndpoint(idx)
                            ep.direction == UsbConstants.USB_DIR_IN && ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK
                        }
                    }

                if (targetInterface == null && device.interfaceCount > 0) {
                    targetInterface = device.getInterface(0)
                }

                if (targetInterface == null || !connection.claimInterface(targetInterface, true)) {
                    onError("Unable to claim USB serial interface.")
                    return@launch
                }

                val inEndpoint = (0 until targetInterface.endpointCount)
                    .map { targetInterface.getEndpoint(it) }
                    .firstOrNull { it.direction == UsbConstants.USB_DIR_IN }

                if (inEndpoint == null) {
                    onError("No USB IN endpoint found on device.")
                    return@launch
                }

                val buffer = ByteArray(128)
                val lineBuffer = StringBuilder()
                val regex = Regex("\\d+")

                while (isActive) {
                    val bytesRead = connection.bulkTransfer(inEndpoint, buffer, buffer.size, 500)
                    if (bytesRead > 0) {
                        val chunk = String(buffer, 0, bytesRead, Charsets.UTF_8)
                        lineBuffer.append(chunk)
                        while (true) {
                            val newlineIdx = lineBuffer.indexOf("\n")
                            if (newlineIdx == -1) break
                            val line = lineBuffer.substring(0, newlineIdx)
                            lineBuffer.delete(0, newlineIdx + 1)
                            val match = regex.find(line)
                            val ppm = match?.value?.toIntOrNull()
                            if (ppm != null && ppm in 0..1500) {
                                onPpmReceived(ppm)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                onError("USB serial read stopped: ${e.localizedMessage ?: "Disconnected"}")
            } finally {
                runCatching { connection.close() }
            }
        }
    }

    fun stop() {
        readJob?.cancel()
        readJob = null
    }
}
