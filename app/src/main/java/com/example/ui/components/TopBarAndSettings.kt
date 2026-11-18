package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.AlertProvider
import com.example.i18n.AppLanguage
import com.example.i18n.LocalizedStrings
import com.example.ui.GasRescueUiState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.CardDarkGlass
import com.example.ui.theme.CardLight
import com.example.ui.theme.DangerRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.WarningAmber

@Composable
fun FuturisticGlassCard(
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    borderColor: Color = if (isDarkMode) Color.White.copy(alpha = 0.10f) else Color(0xFF0F172A).copy(alpha = 0.10f),
    containerColor: Color = if (isDarkMode) CardDarkGlass else CardLight.copy(alpha = 0.94f),
    cornerRadius: Dp = 18.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (isDarkMode) 2.dp else 4.dp,
        shadowElevation = if (isDarkMode) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun TopControlHeader(
    uiState: GasRescueUiState,
    strings: LocalizedStrings,
    onToggleMute: () -> Unit,
    onUsbClick: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = uiState.settings.isDarkMode
    val pulseAlpha = if (uiState.isEmergency) {
        val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
        val animated by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(700),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
        animated
    } else {
        1f
    }

    val statusColor by animateColorAsState(
        targetValue = if (uiState.isEmergency) DangerRed else SafeEmerald,
        label = "headerStatusColor"
    )

    FuturisticGlassCard(
        isDarkMode = isDark,
        modifier = modifier.fillMaxWidth(),
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = strings.headerTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, AccentTeal.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.headerTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = CircleShape,
                            color = AccentTeal.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = "v3.5 Live",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp,
                                color = if (isDark) AccentTeal else Color(0xFF007F70),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = strings.headerSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Live Status Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusColor.copy(alpha = if (uiState.isEmergency) 0.20f else 0.12f),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f)),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .alpha(if (uiState.isEmergency) pulseAlpha else 1f)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = if (uiState.isEmergency) strings.statusDangerTop else strings.statusSafeTop,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Mute / Unmute Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFF0F172A).copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .testTag("mute_audio_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isAudioMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Toggle Buzzer Audio",
                            tint = if (uiState.isAudioMuted) DangerRed else AccentCyan
                        )
                    }
                }

                // USB Arduino Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (uiState.isDeviceConnected) {
                        SafeEmerald.copy(alpha = 0.16f)
                    } else if (isDark) {
                        Color.White.copy(alpha = 0.06f)
                    } else {
                        Color(0xFF0F172A).copy(alpha = 0.06f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (uiState.isDeviceConnected) SafeEmerald.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onUsbClick)
                        .testTag("usb_arduino_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Usb,
                            contentDescription = strings.btnUsb,
                            tint = if (uiState.isDeviceConnected) SafeEmerald else WarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = uiState.connectedDeviceLabel ?: strings.btnUsb,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Settings Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFF0F172A).copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = strings.settingsTitle,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    uiState: GasRescueUiState,
    strings: LocalizedStrings,
    onDismiss: () -> Unit,
    onSaveAndClose: (AlertProvider, String, String, String, String) -> Unit,
    onTestRealMessage: (AlertProvider, String, String, String, String) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onToggleTheme: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onRateSystem: (Int) -> Unit
) {
    val isDark = uiState.settings.isDarkMode
    var selectedProvider by remember(uiState.settings.provider) { mutableStateOf(uiState.settings.provider) }
    var ntfyTopic by remember(uiState.settings.ntfyTopic) { mutableStateOf(uiState.settings.ntfyTopic) }
    var tgToken by remember(uiState.settings.tgToken) { mutableStateOf(uiState.settings.tgToken) }
    var tgChatId by remember(uiState.settings.tgChatId) { mutableStateOf(uiState.settings.tgChatId) }
    var webhookUrl by remember(uiState.settings.webhookUrl) { mutableStateOf(uiState.settings.webhookUrl) }
    var providerMenuExpanded by remember { mutableStateOf(false) }
    var langMenuExpanded by remember { mutableStateOf(false) }
    var currentRating by remember(uiState.settings.rating) { mutableIntStateOf(uiState.settings.rating) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) Color(0xFF0E1730) else Color.White,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = strings.settingsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("close_settings_button")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close Settings")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Real Messaging Config Section
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color.Black.copy(alpha = 0.28f) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.realMsgConfig,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = CircleShape,
                                color = if (uiState.isOnlineChannelConfigured) SafeEmerald.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (uiState.isOnlineChannelConfigured) "API Ready" else "Local Mode",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = if (uiState.isOnlineChannelConfigured) SafeEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Provider Dropdown
                        Text(
                            text = strings.msgProviderLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        val providerLabel = when (selectedProvider) {
                            AlertProvider.NTFY -> strings.providerNtfyLabel
                            AlertProvider.TELEGRAM -> strings.providerTelegramLabel
                            AlertProvider.WEBHOOK -> strings.providerWebhookLabel
                            AlertProvider.LOCAL -> strings.providerLocalLabel
                        }
                        ExposedDropdownMenuBox(
                            expanded = providerMenuExpanded,
                            onExpandedChange = { providerMenuExpanded = !providerMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = providerLabel,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerMenuExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("provider_dropdown"),
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                            ExposedDropdownMenu(
                                expanded = providerMenuExpanded,
                                onDismissRequest = { providerMenuExpanded = false }
                            ) {
                                listOf(
                                    AlertProvider.NTFY to strings.providerNtfyLabel,
                                    AlertProvider.TELEGRAM to strings.providerTelegramLabel,
                                    AlertProvider.WEBHOOK to strings.providerWebhookLabel,
                                    AlertProvider.LOCAL to strings.providerLocalLabel
                                ).forEach { (provider, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label, style = MaterialTheme.typography.bodySmall) },
                                        onClick = {
                                            selectedProvider = provider
                                            providerMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        when (selectedProvider) {
                            AlertProvider.NTFY -> {
                                OutlinedTextField(
                                    value = ntfyTopic,
                                    onValueChange = { ntfyTopic = it },
                                    label = { Text(strings.ntfyTopicHint, fontSize = 11.sp) },
                                    placeholder = { Text("gas-rescue-alert-2026") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ntfy_topic_input")
                                )
                            }
                            AlertProvider.TELEGRAM -> {
                                OutlinedTextField(
                                    value = tgToken,
                                    onValueChange = { tgToken = it },
                                    label = { Text(strings.tgTokenHint, fontSize = 11.sp) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("telegram_token_input")
                                )
                                OutlinedTextField(
                                    value = tgChatId,
                                    onValueChange = { tgChatId = it },
                                    label = { Text(strings.tgChatIdHint, fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("telegram_chat_id_input")
                                )
                            }
                            AlertProvider.WEBHOOK -> {
                                OutlinedTextField(
                                    value = webhookUrl,
                                    onValueChange = { webhookUrl = it },
                                    label = { Text(strings.webhookUrlHint, fontSize = 11.sp) },
                                    placeholder = { Text("https://discord.com/api/webhooks/...") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("webhook_url_input")
                                )
                            }
                            AlertProvider.LOCAL -> Unit
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onTestRealMessage(selectedProvider, ntfyTopic, tgToken, tgChatId, webhookUrl)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("test_real_message_button"),
                                border = BorderStroke(1.dp, AccentCyan),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.btnTestSend,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = onRequestNotificationPermission,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("request_notification_permission_button")
                            ) {
                                Icon(
                                    Icons.Filled.Notifications,
                                    contentDescription = strings.btnEnableNotif,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Theme Toggle Row
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color.Black.copy(alpha = 0.22f) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.themeLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("toggle_theme_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isDark) strings.themeToLight else strings.themeToDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Language Dropdown
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color.Black.copy(alpha = 0.22f) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.langLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        ExposedDropdownMenuBox(
                            expanded = langMenuExpanded,
                            onExpandedChange = { langMenuExpanded = !langMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = uiState.settings.language.displayName,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langMenuExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("language_dropdown"),
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                            ExposedDropdownMenu(
                                expanded = langMenuExpanded,
                                onDismissRequest = { langMenuExpanded = false }
                            ) {
                                AppLanguage.entries.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang.displayName) },
                                        onClick = {
                                            langMenuExpanded = false
                                            onSelectLanguage(lang)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive Star Rating
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color.Black.copy(alpha = 0.22f) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.rateLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            for (star in 1..5) {
                                IconButton(
                                    onClick = {
                                        currentRating = star
                                        onRateSystem(star)
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("star_rating_$star")
                                ) {
                                    Icon(
                                        imageVector = if (star <= currentRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Rate $star stars",
                                        tint = if (star <= currentRating) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Save & Close
                Button(
                    onClick = {
                        onSaveAndClose(selectedProvider, ntfyTopic, tgToken, tgChatId, webhookUrl)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_settings_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentTeal,
                        contentColor = Color(0xFF040914)
                    )
                ) {
                    Text(
                        text = strings.btnSaveClose,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
