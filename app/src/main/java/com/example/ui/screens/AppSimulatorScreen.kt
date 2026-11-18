package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cyclone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.LocalizedStrings
import com.example.ui.GasRescueUiState
import com.example.ui.SmsSendMode
import com.example.ui.components.EmergencyDangerResponseCard
import com.example.ui.components.FuturisticGlassCard
import com.example.ui.components.GasGaugeCard
import com.example.ui.components.GasLiveChart
import com.example.ui.components.MobileRemoteControlPanel
import com.example.ui.components.PackedHardwareCommandPad
import com.example.ui.components.launchNativeSms
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.WarningAmber
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AppSimulatorScreen(
    uiState: GasRescueUiState,
    strings: LocalizedStrings,
    alertMessageProvider: () -> String,
    onToggleFan: () -> Unit,
    onToggleValve: () -> Unit,
    onTogglePower: () -> Unit,
    onToggleBuzzer: () -> Unit,
    onTriggerSmsCard: () -> Unit,
    onOpenSmsWhatsappDispatch: () -> Unit,
    onContinuousLeakChange: (Boolean) -> Unit,
    onSmsModeChange: (SmsSendMode) -> Unit,
    onManualTriggerAlert: () -> Unit,
    onGasSliderChange: (Int) -> Unit,
    onAmperesSliderChange: (Float) -> Unit,
    onPresetNormal: () -> Unit,
    onPresetDanger: () -> Unit,
    onFullRescueTest: () -> Unit,
    onSavePhone: (String) -> Unit,
    onSendCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = uiState.settings.isDarkMode

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isWide = maxWidth >= 760.dp

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Main Dashboard Hero: Simulated MQ-2 Gas Gauge + Text Display
            GasGaugeCard(
                gasPPM = uiState.gasPPM,
                isDarkMode = isDark,
                strings = strings
            )

            // 1b. Dynamic 4-Button Danger Response Panel (Appears whenever gasPPM > 350)
            EmergencyDangerResponseCard(
                uiState = uiState,
                strings = strings,
                onActivateFan = onToggleFan,
                onToggleValve = onToggleValve,
                onTriggerAlarm = onToggleBuzzer,
                onOpenSmsWhatsappDispatch = onOpenSmsWhatsappDispatch
            )

            // 2. Telemetry Summary Row (Current, Response Time, GSM Channel)
            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CurrentSummaryCard(uiState, strings, Modifier.weight(1f))
                    ResponseSummaryCard(uiState, strings, Modifier.weight(1f))
                    GsmSummaryCard(uiState, strings, Modifier.weight(1f))
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CurrentSummaryCard(uiState, strings, Modifier.weight(1f))
                    ResponseSummaryCard(uiState, strings, Modifier.weight(1f))
                }
                GsmSummaryCard(uiState, strings, Modifier.fillMaxWidth())
            }

            // 3. Packed Quick Hardware Command Pad + Simulation Controls + Live Actuators/Chart
            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(7f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        PackedHardwareCommandPad(
                            uiState = uiState,
                            strings = strings,
                            onSelectGasPreset = onGasSliderChange,
                            onToggleFan = onToggleFan,
                            onToggleValve = onToggleValve,
                            onTogglePower = onTogglePower,
                            onToggleBuzzer = onToggleBuzzer,
                            onFullRescueTest = onFullRescueTest,
                            onResetAll = onPresetNormal
                        )
                        SimActuatorsAndChartPanel(
                            uiState = uiState,
                            strings = strings,
                            isDark = isDark,
                            onToggleFan = onToggleFan,
                            onToggleValve = onToggleValve,
                            onTogglePower = onTogglePower,
                            onToggleBuzzer = onToggleBuzzer,
                            onTriggerSmsCard = onTriggerSmsCard,
                            onContinuousLeakChange = onContinuousLeakChange
                        )
                        SimControlsPanel(
                            uiState = uiState,
                            strings = strings,
                            isDark = isDark,
                            alertMessageProvider = alertMessageProvider,
                            onSmsModeChange = onSmsModeChange,
                            onManualTriggerAlert = onManualTriggerAlert,
                            onGasSliderChange = onGasSliderChange,
                            onAmperesSliderChange = onAmperesSliderChange,
                            onPresetNormal = onPresetNormal,
                            onPresetDanger = onPresetDanger
                        )
                    }
                    MobileRemoteControlPanel(
                        uiState = uiState,
                        strings = strings,
                        alertMessageProvider = alertMessageProvider,
                        onSavePhone = onSavePhone,
                        onSendCommand = onSendCommand,
                        modifier = Modifier.weight(5f)
                    )
                }
            } else {
                PackedHardwareCommandPad(
                    uiState = uiState,
                    strings = strings,
                    onSelectGasPreset = onGasSliderChange,
                    onToggleFan = onToggleFan,
                    onToggleValve = onToggleValve,
                    onTogglePower = onTogglePower,
                    onToggleBuzzer = onToggleBuzzer,
                    onFullRescueTest = onFullRescueTest,
                    onResetAll = onPresetNormal,
                    modifier = Modifier.fillMaxWidth()
                )
                SimControlsPanel(
                    uiState = uiState,
                    strings = strings,
                    isDark = isDark,
                    alertMessageProvider = alertMessageProvider,
                    onSmsModeChange = onSmsModeChange,
                    onManualTriggerAlert = onManualTriggerAlert,
                    onGasSliderChange = onGasSliderChange,
                    onAmperesSliderChange = onAmperesSliderChange,
                    onPresetNormal = onPresetNormal,
                    onPresetDanger = onPresetDanger,
                    modifier = Modifier.fillMaxWidth()
                )
                SimActuatorsAndChartPanel(
                    uiState = uiState,
                    strings = strings,
                    isDark = isDark,
                    onToggleFan = onToggleFan,
                    onToggleValve = onToggleValve,
                    onTogglePower = onTogglePower,
                    onToggleBuzzer = onToggleBuzzer,
                    onTriggerSmsCard = onTriggerSmsCard,
                    onContinuousLeakChange = onContinuousLeakChange,
                    modifier = Modifier.fillMaxWidth()
                )
                MobileRemoteControlPanel(
                    uiState = uiState,
                    strings = strings,
                    alertMessageProvider = alertMessageProvider,
                    onSavePhone = onSavePhone,
                    onSendCommand = onSendCommand,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CurrentSummaryCard(
    uiState: GasRescueUiState,
    strings: LocalizedStrings,
    modifier: Modifier = Modifier
) {
    val isDark = uiState.settings.isDarkMode
    val amps = uiState.amperes
    val (impactText, impactColor) = when {
        amps < 3.0f -> strings.currSlow to WarningAmber
        amps > 8.5f -> strings.currOverload to DangerRed
        else -> strings.currFast to SafeEmerald
    }

    FuturisticGlassCard(isDarkMode = isDark, modifier = modifier, contentPadding = 12.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(Icons.Filled.Bolt, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(15.dp))
            Text(
                text = strings.metricCurrent,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${String.format(Locale.US, "%.1f", amps)} A",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = WarningAmber
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = impactText,
            color = impactColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ResponseSummaryCard(
    uiState: GasRescueUiState,
    strings: LocalizedStrings,
    modifier: Modifier = Modifier
) {
    val isDark = uiState.settings.isDarkMode
    FuturisticGlassCard(isDarkMode = isDark, modifier = modifier, contentPadding = 12.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(Icons.Filled.Timer, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(15.dp))
            Text(
                text = strings.metricResponse,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "< ${String.format(Locale.US, "%.2f", uiState.responseTimeSec)} ${strings.unitSec}",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AccentCyan
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = strings.autoRespBadge,
            color = AccentCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GsmSummaryCard(
    uiState: GasRescueUiState,
    strings: LocalizedStrings,
    modifier: Modifier = Modifier
) {
    val isDark = uiState.settings.isDarkMode
    FuturisticGlassCard(isDarkMode = isDark, modifier = modifier, contentPadding = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.SignalCellularAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text(
                    text = strings.metricGsm,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "SIM800L Online",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = SafeEmerald
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Filled.CellTower, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text(
                text = "${strings.emergencyNet} • ${uiState.activeChannelLabel}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SimControlsPanel(
    uiState: GasRescueUiState,
    strings: LocalizedStrings,
    isDark: Boolean,
    alertMessageProvider: () -> String,
    onSmsModeChange: (SmsSendMode) -> Unit,
    onManualTriggerAlert: () -> Unit,
    onGasSliderChange: (Int) -> Unit,
    onAmperesSliderChange: (Float) -> Unit,
    onPresetNormal: () -> Unit,
    onPresetDanger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    FuturisticGlassCard(isDarkMode = isDark, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Science, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                Text(
                    text = strings.simTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = WarningAmber,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Surface(shape = CircleShape, color = WarningAmber.copy(alpha = 0.2f)) {
                Text(
                    text = strings.simBadge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarningAmber,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Gas Slider
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isDark) Color.Black.copy(alpha = 0.25f) else Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.sliderGasLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${uiState.gasPPM} PPM",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isEmergency) DangerRed else MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = uiState.gasPPM.toFloat(),
                    onValueChange = { onGasSliderChange(it.roundToInt()) },
                    valueRange = 50f..950f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (uiState.isEmergency) DangerRed else AccentTeal,
                        activeTrackColor = if (uiState.isEmergency) DangerRed else AccentTeal
                    ),
                    modifier = Modifier.testTag("gas_ppm_slider")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(-50, -10, 10, 50).forEach { delta ->
                        val label = if (delta > 0) "+$delta" else "$delta"
                        val isIncrease = delta > 0
                        OutlinedButton(
                            onClick = { onGasSliderChange((uiState.gasPPM + delta).coerceIn(50, 950)) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                            border = BorderStroke(
                                1.dp,
                                (if (isIncrease) DangerRed else SafeEmerald).copy(alpha = 0.45f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = (if (isIncrease) DangerRed else SafeEmerald).copy(alpha = 0.12f),
                                contentColor = if (isIncrease) DangerRed else SafeEmerald
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("ppm_step_${if (delta > 0) "plus_$delta" else "minus_${-delta}"}")
                        ) {
                            Text(
                                text = label,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Current Slider
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isDark) Color.Black.copy(alpha = 0.25f) else Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.sliderCurrLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", uiState.amperes)} A",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                }
                Slider(
                    value = uiState.amperes,
                    onValueChange = { raw ->
                        val snapped = (raw * 2f).roundToInt() / 2f
                        onAmperesSliderChange(snapped)
                    },
                    valueRange = 1.0f..10.0f,
                    steps = 17,
                    colors = SliderDefaults.colors(
                        thumbColor = WarningAmber,
                        activeTrackColor = WarningAmber
                    ),
                    modifier = Modifier.testTag("current_amps_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Preset Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onPresetNormal,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("preset_normal_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SafeEmerald, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.btnNormal, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Button(
                onClick = onPresetDanger,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("preset_danger_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRed.copy(alpha = 0.22f),
                    contentColor = DangerRed
                ),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.65f))
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.btnDanger, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SMS Trigger Mode Selection
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isDark) Color.Black.copy(alpha = 0.25f) else Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = strings.smsModeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSmsModeChange(SmsSendMode.AUTO) }
                            .testTag("sms_mode_auto")
                    ) {
                        RadioButton(
                            selected = uiState.smsSendMode == SmsSendMode.AUTO,
                            onClick = { onSmsModeChange(SmsSendMode.AUTO) },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentTeal)
                        )
                        Text(strings.smsAuto, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSmsModeChange(SmsSendMode.MANUAL) }
                            .testTag("sms_mode_manual")
                    ) {
                        RadioButton(
                            selected = uiState.smsSendMode == SmsSendMode.MANUAL,
                            onClick = { onSmsModeChange(SmsSendMode.MANUAL) },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentTeal)
                        )
                        Text(strings.smsManual, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedVisibility(
                        visible = uiState.smsSendMode == SmsSendMode.MANUAL,
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedButton(
                            onClick = onManualTriggerAlert,
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("manual_trigger_alert_button"),
                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = DangerRed.copy(alpha = 0.16f),
                                contentColor = DangerRed
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.btnSendApiNow, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            launchNativeSms(
                                context = context,
                                phone = uiState.settings.phone,
                                body = alertMessageProvider(),
                                errorFallback = "No SMS app available on device"
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("open_native_sms_button"),
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = AccentCyan.copy(alpha = 0.14f),
                            contentColor = AccentCyan
                        )
                    ) {
                        Icon(Icons.Filled.Sms, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.btnNativeSms, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun SimActuatorsAndChartPanel(
    uiState: GasRescueUiState,
    strings: LocalizedStrings,
    isDark: Boolean,
    onToggleFan: () -> Unit,
    onToggleValve: () -> Unit,
    onTogglePower: () -> Unit,
    onToggleBuzzer: () -> Unit,
    onTriggerSmsCard: () -> Unit,
    onContinuousLeakChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val fanRotation = if (uiState.isFanActive) {
        val fanTransition = rememberInfiniteTransition(label = "fanSpin")
        val angle by fanTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 420, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "fanAngle"
        )
        angle
    } else {
        0f
    }

    FuturisticGlassCard(isDarkMode = isDark, modifier = modifier) {
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
                Icon(Icons.Filled.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(strings.actuatorsTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
            }
            Text(strings.actuatorsSub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SimActuatorCard(
                    title = strings.actFan,
                    statusText = if (uiState.isFanActive) strings.fanOn else strings.fanOff,
                    relayBadge = if (uiState.isFanActive) "Relay #1 ON" else "Relay #1 OFF",
                    isActive = uiState.isFanActive,
                    activeColor = AccentTeal,
                    icon = Icons.Filled.Cyclone,
                    iconRotation = if (uiState.isFanActive) fanRotation else 0f,
                    isDark = isDark,
                    onClick = onToggleFan,
                    modifier = Modifier.weight(1f).testTag("actuator_fan_card")
                )
                SimActuatorCard(
                    title = strings.actValve,
                    statusText = if (uiState.isValveShutOff) strings.valveClosed else strings.valveOpen,
                    relayBadge = if (uiState.isValveShutOff) "SG90 Servo 90°" else "SG90 Servo 0°",
                    isActive = uiState.isValveShutOff,
                    activeColor = SafeEmerald,
                    inactiveAccent = WarningAmber,
                    icon = if (uiState.isValveShutOff) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    isDark = isDark,
                    onClick = onToggleValve,
                    modifier = Modifier.weight(1f).testTag("actuator_valve_card")
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SimActuatorCard(
                    title = strings.actPower,
                    statusText = if (uiState.isPowerCut) strings.powerCut else strings.powerOn,
                    relayBadge = if (uiState.isPowerCut) "Relay #2 OFF" else "Relay #2 ON",
                    isActive = uiState.isPowerCut,
                    activeColor = DangerRed,
                    inactiveAccent = SafeEmerald,
                    icon = if (uiState.isPowerCut) Icons.Filled.PowerOff else Icons.Filled.Power,
                    isDark = isDark,
                    onClick = onTogglePower,
                    modifier = Modifier.weight(1f).testTag("actuator_power_card")
                )
                SimActuatorCard(
                    title = strings.actBuzzer,
                    statusText = if (uiState.isBuzzerActive) strings.buzzerOn else strings.buzzerOff,
                    relayBadge = "Pin D8 Sound",
                    isActive = uiState.isBuzzerActive,
                    activeColor = DangerRed,
                    icon = if (uiState.isBuzzerActive) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    isDark = isDark,
                    onClick = onToggleBuzzer,
                    modifier = Modifier.weight(1f).testTag("actuator_buzzer_card")
                )
            }

            SimActuatorCard(
                title = strings.actSms,
                statusText = if (uiState.isEmergency) strings.smsSent else strings.smsReady,
                relayBadge = "SIM800L & WhatsApp • ${uiState.settings.phone}",
                isActive = uiState.isEmergency,
                activeColor = AccentCyan,
                icon = if (uiState.isEmergency) Icons.AutoMirrored.Filled.Send else Icons.Filled.Sms,
                isDark = isDark,
                onClick = onTriggerSmsCard,
                modifier = Modifier.fillMaxWidth().testTag("actuator_sms_card")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                Text(strings.chartTitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onContinuousLeakChange(!uiState.continuousLeak) }
                    .padding(horizontal = 4.dp)
                    .testTag("continuous_leak_checkbox")
            ) {
                Checkbox(
                    checked = uiState.continuousLeak,
                    onCheckedChange = onContinuousLeakChange,
                    colors = CheckboxDefaults.colors(checkedColor = DangerRed)
                )
                Text(strings.chkLeak, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 2, modifier = Modifier.width(126.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        GasLiveChart(points = uiState.chartHistory, isDarkMode = isDark)
    }
}

@Composable
private fun SimActuatorCard(
    title: String,
    statusText: String,
    relayBadge: String,
    isActive: Boolean,
    activeColor: Color,
    icon: ImageVector,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    inactiveAccent: Color? = null,
    iconRotation: Float = 0f
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) activeColor.copy(alpha = 0.18f) else if (isDark) Color.Black.copy(alpha = 0.28f) else Color(0xFFF1F5F9),
        label = "actuatorBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) activeColor.copy(alpha = 0.60f) else MaterialTheme.colorScheme.outline,
        label = "actuatorBorder"
    )
    val contentAccent = if (isActive) activeColor else (inactiveAccent ?: MaterialTheme.colorScheme.onSurfaceVariant)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .heightIn(min = 92.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Icon(icon, contentDescription = title, tint = contentAccent, modifier = Modifier.size(22.dp).rotate(iconRotation))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(statusText, fontSize = 11.sp, fontWeight = if (isActive || inactiveAccent != null) FontWeight.Bold else FontWeight.Medium, color = contentAccent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Surface(shape = RoundedCornerShape(6.dp), color = if (isActive) activeColor.copy(alpha = 0.24f) else Color.Black.copy(alpha = 0.25f)) {
                    Text(
                        text = relayBadge,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) activeColor else (inactiveAccent ?: Color(0xFF94A3B8)),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

