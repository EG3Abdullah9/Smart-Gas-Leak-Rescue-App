package com.example.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

data class PairedBtDevice(
    val name: String,
    val address: String
)

class BluetoothDeviceMonitor(private val context: Context) {

    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private var readJob: Job? = null
    private var activeSocket: BluetoothSocket? = null

    fun hasBluetoothConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<PairedBtDevice> {
        if (!hasBluetoothConnectPermission()) return emptyList()
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter: BluetoothAdapter? = manager?.adapter
        if (adapter == null || !adapter.isEnabled) return emptyList()
        return adapter.bondedDevices.orEmpty().map { device: BluetoothDevice ->
            PairedBtDevice(
                name = device.name ?: "Bluetooth Device",
                address = device.address
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(
        address: String,
        scope: CoroutineScope,
        onConnected: (String) -> Unit,
        onPpmReceived: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        stop()
        if (!hasBluetoothConnectPermission()) {
            onError("Bluetooth permission not granted.")
            return
        }
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = manager?.adapter
        if (adapter == null || !adapter.isEnabled) {
            onError("Bluetooth adapter is off or unavailable.")
            return
        }

        readJob = scope.launch(Dispatchers.IO) {
            try {
                val device = adapter.getRemoteDevice(address)
                val label = device.name ?: address
                val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                activeSocket = socket
                adapter.cancelDiscovery()
                socket.connect()
                onConnected(label)

                val reader = BufferedReader(InputStreamReader(socket.inputStream))
                val regex = Regex("\\d+")
                while (isActive) {
                    val line = reader.readLine() ?: break
                    val ppm = regex.find(line)?.value?.toIntOrNull()
                    if (ppm != null && ppm in 0..1500) {
                        onPpmReceived(ppm)
                    }
                }
            } catch (e: Exception) {
                onError("Bluetooth SPP error: ${e.localizedMessage ?: "Connection failed"}")
            } finally {
                stop()
            }
        }
    }

    fun stop() {
        readJob?.cancel()
        readJob = null
        runCatching { activeSocket?.close() }
        activeSocket = null
    }
}
