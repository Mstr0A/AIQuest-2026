package com.a0.daleelak.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Owns focus and a temporary playback file only while its parent coroutine is active. */
class ReplyAudioPlayer(private val context: Context) {
    suspend fun play(audio: ByteArray, onPlaying: () -> Unit) {
        val file = File(context.cacheDir, "daleelak-reply-${UUID.randomUUID()}.mp3")
        try {
            withContext(Dispatchers.IO) { file.writeBytes(audio) }
            withContext(Dispatchers.Main.immediate) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
                    val player = MediaPlayer()
                    var cleaned = false
                    lateinit var focus: AudioFocusRequest
                    fun cleanup() {
                        if (cleaned) return
                        cleaned = true
                        player.release()
                        manager.abandonAudioFocusRequest(focus)
                    }
                    focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setAudioAttributes(attributes).setOnAudioFocusChangeListener { change ->
                            if (change < 0) {
                                continuation.cancel(CancellationException("Audio focus changed"))
                            }
                        }.build()
                    continuation.invokeOnCancellation { Handler(Looper.getMainLooper()).post { cleanup() } }
                    player.setOnPreparedListener {
                        if (!continuation.isActive) { cleanup(); return@setOnPreparedListener }
                        if (manager.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            cleanup()
                            continuation.resumeWithException(IOException("الصوت مشغول في تطبيق آخر. الرد المكتوب متاح."))
                        } else {
                            player.start()
                            Log.i(OpenRouterSpeech.TAG, "Playback started")
                            onPlaying()
                        }
                    }
                    player.setOnCompletionListener {
                        cleanup()
                        Log.i(OpenRouterSpeech.TAG, "Playback complete")
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                    player.setOnErrorListener { _, _, _ ->
                        cleanup()
                        if (continuation.isActive) continuation.resumeWithException(IOException("تعذر تشغيل الصوت. الرد المكتوب متاح."))
                        true
                    }
                    try {
                        player.setAudioAttributes(attributes)
                        player.setDataSource(file.absolutePath)
                        player.prepareAsync()
                    } catch (_: Exception) {
                        cleanup()
                        if (continuation.isActive) continuation.resumeWithException(IOException("تعذر تشغيل الصوت. الرد المكتوب متاح."))
                    }
                }
            }
        } finally {
            file.delete()
        }
    }
}
