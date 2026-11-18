package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cyclone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.WarningAmber

@Composable
fun EmergencyDangerResponseCard(
    uiState: GasRescueUiState,
    strings: LocalizedStrings,
    onActivateFan: () -> Unit,
    onToggleValve: () -> Unit,
    onTriggerAlarm: () -> Unit,
    onOpenSmsWhatsappDispatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = uiState.isEmergency,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val isDark = uiState.settings.isDarkMode
        val pulseAlpha = if (uiState.isEmergency) {
            val transition = rememberInfiniteTransition(label = "dangerCardPulse")
            val alpha by transition.animateFloat(
                initialValue = 0.55f,
                targetValue = 0.98f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 650, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dangerBorderAlpha"
            )
            alpha
        } else {
            0.75f
        }

        val fanRotation = if (uiState.isFanActive) {
            val spin = rememberInfiniteTransition(label = "dangerFanSpin")
            val angle by spin.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 420, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "dangerFanAngle"
            )
            angle
        } else {
            0f
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = if (isDark) Color(0xFF1F0A10) else Color(0xFFFFF1F2),
            border = BorderStroke(2.dp, DangerRed.copy(alpha = pulseAlpha)),
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("emergency_danger_response_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                        Surface(
                            shape = CircleShape,
                            color = DangerRed.copy(alpha = 0.22f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = strings.dangerPanelTitle,
                                tint = DangerRed,
                                modifier = Modifier.padding(7.dp)
                            )
                        }
                        Column {
                            Text(
                                text = strings.dangerPanelTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = DangerRed
                            )
                            Text(
                                text = strings.dangerPanelSub,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DangerRed
                    ) {
                        Text(
                            text = "${uiState.gasPPM} PPM",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }

                // 2x2 Grid of 4 Prominent Emergency Response Buttons
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DangerActionTileButton(
                            title = strings.btnDangerFan,
                            statusBadge = if (uiState.isFanActive) strings.fanOn else strings.fanOff,
                            icon = Icons.Filled.Cyclone,
                            iconRotation = fanRotation,
                            isActive = uiState.isFanActive,
                            activeColor = AccentTeal,
                            inactiveColor = WarningAmber,
                            isDark = isDark,
                            onClick = onActivateFan,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("danger_btn_fan")
                        )
                        DangerActionTileButton(
                            title = strings.btnDangerValve,
                            statusBadge = if (uiState.isValveShutOff) strings.valveClosed else strings.valveOpen,
                            icon = if (uiState.isValveShutOff) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            isActive = uiState.isValveShutOff,
                            activeColor = SafeEmerald,
                            inactiveColor = DangerRed,
                            isDark = isDark,
                            onClick = onToggleValve,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("danger_btn_valve")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DangerActionTileButton(
                            title = strings.btnDangerAlarm,
                            statusBadge = if (uiState.isBuzzerActive) strings.buzzerOn else strings.buzzerOff,
                            icon = if (uiState.isBuzzerActive) {
                                Icons.Filled.NotificationsActive
                            } else {
                                Icons.AutoMirrored.Filled.VolumeUp
                            },
                            isActive = uiState.isBuzzerActive,
                            activeColor = DangerRed,
                            inactiveColor = WarningAmber,
                            isDark = isDark,
                            onClick = onTriggerAlarm,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("danger_btn_alarm")
                        )
                        DangerActionTileButton(
                            title = strings.btnDangerSmsWa,
                            statusBadge = uiState.settings.phone,
                            icon = Icons.AutoMirrored.Filled.Send,
                            isActive = true,
                            activeColor = AccentCyan,
                            inactiveColor = AccentCyan,
                            isDark = isDark,
                            onClick = onOpenSmsWhatsappDispatch,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("danger_btn_sms_wa")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DangerActionTileButton(
    title: String,
    statusBadge: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRotation: Float = 0f
) {
    val accent = if (isActive) activeColor else inactiveColor
    val bgColor by animateColorAsState(
        targetValue = if (isActive) {
            activeColor.copy(alpha = if (isDark) 0.24f else 0.16f)
        } else {
            if (isDark) Color.Black.copy(alpha = 0.34f) else Color.White
        },
        label = "dangerTileBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) activeColor else accent.copy(alpha = 0.55f),
        label = "dangerTileBorder"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .heightIn(min = 82.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(11.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.22f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accent,
                        modifier = Modifier
                            .padding(6.dp)
                            .rotate(iconRotation)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accent.copy(alpha = 0.20f)
                ) {
                    Text(
                        text = if (isActive) "ON" else "TAP",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusBadge,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PackedHardwareCommandPad(
    uiState: GasRescueUiState,
    strings: LocalizedStrings,
    onSelectGasPreset: (Int) -> Unit,
    onToggleFan: () -> Unit,
    onToggleValve: () -> Unit,
    onTogglePower: () -> Unit,
    onToggleBuzzer: () -> Unit,
    onFullRescueTest: () -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = uiState.settings.isDarkMode

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
                Icon(
                    imageVector = Icons.Filled.TouchApp,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(19.dp)
                )
                Column {
                    Text(
                        text = strings.commandPadTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = strings.commandPadSub,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(10.dp))

        // 6 Scenario Preset Buttons (3 x 2 Grid)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScenarioPresetChip(
                    label = strings.presetSafe120,
                    ppm = 120,
                    currentPpm = uiState.gasPPM,
                    accent = SafeEmerald,
                    isDark = isDark,
                    onClick = { onSelectGasPreset(120) },
                    modifier = Modifier.weight(1f).testTag("preset_120")
                )
                ScenarioPresetChip(
                    label = strings.presetMild220,
                    ppm = 220,
                    currentPpm = uiState.gasPPM,
                    accent = AccentTeal,
                    isDark = isDark,
                    onClick = { onSelectGasPreset(220) },
                    modifier = Modifier.weight(1f).testTag("preset_220")
                )
                ScenarioPresetChip(
                    label = strings.presetWarn320,
                    ppm = 320,
                    currentPpm = uiState.gasPPM,
                    accent = WarningAmber,
                    isDark = isDark,
                    onClick = { onSelectGasPreset(320) },
                    modifier = Modifier.weight(1f).testTag("preset_320")
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScenarioPresetChip(
                    label = strings.presetLeak450,
                    ppm = 450,
                    currentPpm = uiState.gasPPM,
                    accent = WarningAmber,
                    isDark = isDark,
                    onClick = { onSelectGasPreset(450) },
                    modifier = Modifier.weight(1f).testTag("preset_450")
                )
                ScenarioPresetChip(
                    label = strings.presetHigh650,
                    ppm = 650,
                    currentPpm = uiState.gasPPM,
                    accent = DangerRed,
                    isDark = isDark,
                    onClick = { onSelectGasPreset(650) },
                    modifier = Modifier.weight(1f).testTag("preset_650")
                )
                ScenarioPresetChip(
                    label = strings.presetCrit900,
                    ppm = 900,
                    currentPpm = uiState.gasPPM,
                    accent = DangerRed,
                    isDark = isDark,
                    onClick = { onSelectGasPreset(900) },
                    modifier = Modifier.weight(1f).testTag("preset_900")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6 Instant Hardware Action Buttons (3 x 2 Grid)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CommandPadActionButton(
                    label = strings.actFan,
                    icon = Icons.Filled.Cyclone,
                    isActive = uiState.isFanActive,
                    accent = AccentTeal,
                    isDark = isDark,
                    onClick = onToggleFan,
                    modifier = Modifier.weight(1f).testTag("pad_btn_fan")
                )
                CommandPadActionButton(
                    label = strings.actValve,
                    icon = if (uiState.isValveShutOff) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    isActive = uiState.isValveShutOff,
                    accent = SafeEmerald,
                    isDark = isDark,
                    onClick = onToggleValve,
                    modifier = Modifier.weight(1f).testTag("pad_btn_valve")
                )
                CommandPadActionButton(
                    label = strings.actPower,
                    icon = if (uiState.isPowerCut) Icons.Filled.PowerOff else Icons.Filled.Power,
                    isActive = uiState.isPowerCut,
                    accent = DangerRed,
                    isDark = isDark,
                    onClick = onTogglePower,
                    modifier = Modifier.weight(1f).testTag("pad_btn_power")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CommandPadActionButton(
                    label = strings.actBuzzer,
                    icon = if (uiState.isBuzzerActive) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    isActive = uiState.isBuzzerActive,
                    accent = WarningAmber,
                    isDark = isDark,
                    onClick = onToggleBuzzer,
                    modifier = Modifier.weight(1f).testTag("pad_btn_alarm")
                )
                CommandPadActionButton(
                    label = strings.btnFullRescue,
                    icon = Icons.Filled.Shield,
                    isActive = uiState.isEmergency && uiState.isFanActive && uiState.isValveShutOff,
                    accent = DangerRed,
                    isDark = isDark,
                    onClick = onFullRescueTest,
                    modifier = Modifier.weight(1f).testTag("pad_btn_full_rescue")
                )
                CommandPadActionButton(
                    label = strings.btnResetAll,
                    icon = Icons.Filled.RestartAlt,
                    isActive = false,
                    accent = AccentCyan,
                    isDark = isDark,
                    onClick = onResetAll,
                    modifier = Modifier.weight(1f).testTag("pad_btn_reset_all")
                )
            }
        }
    }
}

@Composable
private fun ScenarioPresetChip(
    label: String,
    ppm: Int,
    currentPpm: Int,
    accent: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = kotlin.math.abs(currentPpm - ppm) <= 15
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
        border = BorderStroke(
            width = if (selected) 1.6.dp else 1.dp,
            color = if (selected) accent else accent.copy(alpha = 0.42f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) accent.copy(alpha = 0.24f) else if (isDark) Color.Black.copy(alpha = 0.25f) else Color(0xFFF8FAFC),
            contentColor = accent
        ),
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CommandPadActionButton(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    accent: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) accent.copy(alpha = 0.22f) else if (isDark) Color.Black.copy(alpha = 0.26f) else Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, if (isActive) accent else MaterialTheme.colorScheme.outline),
        onClick = onClick,
        modifier = modifier.heightIn(min = 56.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isActive) accent else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmergencyDispatchDialog(
    phone: String,
    alertMessage: String,
    strings: LocalizedStrings,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                tint = DangerRed,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = strings.dispatchDialogTitle,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.dispatchDialogSub,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentCyan.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = phone,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = alertMessage,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Button(
                    onClick = {
                        launchNativeSms(
                            context = context,
                            phone = phone,
                            body = alertMessage,
                            errorFallback = "No SMS app available on device"
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCyan,
                        contentColor = Color(0xFF040914)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dispatch_sms_button")
                ) {
                    Icon(Icons.Filled.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.dispatchOpenSms, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        launchWhatsApp(
                            context = context,
                            phone = phone,
                            body = alertMessage,
                            errorFallback = "WhatsApp / Browser not available"
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafeEmerald,
                        contentColor = Color(0xFF040914)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dispatch_whatsapp_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.dispatchOpenWa, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("dispatch_close_button")
            ) {
                Text(strings.dispatchClose, fontWeight = FontWeight.Bold)
            }
        }
    )
}
