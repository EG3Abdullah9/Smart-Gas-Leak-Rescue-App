package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PiezoBuzzerPlayer {

    private var beepJob: Job? = null

    fun setBeeping(active: Boolean, scope: CoroutineScope) {
        if (active) {
            if (beepJob?.isActive == true) return
            beepJob = scope.launch(Dispatchers.Default) {
                while (isActive) {
                    playSquarePulse()
                    delay(550L)
                }
            }
        } else {
            stop()
        }
    }

    fun stop() {
        beepJob?.cancel()
        beepJob = null
    }

    private fun playSquarePulse() {
        val sampleRate = 22050
        val durationMs = 180
        val numSamples = (sampleRate * durationMs) / 1000
        val freqHz = 2400.0
        val samples = ShortArray(numSamples)
        val periodSamples = sampleRate / freqHz
        val amplitude = (Short.MAX_VALUE * 0.14).toInt()

        for (i in 0 until numSamples) {
            val phase = (i % periodSamples) / periodSamples
            // Gentle fade-out on last 25ms to prevent speaker click
            val fadeFactor = if (i > numSamples * 0.85) {
                1.0 - (i - numSamples * 0.85) / (numSamples * 0.15)
            } else {
                1.0
            }
            val raw = if (phase < 0.5) amplitude else -amplitude
            samples[i] = (raw * fadeFactor).toInt().toShort()
        }

        val bufferBytes = samples.size * 2
        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferBytes)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
        }.getOrNull() ?: return

        try {
            track.write(samples, 0, samples.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 20L)
            track.stop()
        } catch (_: Throwable) {
        } finally {
            runCatching { track.release() }
        }
    }
}
