package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Cyclone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.i18n.AppLanguage
import com.example.i18n.LocalizedStrings
import com.example.ui.GasRescueUiState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.CardDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PanelDark
import com.example.ui.theme.SafeEmerald

@Composable
fun MobileRemoteControlPanel(
    uiState: GasRescueUiState,
    strings: LocalizedStrings,
    alertMessageProvider: () -> String,
    onSavePhone: (String) -> Unit,
    onSendCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAr = uiState.settings.language == AppLanguage.AR
    var phoneInput by remember(uiState.settings.phone) { mutableStateOf(uiState.settings.phone) }
    var commandInput by remember { mutableStateOf("") }
    val chatScrollState = rememberScrollState()

    LaunchedEffect(uiState.appMessages.size) {
        if (chatScrollState.maxValue > 0) {
            chatScrollState.animateScrollTo(chatScrollState.maxValue)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color(0xFF020617),
        border = BorderStroke(2.5.dp, Color(0xFF334155)),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Mini App Header
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PanelDark,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Smart Gas App Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Column {
                            Text(
                                text = "Smart Gas App",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (uiState.isEmergency) "${uiState.gasPPM} PPM • DANGER!" else "${uiState.gasPPM} PPM • Safe",
                                fontFamily = JetBrainsMonoFontFamily,
                                color = if (uiState.isEmergency) DangerRed else Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isEmergency) DangerRed else SafeEmerald)
                    )
                }
            }

            // Emergency Phone Number
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardDark,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = strings.appPhoneLabel,
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                color = AccentCyan
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("emergency_phone_input")
                        )
                        Button(
                            onClick = { onSavePhone(phoneInput) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentTeal,
                                contentColor = Color(0xFF040914)
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("save_phone_button")
                        ) {
                            Text(
                                text = strings.btnSave,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    AnimatedVisibility(visible = uiState.showPhoneSavedBanner) {
                        Text(
                            text = strings.phoneSavedMsg,
                            color = SafeEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Real External Send Buttons (SMS & WhatsApp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        launchNativeSms(
                            context,
                            uiState.settings.phone,
                            alertMessageProvider(),
                            "No SMS app available on device"
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("app_real_sms_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.45f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = AccentCyan.copy(alpha = 0.16f),
                        contentColor = AccentCyan
                    )
                ) {
                    Icon(Icons.Filled.Sms, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = strings.appSendSms,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = {
                        launchWhatsApp(
                            context,
                            uiState.settings.phone,
                            alertMessageProvider(),
                            "WhatsApp / Browser not available"
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("app_real_whatsapp_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SafeEmerald.copy(alpha = 0.45f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SafeEmerald.copy(alpha = 0.16f),
                        contentColor = SafeEmerald
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = strings.appSendWa,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Interactive SMS Chat Stream
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Filled.Forum, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(14.dp))
                            Text(strings.appMsgs, color = Color(0xFF94A3B8), fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp)
                        }
                        Text(
                            text = uiState.settings.phone,
                            color = AccentTeal,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.10f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .verticalScroll(chatScrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.appMessages.forEach { msg ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (msg.isUser) AccentTeal.copy(alpha = 0.18f) else CardDark,
                                border = BorderStroke(
                                    1.dp,
                                    if (msg.isUser) AccentTeal.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.10f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = if (msg.isUser) 16.dp else 0.dp,
                                        end = if (msg.isUser) 0.dp else 16.dp
                                    )
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "${msg.sender}:",
                                        color = Color(0xFF94A3B8),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = msg.text,
                                        color = if (msg.isUser) AccentTeal else Color(0xFFE2E8F0),
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.10f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commandInput,
                            onValueChange = { commandInput = it },
                            placeholder = {
                                Text(strings.cmdPlaceholder, fontSize = 11.sp, color = Color(0xFF64748B))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (commandInput.isNotBlank()) {
                                        onSendCommand(commandInput)
                                        commandInput = ""
                                    }
                                }
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentTeal,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
                                focusedContainerColor = Color(0xFF020617),
                                unfocusedContainerColor = Color(0xFF020617)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("app_command_input")
                        )

                        Button(
                            onClick = {
                                if (commandInput.isNotBlank()) {
                                    onSendCommand(commandInput)
                                    commandInput = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentCyan,
                                contentColor = Color(0xFF040914)
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .height(48.dp)
                                .testTag("send_app_command_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Command",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 4 Quick Command Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SimQuickCmdButton(
                    label = strings.qFan,
                    icon = Icons.Filled.Cyclone,
                    tint = AccentTeal,
                    onClick = { onSendCommand(if (isAr) "تشغيل الشفاط" else "toggle fan") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_cmd_fan")
                )
                SimQuickCmdButton(
                    label = strings.qPower,
                    icon = Icons.Filled.PowerSettingsNew,
                    tint = DangerRed,
                    onClick = { onSendCommand(if (isAr) "فصل الكهرباء" else "toggle power") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_cmd_power")
                )
                SimQuickCmdButton(
                    label = strings.qStatus,
                    icon = Icons.Filled.Favorite,
                    tint = AccentCyan,
                    onClick = { onSendCommand(if (isAr) "فحص الحالة" else "status") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_cmd_status")
                )
                SimQuickCmdButton(
                    label = strings.qReset,
                    icon = Icons.Filled.Refresh,
                    tint = SafeEmerald,
                    onClick = { onSendCommand(if (isAr) "تصفير" else "reset") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_cmd_reset")
                )
            }
        }
    }
}

@Composable
private fun SimQuickCmdButton(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CardDark,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        onClick = onClick,
        modifier = modifier.height(54.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                color = Color(0xFFE2E8F0),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
