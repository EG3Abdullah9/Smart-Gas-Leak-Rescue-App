package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.PiezoBuzzerPlayer
import com.example.data.AlertGateway
import com.example.data.AlertProvider
import com.example.data.PersistedSettings
import com.example.data.SettingsRepository
import com.example.device.BluetoothDeviceMonitor
import com.example.device.PairedBtDevice
import com.example.device.WifiDeviceMonitor
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.usb.ArduinoUsbMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

enum class AppScreen {
    DEVICE_CONNECT,
    APP_SIMULATOR
}

enum class DeviceConnectionTransport {
    NONE,
    USB,
    WIFI,
    BLUETOOTH
}

enum class SmsSendMode {
    AUTO,
    MANUAL
}

enum class LogTone {
    NORMAL,
    ACCENT,
    WARNING,
    DANGER
}

enum class ToastTone {
    INFO,
    SUCCESS,
    WARNING,
    DANGER
}

data class GasDataPoint(
    val ppm: Int,
    val timestamp: String
)

data class TerminalLogEntry(
    val id: Long,
    val timestamp: String,
    val message: String,
    val tone: LogTone
)

data class AppChatMessage(
    val id: Long,
    val sender: String,
    val text: String,
    val isUser: Boolean
)

data class ToastEvent(
    val message: String,
    val tone: ToastTone
)

data class GasRescueUiState(
    val activeScreen: AppScreen = AppScreen.DEVICE_CONNECT,
    val gasPPM: Int = 120,
    val amperes: Float = 5.0f,
    val isEmergency: Boolean = false,
    val isFanActive: Boolean = false,
    val isValveShutOff: Boolean = false,
    val isPowerCut: Boolean = false,
    val isBuzzerActive: Boolean = false,
    val isAudioMuted: Boolean = false,
    val continuousLeak: Boolean = false,
    val smsSendMode: SmsSendMode = SmsSendMode.AUTO,
    val connectedTransport: DeviceConnectionTransport = DeviceConnectionTransport.NONE,
    val connectedDeviceLabel: String? = null,
    val pairedBluetoothDevices: List<PairedBtDevice> = emptyList(),
    val bluetoothScannedOnce: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showEmergencyDispatchDialog: Boolean = false,
    val showPhoneSavedBanner: Boolean = false,
    val settings: PersistedSettings = PersistedSettings(),
    val chartHistory: List<GasDataPoint> = emptyList(),
    val terminalLogs: List<TerminalLogEntry> = emptyList(),
    val appMessages: List<AppChatMessage> = emptyList()
) {
    val responseTimeSec: Double get() = max(0.4, 2.5 - (amperes * 0.18))
    val isDeviceConnected: Boolean get() = connectedTransport != DeviceConnectionTransport.NONE

    val activeChannelLabel: String
        get() = when {
            settings.provider == AlertProvider.NTFY && settings.ntfyTopic.isNotBlank() ->
                "ntfy.sh/${settings.ntfyTopic}"
            settings.provider == AlertProvider.TELEGRAM && settings.tgToken.isNotBlank() && settings.tgChatId.isNotBlank() ->
                "Telegram Bot Connected"
            settings.provider == AlertProvider.WEBHOOK && settings.webhookUrl.isNotBlank() ->
                "Webhook Active"
            else -> "Real SMS / WhatsApp Ready"
        }

    val isOnlineChannelConfigured: Boolean
        get() = when (settings.provider) {
            AlertProvider.NTFY -> settings.ntfyTopic.isNotBlank()
            AlertProvider.TELEGRAM -> settings.tgToken.isNotBlank() && settings.tgChatId.isNotBlank()
            AlertProvider.WEBHOOK -> settings.webhookUrl.isNotBlank()
            AlertProvider.LOCAL -> false
        }
}

class GasRescueViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)
    private val alertGateway = AlertGateway(application)
    private val piezoPlayer = PiezoBuzzerPlayer()
    private val usbMonitor = ArduinoUsbMonitor(application)
    private val wifiMonitor = WifiDeviceMonitor()
    private val bluetoothMonitor = BluetoothDeviceMonitor(application)

    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.US)
    private var idCounter = 1L

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<GasRescueUiState> = _uiState.asStateFlow()

    private val _toastEvents = MutableSharedFlow<ToastEvent>(extraBufferCapacity = 8)
    val toastEvents: SharedFlow<ToastEvent> = _toastEvents.asSharedFlow()

    init {
        alertGateway.ensureNotificationChannel()

        viewModelScope.launch {
            settingsRepo.settingsFlow.collect { persisted ->
                _uiState.update { state ->
                    val currentWelcome = state.appMessages.firstOrNull()
                    val updatedMessages = if (currentWelcome != null && currentWelcome.id == 1L) {
                        val strings = AppStrings.forLang(persisted.language)
                        listOf(currentWelcome.copy(text = strings.welcomeSystemMsg)) + state.appMessages.drop(1)
                    } else {
                        state.appMessages
                    }
                    state.copy(settings = persisted, appMessages = updatedMessages)
                }
            }
        }

        startSensorPhysicsLoop()
    }

    private fun createInitialState(): GasRescueUiState {
        val nowMs = System.currentTimeMillis()
        val initialPoints = (0 until 10).map { idx ->
            val t = nowMs - (9 - idx) * 2000L
            GasDataPoint(ppm = 120, timestamp = timeFormatter.format(Date(t)))
        }
        val initialLogs = listOf(
            TerminalLogEntry(idCounter++, "00:00:01", "Hardware Connection Manager Ready (USB / Wi-Fi / BT).", LogTone.NORMAL),
            TerminalLogEntry(idCounter++, "00:00:02", "MQ-2 Profile Loaded (Threshold: 350 PPM).", LogTone.ACCENT),
            TerminalLogEntry(idCounter++, "00:00:03", "Waiting for physical device pairing or App Simulator.", LogTone.NORMAL)
        )
        val initialMessages = listOf(
            AppChatMessage(
                id = idCounter++,
                sender = "System",
                text = AppStrings.forLang(AppLanguage.AR).welcomeSystemMsg,
                isUser = false
            )
        )
        return GasRescueUiState(
            chartHistory = initialPoints,
            terminalLogs = initialLogs,
            appMessages = initialMessages
        )
    }

    private fun startSensorPhysicsLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(2000L)
                val current = _uiState.value
                // Only run simulated physics when not actively streaming from a physical hardware device
                if (current.connectedTransport == DeviceConnectionTransport.NONE) {
                    val fanActive = current.isFanActive
                    if (fanActive && !current.continuousLeak && current.gasPPM > 110) {
                        val valveBonus = if (current.isValveShutOff) 12 else 0
                        val ventRate = (18.0 + current.amperes * 3.5 + valveBonus).roundToInt()
                        val nextPpm = max(105, current.gasPPM - ventRate)
                        updateGasPPM(nextPpm, pushToChart = true)
                    } else {
                        val jitter = Random.nextInt(-3, 4)
                        val nextPpm = (current.gasPPM + jitter).coerceIn(80, 950)
                        if (current.gasPPM <= 350 || current.continuousLeak) {
                            pushChartPoint(nextPpm)
                        }
                    }
                }
            }
        }
    }

    private fun syncBuzzer() {
        val state = _uiState.value
        val shouldBeep = state.isBuzzerActive && !state.isAudioMuted
        piezoPlayer.setBeeping(shouldBeep, viewModelScope)
    }

    fun selectScreen(screen: AppScreen) {
        _uiState.update { it.copy(activeScreen = screen) }
    }

    fun openSettings() {
        _uiState.update { it.copy(showSettingsDialog = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(showSettingsDialog = false) }
    }

    fun toggleAudioMute() {
        _uiState.update { it.copy(isAudioMuted = !it.isAudioMuted) }
        syncBuzzer()
    }

    fun toggleFanOverride() {
        val next = !_uiState.value.isFanActive
        val isAr = _uiState.value.settings.language == AppLanguage.AR
        _uiState.update { it.copy(isFanActive = next) }
        appendTerminalLog("Exhaust Fan Relay #1: ${if (next) "ON" else "OFF"}", LogTone.ACCENT)
        _toastEvents.tryEmit(
            ToastEvent(
                if (isAr) {
                    if (next) "✓ تم تشغيل شفاط التهوية" else "تم إيقاف شفاط التهوية"
                } else {
                    if (next) "✓ Ventilation Fan Activated" else "Ventilation Fan Stopped"
                },
                ToastTone.INFO
            )
        )
    }

    fun toggleGasValve() {
        val next = !_uiState.value.isValveShutOff
        val isAr = _uiState.value.settings.language == AppLanguage.AR
        _uiState.update {
            it.copy(
                isValveShutOff = next,
                continuousLeak = if (next) false else it.continuousLeak
            )
        }
        appendTerminalLog(
            "SG90 Servo Valve: ${if (next) "SHUT OFF (90°)" else "OPEN (0°)"}",
            if (next) LogTone.DANGER else LogTone.ACCENT
        )
        _toastEvents.tryEmit(
            ToastEvent(
                if (isAr) {
                    if (next) "🔒 تم قفل محبس الغاز بزاوية 90°" else "🔓 تم فتح محبس الغاز (0°)"
                } else {
                    if (next) "🔒 Gas Valve Shut Off (90°)" else "🔓 Gas Valve Opened (0°)"
                },
                if (next) ToastTone.SUCCESS else ToastTone.WARNING
            )
        )
    }

    fun togglePowerCutOverride() {
        val next = !_uiState.value.isPowerCut
        _uiState.update { it.copy(isPowerCut = next) }
        appendTerminalLog("Main Power Relay #2: ${if (next) "CUT OFF" else "ON"}", LogTone.DANGER)
    }

    fun toggleBuzzerOverride() {
        val next = !_uiState.value.isBuzzerActive
        val isAr = _uiState.value.settings.language == AppLanguage.AR
        _uiState.update { state ->
            state.copy(
                isBuzzerActive = next,
                isAudioMuted = if (next) false else state.isAudioMuted
            )
        }
        syncBuzzer()
        appendTerminalLog("Alarm Siren (Pin D8): ${if (next) "TRIGGERED" else "OFF"}", LogTone.WARNING)
        _toastEvents.tryEmit(
            ToastEvent(
                if (isAr) {
                    if (next) "🚨 تم إطلاق صفارة الإنذار" else "تم إيقاف صفارة الإنذار"
                } else {
                    if (next) "🚨 Alarm Siren Triggered" else "Alarm Siren Stopped"
                },
                if (next) ToastTone.DANGER else ToastTone.INFO
            )
        )
    }

    fun openEmergencyDispatchDialog() {
        val msg = buildAlertMessage("🚨 طوارئ! تسرب غاز (${_uiState.value.gasPPM} PPM)")
        appendAppChat("Emergency Dispatch", msg, isUser = false)
        dispatchOnlineAlert(msg, showFeedbackToast = false)
        _uiState.update { it.copy(showEmergencyDispatchDialog = true) }
    }

    fun closeEmergencyDispatchDialog() {
        _uiState.update { it.copy(showEmergencyDispatchDialog = false) }
    }

    fun triggerManualAlertFromDevice() {
        val msg = buildAlertMessage("🚨 Manual Alert Triggered from Dashboard")
        appendAppChat("Manual Alert", msg, isUser = false)
        dispatchOnlineAlert(msg, showFeedbackToast = true)
    }

    fun triggerManualSmsButton() {
        val ppm = _uiState.value.gasPPM
        val msg = buildAlertMessage("🚨 Manual Emergency Alert ($ppm PPM)")
        appendAppChat("Manual SMS/Push", msg, isUser = false)
        dispatchOnlineAlert(msg, showFeedbackToast = true)
    }

    fun setSmsSendMode(mode: SmsSendMode) {
        _uiState.update { it.copy(smsSendMode = mode) }
    }

    fun setContinuousLeak(enabled: Boolean) {
        _uiState.update { it.copy(continuousLeak = enabled) }
    }

    fun setAmperes(amps: Float) {
        _uiState.update { it.copy(amperes = amps.coerceIn(1.0f, 10.0f)) }
    }

    fun updateGasPPM(rawValue: Int, pushToChart: Boolean = true) {
        val clamped = min(950, max(50, rawValue))
        val prevState = _uiState.value
        val wasEmergency = prevState.isEmergency
        val nowEmergency = clamped > 350

        _uiState.update { state ->
            if (nowEmergency && !wasEmergency && state.smsSendMode == SmsSendMode.AUTO) {
                state.copy(
                    gasPPM = clamped,
                    isEmergency = true,
                    isFanActive = true,
                    isValveShutOff = true,
                    isPowerCut = true,
                    isBuzzerActive = true
                )
            } else if (!nowEmergency && wasEmergency) {
                state.copy(
                    gasPPM = clamped,
                    isEmergency = false,
                    showEmergencyDispatchDialog = false
                )
            } else {
                state.copy(gasPPM = clamped, isEmergency = nowEmergency)
            }
        }
        syncBuzzer()

        if (nowEmergency && !wasEmergency) {
            appendTerminalLog("EMERGENCY: Gas Leak Detected ($clamped PPM)! Rapid Response Ready.", LogTone.DANGER)
            if (prevState.smsSendMode == SmsSendMode.AUTO) {
                val alertMsg = buildAlertMessage("🚨 طوارئ! تسرب غاز مرتفع ($clamped PPM)")
                appendAppChat("Auto GSM Alert", alertMsg, isUser = false)
                dispatchOnlineAlert(alertMsg, showFeedbackToast = false)
            }
        } else if (!nowEmergency && wasEmergency) {
            appendTerminalLog("Gas concentration normalized ($clamped PPM). System safe.", LogTone.ACCENT)
            appendAppChat("System Update", "✓ Gas normalized ($clamped PPM). Area safe.", isUser = false)
        }

        if (pushToChart) {
            pushChartPoint(clamped)
        }
    }

    fun activateNormalPreset() {
        _uiState.update {
            it.copy(
                continuousLeak = false,
                isFanActive = false,
                isValveShutOff = false,
                isPowerCut = false,
                isBuzzerActive = false,
                showEmergencyDispatchDialog = false
            )
        }
        updateGasPPM(120, pushToChart = true)
    }

    fun activateDangerPreset() {
        updateGasPPM(780, pushToChart = true)
    }

    fun triggerFullRescueTest() {
        _uiState.update {
            it.copy(
                isFanActive = true,
                isValveShutOff = true,
                isPowerCut = true,
                isBuzzerActive = true,
                isAudioMuted = false
            )
        }
        updateGasPPM(780, pushToChart = true)
        appendTerminalLog("FULL RESCUE SEQUENCE: Fan ON + Valve 90° + Power Cut + Siren ON", LogTone.DANGER)
    }

    fun clearTerminalLogs() {
        _uiState.update { it.copy(terminalLogs = emptyList()) }
    }

    fun saveEmergencyPhone(phoneInput: String) {
        val cleaned = phoneInput.trim()
        if (cleaned.isEmpty()) return
        viewModelScope.launch {
            settingsRepo.setPhone(cleaned)
            appendTerminalLog("Emergency contact updated to: $cleaned", LogTone.ACCENT)
            _uiState.update { it.copy(showPhoneSavedBanner = true) }
            delay(3000L)
            _uiState.update { it.copy(showPhoneSavedBanner = false) }
        }
    }

    fun processRemoteCommand(commandText: String) {
        val trimmed = commandText.trim()
        if (trimmed.isEmpty()) return
        val current = _uiState.value
        appendAppChat("User (${current.settings.phone})", trimmed, isUser = true)

        val lower = trimmed.lowercase(Locale.ROOT)
        viewModelScope.launch {
            delay(350L)
            val state = _uiState.value
            val isAr = state.settings.language == AppLanguage.AR

            when {
                lower.contains("شفاط") || lower.contains("مروحة") || lower.contains("fan") -> {
                    val nextFan = !state.isFanActive
                    _uiState.update { it.copy(isFanActive = nextFan) }
                    val reply = if (nextFan) {
                        if (isAr) "تم تشغيل الشفاط الآلي عن بعد." else "Exhaust Fan ACTIVATED remotely."
                    } else {
                        if (isAr) "تم إيقاف الشفاط عن بعد." else "Exhaust Fan STOPPED remotely."
                    }
                    appendAppChat("System (SMS Reply)", reply, isUser = false)
                    appendTerminalLog("Remote Command: FAN ${if (nextFan) "ON" else "OFF"}", LogTone.ACCENT)
                }

                lower.contains("محبس") || lower.contains("صمام") || lower.contains("valve") -> {
                    val nextValve = !state.isValveShutOff
                    _uiState.update { it.copy(isValveShutOff = nextValve) }
                    val reply = if (nextValve) {
                        if (isAr) "تم قفل محبس الغاز آلياً بزاوية 90°." else "Gas Valve SHUT OFF (90°) remotely."
                    } else {
                        if (isAr) "تم فتح محبس الغاز (0°)." else "Gas Valve OPENED (0°) remotely."
                    }
                    appendAppChat("System (SMS Reply)", reply, isUser = false)
                    appendTerminalLog("Remote Command: VALVE ${if (nextValve) "CLOSED 90°" else "OPEN"}", LogTone.DANGER)
                }

                lower.contains("كهرباء") || lower.contains("فصل") || lower.contains("power") -> {
                    val nextCut = !state.isPowerCut
                    _uiState.update { it.copy(isPowerCut = nextCut) }
                    val reply = if (nextCut) {
                        if (isAr) "تم فصل التيار الكهربائي الرئيسي تماماً." else "Main Power CUT OFF remotely."
                    } else {
                        if (isAr) "تم إعادة توصيل التيار الكهربائي." else "Main Power RESTORED remotely."
                    }
                    appendAppChat("System (SMS Reply)", reply, isUser = false)
                    appendTerminalLog("Remote Command: POWER ${if (nextCut) "CUT" else "ON"}", LogTone.DANGER)
                }

                lower.contains("حالة") || lower.contains("فحص") || lower.contains("status") -> {
                    val report = buildAlertMessage("📊 Live System Status Report")
                    appendAppChat("System (SMS Reply)", report, isUser = false)
                    appendTerminalLog("Remote Command: STATUS REPORT SENT", LogTone.ACCENT)
                }

                lower.contains("تشغيل") || lower.contains("إعادة") || lower.contains("تصفير") ||
                    lower.contains("reset") || lower.contains("start") -> {
                    _uiState.update {
                        it.copy(
                            isEmergency = false,
                            isFanActive = false,
                            isValveShutOff = false,
                            isPowerCut = false,
                            isBuzzerActive = false,
                            continuousLeak = false
                        )
                    }
                    updateGasPPM(120, pushToChart = true)
                    val reply = if (isAr) {
                        "تم إعادة ضبط النظام والوضع الآن آمن 100%."
                    } else {
                        "System reset to normal (120 PPM)."
                    }
                    appendAppChat("System (SMS Reply)", reply, isUser = false)
                    appendTerminalLog("Remote Command: SYSTEM RESET TO NORMAL", LogTone.ACCENT)
                }

                lower.contains("إيقاف") || lower.contains("إنذار") || lower.contains("صامت") ||
                    lower.contains("stop") || lower.contains("mute") -> {
                    _uiState.update { it.copy(isAudioMuted = true, isBuzzerActive = false) }
                    syncBuzzer()
                    val reply = if (isAr) "تم كتم صفارة الإنذار." else "Alarm buzzer muted."
                    appendAppChat("System (SMS Reply)", reply, isUser = false)
                    appendTerminalLog("Remote Command: BUZZER MUTED", LogTone.WARNING)
                }

                else -> {
                    val reply = if (isAr) {
                        "أمر مستلم. الأوامر المتاحة: تشغيل الشفاط، فصل الكهرباء، فحص الحالة، تصفير."
                    } else {
                        "Command received. Try: fan, power, status, reset, mute."
                    }
                    appendAppChat("System (SMS Reply)", reply, isUser = false)
                }
            }
        }
    }

    fun toggleTheme() {
        val nextDark = !_uiState.value.settings.isDarkMode
        viewModelScope.launch { settingsRepo.setDarkMode(nextDark) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsRepo.setLanguage(language) }
    }

    fun setRating(stars: Int) {
        viewModelScope.launch {
            settingsRepo.setRating(stars)
            _toastEvents.tryEmit(ToastEvent("Thank you for rating $stars/5!", ToastTone.SUCCESS))
        }
    }

    fun saveMessagingSettings(
        provider: AlertProvider,
        ntfyTopic: String,
        tgToken: String,
        tgChatId: String,
        webhookUrl: String,
        closeAfter: Boolean
    ) {
        viewModelScope.launch {
            settingsRepo.saveMessagingConfig(provider, ntfyTopic, tgToken, tgChatId, webhookUrl)
            if (closeAfter) {
                val strings = AppStrings.forLang(_uiState.value.settings.language)
                _uiState.update { it.copy(showSettingsDialog = false) }
                _toastEvents.tryEmit(ToastEvent(strings.settingsSavedToast, ToastTone.SUCCESS))
            }
        }
    }

    fun sendTestRealMessage(
        provider: AlertProvider,
        ntfyTopic: String,
        tgToken: String,
        tgChatId: String,
        webhookUrl: String
    ) {
        viewModelScope.launch {
            settingsRepo.saveMessagingConfig(provider, ntfyTopic, tgToken, tgChatId, webhookUrl)
            val updatedSettings = _uiState.value.settings.copy(
                provider = provider,
                ntfyTopic = ntfyTopic.trim(),
                tgToken = tgToken.trim(),
                tgChatId = tgChatId.trim(),
                webhookUrl = webhookUrl.trim()
            )
            val testMsg = buildAlertMessage("🔔 TEST ALERT - Smart Gas Rescue System")
            val result = alertGateway.sendAlert(
                settings = updatedSettings,
                messageText = testMsg,
                gasPpm = _uiState.value.gasPPM,
                isEmergency = _uiState.value.isEmergency
            )
            if (result.deliveredOnline) {
                appendTerminalLog(result.statusNote, LogTone.ACCENT)
                appendAppChat("Real Delivery Receipt", result.statusNote, isUser = false)
                _toastEvents.tryEmit(ToastEvent(result.statusNote, ToastTone.SUCCESS))
            } else {
                appendTerminalLog(result.statusNote, LogTone.WARNING)
                _toastEvents.tryEmit(ToastEvent(result.statusNote, ToastTone.WARNING))
            }
        }
    }

    // =========================================
    // HARDWARE CONNECTION METHODS (1st Window)
    // =========================================

    fun connectArduinoUsb() {
        val isAr = _uiState.value.settings.language == AppLanguage.AR
        val scanResult = usbMonitor.scanAndConnect(
            scope = viewModelScope,
            onPpmReceived = { ppm ->
                viewModelScope.launch { updateGasPPM(ppm, pushToChart = true) }
            },
            onError = { msg ->
                viewModelScope.launch {
                    appendTerminalLog(msg, LogTone.WARNING)
                    _uiState.update {
                        if (it.connectedTransport == DeviceConnectionTransport.USB) {
                            it.copy(connectedTransport = DeviceConnectionTransport.NONE, connectedDeviceLabel = null)
                        } else it
                    }
                }
            }
        )

        when (scanResult) {
            is ArduinoUsbMonitor.UsbScanResult.Connected -> {
                wifiMonitor.stop()
                bluetoothMonitor.stop()
                _uiState.update {
                    it.copy(
                        connectedTransport = DeviceConnectionTransport.USB,
                        connectedDeviceLabel = scanResult.deviceName
                    )
                }
                appendTerminalLog("USB Serial Connected: ${scanResult.deviceName}", LogTone.ACCENT)
                _toastEvents.tryEmit(
                    ToastEvent(
                        if (isAr) "تم الاتصال بـ Arduino: ${scanResult.deviceName}" else "Arduino USB Connected: ${scanResult.deviceName}",
                        ToastTone.SUCCESS
                    )
                )
            }
            is ArduinoUsbMonitor.UsbScanResult.PermissionRequested -> {
                _toastEvents.tryEmit(
                    ToastEvent(
                        if (isAr) "يرجى منح إذن USB للجهاز: ${scanResult.deviceName}" else "Grant USB permission for ${scanResult.deviceName} and tap again.",
                        ToastTone.INFO
                    )
                )
            }
            ArduinoUsbMonitor.UsbScanResult.NoDeviceFound -> {
                val msg = if (isAr) {
                    "لم يتم العثور على أردوينو متصل عبر منفذ USB OTG."
                } else {
                    "No Arduino USB OTG device detected."
                }
                appendTerminalLog(msg, LogTone.WARNING)
                _toastEvents.tryEmit(ToastEvent(msg, ToastTone.WARNING))
            }
            ArduinoUsbMonitor.UsbScanResult.NotSupported -> {
                val msg = if (isAr) "ميزة USB Host غير مدعومة على هذا الجهاز." else "USB Host is not supported on this device."
                _toastEvents.tryEmit(ToastEvent(msg, ToastTone.WARNING))
            }
        }
    }

    fun connectWifiDevice(url: String) {
        val cleaned = url.trim()
        if (cleaned.isEmpty()) return
        val isAr = _uiState.value.settings.language == AppLanguage.AR

        viewModelScope.launch { settingsRepo.setWifiDeviceUrl(cleaned) }
        usbMonitor.stop()
        bluetoothMonitor.stop()
        appendTerminalLog("Connecting to Wi-Fi endpoint: $cleaned ...", LogTone.NORMAL)

        wifiMonitor.connectAndPoll(
            url = cleaned,
            scope = viewModelScope,
            onConnected = { normalizedUrl ->
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(
                            connectedTransport = DeviceConnectionTransport.WIFI,
                            connectedDeviceLabel = normalizedUrl
                        )
                    }
                    appendTerminalLog("Wi-Fi Device Connected: $normalizedUrl", LogTone.ACCENT)
                    _toastEvents.tryEmit(
                        ToastEvent(
                            if (isAr) "تم ربط جهاز Wi-Fi بنجاح" else "Wi-Fi Device Connected!",
                            ToastTone.SUCCESS
                        )
                    )
                }
            },
            onPpmReceived = { ppm ->
                viewModelScope.launch { updateGasPPM(ppm, pushToChart = true) }
            },
            onError = { err ->
                viewModelScope.launch {
                    appendTerminalLog(err, LogTone.WARNING)
                    _uiState.update {
                        if (it.connectedTransport == DeviceConnectionTransport.WIFI) {
                            it.copy(connectedTransport = DeviceConnectionTransport.NONE, connectedDeviceLabel = null)
                        } else it
                    }
                    _toastEvents.tryEmit(ToastEvent(err, ToastTone.WARNING))
                }
            }
        )
    }

    fun hasBluetoothPermission(): Boolean = bluetoothMonitor.hasBluetoothConnectPermission()

    fun scanPairedBluetoothDevices() {
        val isAr = _uiState.value.settings.language == AppLanguage.AR
        val devices = bluetoothMonitor.getPairedDevices()
        _uiState.update {
            it.copy(pairedBluetoothDevices = devices, bluetoothScannedOnce = true)
        }
        val msg = if (devices.isEmpty()) {
            if (isAr) "لا توجد أجهزة بلوتوث مقترنة (تأكد من تشغيل البلوتوث)." else "No paired Bluetooth devices found."
        } else {
            if (isAr) "تم العثور على ${devices.size} جهاز بلوتوث مقترن." else "Found ${devices.size} paired Bluetooth device(s)."
        }
        appendTerminalLog(msg, if (devices.isEmpty()) LogTone.WARNING else LogTone.ACCENT)
    }

    fun connectBluetoothDevice(device: PairedBtDevice) {
        val isAr = _uiState.value.settings.language == AppLanguage.AR
        usbMonitor.stop()
        wifiMonitor.stop()
        appendTerminalLog("Connecting Bluetooth SPP to ${device.name} (${device.address})...", LogTone.NORMAL)

        bluetoothMonitor.connectToDevice(
            address = device.address,
            scope = viewModelScope,
            onConnected = { label ->
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(
                            connectedTransport = DeviceConnectionTransport.BLUETOOTH,
                            connectedDeviceLabel = label
                        )
                    }
                    appendTerminalLog("Bluetooth Connected: $label", LogTone.ACCENT)
                    _toastEvents.tryEmit(
                        ToastEvent(
                            if (isAr) "تم ربط البلوتوث: $label" else "Bluetooth Connected: $label",
                            ToastTone.SUCCESS
                        )
                    )
                }
            },
            onPpmReceived = { ppm ->
                viewModelScope.launch { updateGasPPM(ppm, pushToChart = true) }
            },
            onError = { err ->
                viewModelScope.launch {
                    appendTerminalLog(err, LogTone.WARNING)
                    _uiState.update {
                        if (it.connectedTransport == DeviceConnectionTransport.BLUETOOTH) {
                            it.copy(connectedTransport = DeviceConnectionTransport.NONE, connectedDeviceLabel = null)
                        } else it
                    }
                    _toastEvents.tryEmit(ToastEvent(err, ToastTone.WARNING))
                }
            }
        )
    }

    fun disconnectHardware() {
        val isAr = _uiState.value.settings.language == AppLanguage.AR
        usbMonitor.stop()
        wifiMonitor.stop()
        bluetoothMonitor.stop()
        _uiState.update {
            it.copy(connectedTransport = DeviceConnectionTransport.NONE, connectedDeviceLabel = null)
        }
        val msg = if (isAr) "تم قطع الاتصال بالجهاز." else "Disconnected from hardware device."
        appendTerminalLog(msg, LogTone.WARNING)
        _toastEvents.tryEmit(ToastEvent(msg, ToastTone.INFO))
    }

    fun buildAlertMessage(customReason: String? = null): String {
        val s = _uiState.value
        val reason = customReason ?: if (s.isEmergency) "🚨 GAS LEAK EMERGENCY" else "ℹ️ System Status Update"
        val fanState = if (s.isFanActive) "ON" else "OFF"
        val valveState = if (s.isValveShutOff) "CLOSED (90°)" else "OPEN (0°)"
        val powerState = if (s.isPowerCut) "CUT OFF" else "CONNECTED"
        return "$reason\nGas (MQ-2): ${s.gasPPM} PPM\nGas Valve: $valveState\nExhaust Fan: $fanState\nMain Power: $powerState\nCurrent: ${String.format(Locale.US, "%.1f", s.amperes)} A\nTarget Phone: ${s.settings.phone}"
    }

    fun notifyToast(message: String, tone: ToastTone = ToastTone.INFO) {
        _toastEvents.tryEmit(ToastEvent(message, tone))
    }

    private fun dispatchOnlineAlert(message: String, showFeedbackToast: Boolean) {
        viewModelScope.launch {
            val state = _uiState.value
            val result = alertGateway.sendAlert(
                settings = state.settings,
                messageText = message,
                gasPpm = state.gasPPM,
                isEmergency = state.isEmergency
            )
            if (result.deliveredOnline) {
                appendTerminalLog(result.statusNote, LogTone.ACCENT)
                appendAppChat("Real Delivery Receipt", result.statusNote, isUser = false)
                _toastEvents.tryEmit(ToastEvent(result.statusNote, ToastTone.SUCCESS))
            } else {
                appendTerminalLog(result.statusNote, LogTone.WARNING)
                if (showFeedbackToast) {
                    _toastEvents.tryEmit(ToastEvent(result.statusNote, ToastTone.WARNING))
                }
            }
        }
    }

    private fun pushChartPoint(ppm: Int) {
        val label = timeFormatter.format(Date())
        _uiState.update { state ->
            val updated = (state.chartHistory + GasDataPoint(ppm, label)).takeLast(12)
            state.copy(chartHistory = updated)
        }
    }

    private fun appendTerminalLog(message: String, tone: LogTone) {
        val ts = timeFormatter.format(Date())
        _uiState.update { state ->
            val updated = (state.terminalLogs + TerminalLogEntry(idCounter++, ts, message, tone)).takeLast(60)
            state.copy(terminalLogs = updated)
        }
    }

    private fun appendAppChat(sender: String, text: String, isUser: Boolean) {
        _uiState.update { state ->
            val updated = (state.appMessages + AppChatMessage(idCounter++, sender, text, isUser)).takeLast(60)
            state.copy(appMessages = updated)
        }
    }

    override fun onCleared() {
        super.onCleared()
        piezoPlayer.stop()
        usbMonitor.stop()
        wifiMonitor.stop()
        bluetoothMonitor.stop()
    }
}
