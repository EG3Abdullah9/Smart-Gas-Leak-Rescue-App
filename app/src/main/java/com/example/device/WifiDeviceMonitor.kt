package com.example.device

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WifiDeviceMonitor {

    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()

    private var pollJob: Job? = null

    fun connectAndPoll(
        url: String,
        scope: CoroutineScope,
        onConnected: (String) -> Unit,
        onPpmReceived: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        stop()
        val normalized = url.trim().let {
            if (it.startsWith("http://") || it.startsWith("https://")) it else "http://$it"
        }

        pollJob = scope.launch(Dispatchers.IO) {
            var announced = false
            while (isActive) {
                try {
                    val req = Request.Builder().url(normalized).get().build()
                    client.newCall(req).execute().use { resp ->
                        if (!resp.isSuccessful) {
                            onError("Wi-Fi HTTP ${resp.code} from $normalized")
                            stop()
                            return@launch
                        }
                        val body = resp.body?.string().orEmpty().trim()
                        val ppm = parsePpmPayload(body)
                        if (!announced) {
                            announced = true
                            onConnected(normalized)
                        }
                        if (ppm != null) {
                            onPpmReceived(ppm.coerceIn(0, 1500))
                        }
                    }
                } catch (e: Exception) {
                    onError("Wi-Fi unreachable ($normalized): ${e.localizedMessage ?: "Timeout"}")
                    stop()
                    return@launch
                }
                delay(2000L)
            }
        }
    }

    private fun parsePpmPayload(raw: String): Int? {
        runCatching {
            val json = JSONObject(raw)
            for (key in listOf("ppm", "gas", "mq2", "value")) {
                if (json.has(key)) return json.optInt(key, -1).takeIf { it >= 0 }
            }
        }
        return Regex("\\d+").find(raw)?.value?.toIntOrNull()
    }

    fun stop() {
        pollJob?.cancel()
        pollJob = null
    }
}
