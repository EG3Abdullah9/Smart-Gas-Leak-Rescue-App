package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.AppScreen
import com.example.ui.GasRescueViewModel
import com.example.ui.ToastTone
import com.example.ui.components.EmergencyDispatchDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TopControlHeader
import com.example.ui.screens.AppSimulatorScreen
import com.example.ui.screens.DeviceConnectScreen
import com.example.ui.theme.BgDark
import com.example.ui.theme.BgLight
import com.example.ui.theme.GasRescueTheme
import com.example.ui.theme.PanelDark
import com.example.ui.theme.PanelLight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GasRescueRoot()
        }
    }
}

@Composable
fun GasRescueRoot(
    viewModel: GasRescueViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = remember(uiState.settings.language) {
        AppStrings.forLang(uiState.settings.language)
    }
    val layoutDirection = if (uiState.settings.language == AppLanguage.AR) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val msg = if (granted) {
            if (uiState.settings.language == AppLanguage.AR) "✓ تم تفعيل إشعارات الهاتف بنجاح" else "✓ Android notifications enabled"
        } else {
            if (uiState.settings.language == AppLanguage.AR) "تم رفض إذن الإشعارات" else "Notification permission denied"
        }
        viewModel.notifyToast(msg, if (granted) ToastTone.SUCCESS else ToastTone.WARNING)
    }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.scanPairedBluetoothDevices()
        } else {
            val msg = if (uiState.settings.language == AppLanguage.AR) {
                "يرجى منح إذن البلوتوث للبحث عن الأجهزة المقترنة."
            } else {
                "Bluetooth permission is required to list paired devices."
            }
            viewModel.notifyToast(msg, ToastTone.WARNING)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { event ->
            snackbarHostState.showSnackbar(event.message)
        }
    }

    BackHandler(enabled = uiState.activeScreen == AppScreen.APP_SIMULATOR) {
        viewModel.selectScreen(AppScreen.DEVICE_CONNECT)
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        GasRescueTheme(darkTheme = uiState.settings.isDarkMode) {
            val isDark = uiState.settings.isDarkMode
            val bgBrush = if (isDark) {
                Brush.verticalGradient(listOf(PanelDark, BgDark))
            } else {
                Brush.verticalGradient(listOf(PanelLight, BgLight))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgBrush)
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
                        NavigationBar(
                            containerColor = if (isDark) PanelDark.copy(alpha = 0.94f) else Color.White.copy(alpha = 0.95f),
                            tonalElevation = 8.dp,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            NavigationBarItem(
                                selected = uiState.activeScreen == AppScreen.DEVICE_CONNECT,
                                onClick = { viewModel.selectScreen(AppScreen.DEVICE_CONNECT) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Cable,
                                        contentDescription = strings.navConnect
                                    )
                                },
                                label = {
                                    Text(
                                        text = strings.navConnect,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("nav_device_connect")
                            )

                            NavigationBarItem(
                                selected = uiState.activeScreen == AppScreen.APP_SIMULATOR,
                                onClick = { viewModel.selectScreen(AppScreen.APP_SIMULATOR) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Speed,
                                        contentDescription = strings.navAppSim
                                    )
                                },
                                label = {
                                    Text(
                                        text = strings.navAppSim,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("nav_app_simulator")
                            )
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        TopControlHeader(
                            uiState = uiState,
                            strings = strings,
                            onToggleMute = viewModel::toggleAudioMute,
                            onUsbClick = viewModel::connectArduinoUsb,
                            onOpenSettings = viewModel::openSettings
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        when (uiState.activeScreen) {
                            AppScreen.DEVICE_CONNECT -> {
                                DeviceConnectScreen(
                                    uiState = uiState,
                                    strings = strings,
                                    onConnectUsb = viewModel::connectArduinoUsb,
                                    onConnectWifi = viewModel::connectWifiDevice,
                                    onScanBluetooth = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !viewModel.hasBluetoothPermission()) {
                                            bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                        } else {
                                            viewModel.scanPairedBluetoothDevices()
                                        }
                                    },
                                    onConnectBluetooth = viewModel::connectBluetoothDevice,
                                    onDisconnect = viewModel::disconnectHardware,
                                    onOpenAppSimulator = { viewModel.selectScreen(AppScreen.APP_SIMULATOR) },
                                    onClearLogs = viewModel::clearTerminalLogs
                                )
                            }

                            AppScreen.APP_SIMULATOR -> {
                                AppSimulatorScreen(
                                    uiState = uiState,
                                    strings = strings,
                                    alertMessageProvider = { viewModel.buildAlertMessage() },
                                    onToggleFan = viewModel::toggleFanOverride,
                                    onToggleValve = viewModel::toggleGasValve,
                                    onTogglePower = viewModel::togglePowerCutOverride,
                                    onToggleBuzzer = viewModel::toggleBuzzerOverride,
                                    onTriggerSmsCard = viewModel::triggerManualAlertFromDevice,
                                    onOpenSmsWhatsappDispatch = viewModel::openEmergencyDispatchDialog,
                                    onContinuousLeakChange = viewModel::setContinuousLeak,
                                    onSmsModeChange = viewModel::setSmsSendMode,
                                    onManualTriggerAlert = viewModel::triggerManualSmsButton,
                                    onGasSliderChange = { ppm -> viewModel.updateGasPPM(ppm, pushToChart = true) },
                                    onAmperesSliderChange = viewModel::setAmperes,
                                    onPresetNormal = viewModel::activateNormalPreset,
                                    onPresetDanger = viewModel::activateDangerPreset,
                                    onFullRescueTest = viewModel::triggerFullRescueTest,
                                    onSavePhone = viewModel::saveEmergencyPhone,
                                    onSendCommand = viewModel::processRemoteCommand
                                )
                            }
                        }

                        Text(
                            text = "Smart Gas Leak Rescue System © 2026 - Real-Time Hardware & Messaging Platform",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }
                }

                if (uiState.showSettingsDialog) {
                    SettingsDialog(
                        uiState = uiState,
                        strings = strings,
                        onDismiss = viewModel::closeSettings,
                        onSaveAndClose = { provider, topic, token, chatId, webhook ->
                            viewModel.saveMessagingSettings(
                                provider = provider,
                                ntfyTopic = topic,
                                tgToken = token,
                                tgChatId = chatId,
                                webhookUrl = webhook,
                                closeAfter = true
                            )
                        },
                        onTestRealMessage = { provider, topic, token, chatId, webhook ->
                            viewModel.sendTestRealMessage(provider, topic, token, chatId, webhook)
                        },
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val msg = if (uiState.settings.language == AppLanguage.AR) {
                                    "✓ الإشعارات مفعلة تلقائياً على هذا الإصدار"
                                } else {
                                    "✓ Notifications enabled automatically on this Android version"
                                }
                                viewModel.notifyToast(msg, ToastTone.SUCCESS)
                            }
                        },
                        onToggleTheme = viewModel::toggleTheme,
                        onSelectLanguage = viewModel::setLanguage,
                        onRateSystem = viewModel::setRating
                    )
                }

                if (uiState.showEmergencyDispatchDialog) {
                    EmergencyDispatchDialog(
                        phone = uiState.settings.phone,
                        alertMessage = viewModel.buildAlertMessage(),
                        strings = strings,
                        onDismiss = viewModel::closeEmergencyDispatchDialog
                    )
                }
            }
        }
    }
}
