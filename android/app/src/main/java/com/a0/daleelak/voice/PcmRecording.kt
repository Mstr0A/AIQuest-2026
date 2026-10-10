package com.a0.daleelak.voice

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

data class RecordedClip(val wav: ByteArray, val hasSignal: Boolean)

/** Captures at most 30 seconds in memory, without an installed recognition service. */
class PcmRecording {
    @Volatile private var stopped = false

    fun stop() {
        stopped = true
    }

    @SuppressLint("MissingPermission") // The screen requests RECORD_AUDIO before starting capture.
    suspend fun capture(): RecordedClip = withContext(Dispatchers.IO) {
        val minimum = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (minimum <= 0) throw IOException("الميكروفون غير متاح. تقدر تكتب رسالتك.")
        val recorder = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, max(minimum, 6400))
        val output = ByteArrayOutputStream()
        var signalSamples = 0
        val started = SystemClock.elapsedRealtime()
        try {
            if (recorder.state != AudioRecord.STATE_INITIALIZED) throw IOException("تعذر فتح الميكروفون.")
            if (!stopped) recorder.startRecording()
            if (!stopped && recorder.recordingState != AudioRecord.RECORDSTATE_RECORDING) throw IOException("تعذر بدء التسجيل.")
            if (!stopped) Log.i(OpenRouterSpeech.TAG, "Microphone recording started")
            val samples = ShortArray(1600)
            while (!stopped && output.size() < MAX_PCM_BYTES && SystemClock.elapsedRealtime() - started < 30_000) {
                currentCoroutineContext().ensureActive()
                // Keep stop/cancellation responsive without calling native stop on the UI thread.
                val count = recorder.read(samples, 0, samples.size, AudioRecord.READ_NON_BLOCKING)
                if (count < 0) {
                    if (stopped) break
                    throw IOException("انقطع تسجيل الميكروفون. جرّب مجدداً.")
                }
                if (count == 0) { delay(10); continue }
                val usable = count.coerceAtMost((MAX_PCM_BYTES - output.size()) / 2)
                for (index in 0 until usable) {
                    val sample = samples[index].toInt()
                    if (kotlin.math.abs(sample) > 180) signalSamples++
                    output.write(sample and 0xff)
                    output.write((sample shr 8) and 0xff)
                }
            }
            Log.i(OpenRouterSpeech.TAG, "Microphone recording complete bytes=${output.size()} elapsed_ms=${SystemClock.elapsedRealtime() - started}")
            // This rejects near-silence, not every non-speech sound; users still review the transcript.
            RecordedClip(wav(output.toByteArray()), signalSamples >= SAMPLE_RATE / 10)
        } finally {
            stopped = true
            runCatching { recorder.stop() }
            recorder.release()
        }
    }

    companion object {
        const val SAMPLE_RATE = 16000
        const val MAX_PCM_BYTES = SAMPLE_RATE * 2 * 30

        fun wav(pcm: ByteArray): ByteArray {
            require(pcm.size % 2 == 0)
            return ByteBuffer.allocate(44 + pcm.size).order(ByteOrder.LITTLE_ENDIAN)
                .put("RIFF".toByteArray(Charsets.US_ASCII)).putInt(36 + pcm.size)
                .put("WAVEfmt ".toByteArray(Charsets.US_ASCII)).putInt(16)
                .putShort(1).putShort(1).putInt(SAMPLE_RATE).putInt(SAMPLE_RATE * 2)
                .putShort(2).putShort(16).put("data".toByteArray(Charsets.US_ASCII))
                .putInt(pcm.size).put(pcm).array()
        }
    }
}
