package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gas_rescue_settings")

enum class AlertProvider(val id: String) {
    NTFY("ntfy"),
    TELEGRAM("telegram"),
    WEBHOOK("webhook"),
    LOCAL("local");

    companion object {
        fun fromId(id: String): AlertProvider =
            entries.firstOrNull { it.id == id } ?: NTFY
    }
}

data class PersistedSettings(
    val isDarkMode: Boolean = true,
    val language: AppLanguage = AppLanguage.AR,
    val phone: String = "+201098765432",
    val provider: AlertProvider = AlertProvider.NTFY,
    val ntfyTopic: String = "",
    val tgToken: String = "",
    val tgChatId: String = "",
    val webhookUrl: String = "",
    val wifiDeviceUrl: String = "http://192.168.4.1/gas",
    val rating: Int = 5
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("sgr_dark_mode")
        val LANG = stringPreferencesKey("sgr_lang")
        val PHONE = stringPreferencesKey("sgr_phone")
        val PROVIDER = stringPreferencesKey("sgr_provider")
        val NTFY_TOPIC = stringPreferencesKey("sgr_ntfy_topic")
        val TG_TOKEN = stringPreferencesKey("sgr_tg_token")
        val TG_CHAT_ID = stringPreferencesKey("sgr_tg_chat_id")
        val WEBHOOK_URL = stringPreferencesKey("sgr_webhook_url")
        val WIFI_DEVICE_URL = stringPreferencesKey("sgr_wifi_device_url")
        val RATING = intPreferencesKey("sgr_rating")
    }

    val settingsFlow: Flow<PersistedSettings> = context.dataStore.data.map { prefs ->
        PersistedSettings(
            isDarkMode = prefs[Keys.DARK_MODE] ?: true,
            language = AppLanguage.fromCode(prefs[Keys.LANG] ?: "ar"),
            phone = prefs[Keys.PHONE] ?: "+201098765432",
            provider = AlertProvider.fromId(prefs[Keys.PROVIDER] ?: "ntfy"),
            ntfyTopic = prefs[Keys.NTFY_TOPIC] ?: "",
            tgToken = prefs[Keys.TG_TOKEN] ?: "",
            tgChatId = prefs[Keys.TG_CHAT_ID] ?: "",
            webhookUrl = prefs[Keys.WEBHOOK_URL] ?: "",
            wifiDeviceUrl = prefs[Keys.WIFI_DEVICE_URL] ?: "http://192.168.4.1/gas",
            rating = (prefs[Keys.RATING] ?: 5).coerceIn(1, 5)
        )
    }

    suspend fun setDarkMode(dark: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = dark }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANG] = language.code }
    }

    suspend fun setPhone(phone: String) {
        context.dataStore.edit { it[Keys.PHONE] = phone }
    }

    suspend fun setWifiDeviceUrl(url: String) {
        context.dataStore.edit { it[Keys.WIFI_DEVICE_URL] = url.trim() }
    }

    suspend fun setRating(rating: Int) {
        context.dataStore.edit { it[Keys.RATING] = rating.coerceIn(1, 5) }
    }

    suspend fun saveMessagingConfig(
        provider: AlertProvider,
        ntfyTopic: String,
        tgToken: String,
        tgChatId: String,
        webhookUrl: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PROVIDER] = provider.id
            prefs[Keys.NTFY_TOPIC] = ntfyTopic.trim()
            prefs[Keys.TG_TOKEN] = tgToken.trim()
            prefs[Keys.TG_CHAT_ID] = tgChatId.trim()
            prefs[Keys.WEBHOOK_URL] = webhookUrl.trim()
        }
    }
}
