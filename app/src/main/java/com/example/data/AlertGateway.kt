package com.example.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.i18n.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class AlertDeliveryResult(
    val deliveredOnline: Boolean,
    val statusNote: String
)

class AlertGateway(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "gas_rescue_alerts"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Gas Rescue Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical gas leak emergency notifications and test alerts"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showLocalNotification(title: String, body: String) {
        ensureNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_shield)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }

    suspend fun sendAlert(
        settings: PersistedSettings,
        messageText: String,
        gasPpm: Int,
        isEmergency: Boolean
    ): AlertDeliveryResult = withContext(Dispatchers.IO) {
        showLocalNotification(
            title = if (isEmergency) "🚨 Smart Gas Rescue Alert" else "Smart Gas Rescue",
            body = messageText
        )

        val lang = settings.language
        when (settings.provider) {
            AlertProvider.NTFY -> {
                val topic = settings.ntfyTopic.trim()
                if (topic.isEmpty()) {
                    val note = if (lang == AppLanguage.AR) {
                        "لم يتم إدخال اسم Topic في الإعدادات (ntfy.sh)."
                    } else {
                        "Set an ntfy.sh Topic in Settings for real push delivery."
                    }
                    AlertDeliveryResult(false, note)
                } else {
                    try {
                        val encodedTopic = URLEncoder.encode(topic, "UTF-8").replace("+", "%20")
                        val request = Request.Builder()
                            .url("https://ntfy.sh/$encodedTopic")
                            .post(messageText.toRequestBody("text/plain; charset=utf-8".toMediaType()))
                            .header("Title", if (isEmergency) "GAS LEAK EMERGENCY" else "Gas Rescue Alert")
                            .header("Priority", if (isEmergency) "5" else "3")
                            .build()
                        httpClient.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                AlertDeliveryResult(true, "✓ Real Push sent via ntfy.sh/$topic")
                            } else {
                                AlertDeliveryResult(false, "ntfy.sh HTTP ${response.code}")
                            }
                        }
                    } catch (e: Exception) {
                        AlertDeliveryResult(false, "Network error (ntfy.sh): ${e.localizedMessage ?: "Unknown"}")
                    }
                }
            }

            AlertProvider.TELEGRAM -> {
                val token = settings.tgToken.trim()
                val chatId = settings.tgChatId.trim()
                if (token.isEmpty() || chatId.isEmpty()) {
                    val note = if (lang == AppLanguage.AR) {
                        "يرجى إدخال Bot Token و Chat ID في الإعدادات."
                    } else {
                        "Enter Telegram Bot Token & Chat ID in Settings."
                    }
                    AlertDeliveryResult(false, note)
                } else {
                    try {
                        val payload = JSONObject().apply {
                            put("chat_id", chatId)
                            put("text", messageText)
                        }
                        val request = Request.Builder()
                            .url("https://api.telegram.org/bot$token/sendMessage")
                            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                            .build()
                        httpClient.newCall(request).execute().use { response ->
                            val rawBody = response.body?.string().orEmpty()
                            val json = runCatching { JSONObject(rawBody) }.getOrNull()
                            if (response.isSuccessful && json?.optBoolean("ok") == true) {
                                AlertDeliveryResult(true, "✓ Real Telegram message delivered to Chat $chatId")
                            } else {
                                val desc = json?.optString("description").orEmpty().ifEmpty { "HTTP ${response.code}" }
                                AlertDeliveryResult(false, "Telegram Error: $desc")
                            }
                        }
                    } catch (e: Exception) {
                        AlertDeliveryResult(false, "Telegram Network Error: ${e.localizedMessage ?: "Unknown"}")
                    }
                }
            }

            AlertProvider.WEBHOOK -> {
                val url = settings.webhookUrl.trim()
                if (url.isEmpty()) {
                    val note = if (lang == AppLanguage.AR) {
                        "يرجى إدخال رابط Webhook في الإعدادات."
                    } else {
                        "Enter a Webhook URL in Settings."
                    }
                    AlertDeliveryResult(false, note)
                } else {
                    try {
                        val payload = JSONObject().apply {
                            put("content", messageText)
                            put("text", messageText)
                            put("gas_ppm", gasPpm)
                            put("phone", settings.phone)
                            put("emergency", isEmergency)
                        }
                        val request = Request.Builder()
                            .url(url)
                            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                            .build()
                        httpClient.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                AlertDeliveryResult(true, "✓ Real Webhook delivered (HTTP ${response.code})")
                            } else {
                                AlertDeliveryResult(false, "Webhook HTTP ${response.code}")
                            }
                        }
                    } catch (e: Exception) {
                        AlertDeliveryResult(false, "Webhook Error: ${e.localizedMessage ?: "Unknown"}")
                    }
                }
            }

            AlertProvider.LOCAL -> {
                val note = if (lang == AppLanguage.AR) {
                    "تم التنبيه محلياً (استخدم زر SMS/WhatsApp أو فعّل ntfy/Telegram في الإعدادات)."
                } else {
                    "Local alert fired (Configure ntfy/Telegram in Settings or use SMS/WhatsApp button)."
                }
                AlertDeliveryResult(false, note)
            }
        }
    }
}
