package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.device.PairedBtDevice
import com.example.i18n.LocalizedStrings
import com.example.ui.DeviceConnectionTransport
import com.example.ui.GasRescueUiState
import com.example.ui.LogTone
import com.example.ui.components.FuturisticGlassCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.WarningAmber

@Composable
fun DeviceConnectScreen(
    uiState: GasRescueUiState,
    strings: LocalizedStrings,
    onConnectUsb: () -> Unit,
    onConnectWifi: (String) -> Unit,
    onScanBluetooth: () -> Unit,
    onConnectBluetooth: (PairedBtDevice) -> Unit,
    onDisconnect: () -> Unit,
    onOpenAppSimulator: () -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = uiState.settings.isDarkMode
    var wifiUrlInput by remember(uiState.settings.wifiDeviceUrl) {
        mutableStateOf(uiState.settings.wifiDeviceUrl)
    }
    val logScrollState = rememberScrollState()

    LaunchedEffect(uiState.terminalLogs.size) {
        if (logScrollState.maxValue > 0) {
            logScrollState.animateScrollTo(logScrollState.maxValue)
        }
    }

    val isConnected = uiState.isDeviceConnected
    val connColor = if (isConnected) SafeEmerald else WarningAmber

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hardware Connection Status Hero Card
        FuturisticGlassCard(
            isDarkMode = isDark,
            borderColor = connColor.copy(alpha = 0.45f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(connColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cable,
                            contentDescription = null,
                            tint = connColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.connectScreenTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = strings.connectScreenSub,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = connColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, connColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isConnected) strings.connStatusConnected else strings.connStatusDisconnected,
                            color = connColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isConnected) {
                                "${uiState.connectedTransport.name}: ${uiState.connectedDeviceLabel.orEmpty()} (${uiState.gasPPM} PPM)"
                            } else {
                                strings.connWaitingHardware
                            },
                            fontFamily = if (isConnected) JetBrainsMonoFontFamily else null,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isConnected) {
                        OutlinedButton(
                            onClick = onDisconnect,
                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("disconnect_hardware_button")
                        ) {
                            Icon(Icons.Filled.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.connDisconnectBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onOpenAppSimulator,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("open_app_simulator_cta"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.55f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AccentTeal.copy(alpha = 0.12f),
                    contentColor = if (isDark) AccentTeal else Color(0xFF007F70)
                )
            ) {
                Icon(Icons.Filled.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.connOpenSimulatorCta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // 2. USB OTG Serial Connection Card
        FuturisticGlassCard(isDarkMode = isDark) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Usb, contentDescription = null, tint = WarningAmber)
                    Text(
                        text = strings.usbCardTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = WarningAmber.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "9600 baud",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = WarningAmber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = strings.usbCardDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onConnectUsb,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("connect_usb_device_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentTeal,
                    contentColor = Color(0xFF040914)
                )
            ) {
                Icon(Icons.Filled.Usb, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.usbScanBtn, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }

        // 3. Wi-Fi Local HTTP Device Card (ESP8266 / ESP32)
        FuturisticGlassCard(isDarkMode = isDark) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Wifi, contentDescription = null, tint = AccentCyan)
                Text(
                    text = strings.wifiCardTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = strings.wifiCardDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = wifiUrlInput,
                onValueChange = { wifiUrlInput = it },
                label = { Text(strings.wifiUrlLabel, fontSize = 11.sp) },
                placeholder = { Text("http://192.168.4.1/gas") },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wifi_endpoint_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onConnectWifi(wifiUrlInput) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("connect_wifi_device_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan,
                    contentColor = Color(0xFF040914)
                )
            ) {
                Icon(Icons.Filled.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.wifiConnectBtn, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }

        // 4. Bluetooth Classic SPP Card (HC-05 / HC-06)
        FuturisticGlassCard(isDarkMode = isDark) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Bluetooth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = strings.btCardTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = strings.btCardDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onScanBluetooth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("scan_bluetooth_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
            ) {
                Icon(Icons.Filled.BluetoothConnected, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.btScanBtn, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            if (uiState.bluetoothScannedOnce) {
                Spacer(modifier = Modifier.height(10.dp))
                if (uiState.pairedBluetoothDevices.isEmpty()) {
                    Text(
                        text = strings.btEmptyPaired,
                        style = MaterialTheme.typography.bodySmall,
                        color = WarningAmber
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.pairedBluetoothDevices.forEach { device ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color.Black.copy(alpha = 0.25f) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = device.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = device.address,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Button(
                                        onClick = { onConnectBluetooth(device) },
                                        modifier = Modifier.minimumInteractiveComponentSize(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AccentTeal,
                                            contentColor = Color(0xFF040914)
                                        )
                                    ) {
                                        Text(strings.btConnectBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Hardware Connection Terminal Log
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isDark) Color.Black.copy(alpha = 0.45f) else Color(0xFF0F172A),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Terminal,
                            contentDescription = null,
                            tint = AccentTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = strings.terminalTitle,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    TextButton(
                        onClick = onClearLogs,
                        modifier = Modifier.testTag("clear_terminal_button")
                    ) {
                        Text(
                            text = strings.btnClear,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .verticalScroll(logScrollState),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    uiState.terminalLogs.forEach { entry ->
                        val color = when (entry.tone) {
                            LogTone.DANGER -> DangerRed
                            LogTone.ACCENT -> AccentTeal
                            LogTone.WARNING -> WarningAmber
                            LogTone.NORMAL -> Color(0xFFCBD5E1)
                        }
                        Text(
                            text = "[${entry.timestamp}] ${entry.message}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = color,
                            fontWeight = if (entry.tone == LogTone.DANGER) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
