package com.a0.daleelak.voice

import android.util.Log
import com.a0.daleelak.domain.TranscriptionGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Base64
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Audio and credentials are transient. This adapter never returns government guidance. */
class OpenRouterSpeech(private val apiKey: String) : TranscriptionGateway {
    override suspend fun transcribe(audio: ByteArray, mimeType: String): String = withContext(Dispatchers.IO) {
        require(audio.isNotEmpty() && audio.size <= MAX_UPLOAD_BYTES) { "التسجيل فارغ أو أطول من الحد المسموح." }
        val format = when (mimeType) {
            "audio/wav", "audio/x-wav" -> "wav"
            "audio/mpeg" -> "mp3"
            else -> throw IOException("صيغة التسجيل غير مدعومة.")
        }
        val input = JSONObject()
            .put("model", TRANSCRIPTION_MODEL)
            .put("input_audio", JSONObject().put("data", Base64.getEncoder().encodeToString(audio)).put("format", format))
            .put("response_format", "json")
            // Let Scribe detect both languages; an Arabic-only hint can lose English spans.
            .put("provider", JSONObject().put("options", JSONObject().put("elevenlabs",
                JSONObject().put("diarize", false).put("tag_audio_events", false))))
        request("audio/transcriptions", input).use { response ->
            requireSuccess(response)
            val json = try { JSONObject(String(readLimited(response, 256 * 1024), Charsets.UTF_8)) }
                catch (_: Exception) { throw IOException("وصل نص تفريغ غير صالح. بقيت رسالتك؛ حاول مجدداً.") }
            val text = (json.opt("text") as? String).orEmpty().trim()
            if (text.isBlank() || text.length > 8000) throw IOException("ما وصلني كلام واضح. جرّب مرة ثانية أو اكتب رسالتك.")
            val usage = json.optJSONObject("usage")
            Log.i(TAG, "STT complete model=$TRANSCRIPTION_MODEL characters=${text.length} cost=${usage?.optDouble("cost", 0.0)}")
            text
        }
    }

    suspend fun synthesize(text: String, voice: String = DEFAULT_VOICE): ByteArray = withContext(Dispatchers.IO) {
        require(text.isNotBlank() && text.length <= 10_000) { "الرد فارغ أو طويل جداً للقراءة الصوتية." }
        val input = JSONObject().put("model", SPEECH_MODEL).put("input", text)
            .put("voice", voice).put("response_format", "mp3")
        request("audio/speech", input).use { response ->
            requireSuccess(response)
            val type = response.header("Content-Type").orEmpty()
            if (!type.startsWith("audio/") && !type.startsWith("application/octet-stream")) {
                throw IOException("لم تصل قراءة صوتية صالحة. الرد المكتوب متاح.")
            }
            val audio = readLimited(response, 10 * 1024 * 1024)
            if (audio.size < 128) throw IOException("وصل صوت فارغ. الرد المكتوب متاح.")
            Log.i(TAG, "TTS complete model=$SPEECH_MODEL characters=${text.length} bytes=${audio.size}")
            audio
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun request(path: String, payload: JSONObject): Response = suspendCancellableCoroutine { continuation ->
        if (apiKey.isBlank()) {
            continuation.resumeWithException(IOException("فعّل مفتاح OpenRouter لاستخدام الصوت."))
            return@suspendCancellableCoroutine
        }
        val request = Request.Builder().url("https://openrouter.ai/api/v1/$path")
            .header("Authorization", "Bearer ${apiKey.trim()}")
            .header("X-Title", "DALEELAK")
            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())).build()
        val call = client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(IOException("تعذر الاتصال بخدمة الصوت. بقي النص؛ حاول مجدداً."))
            }
            override fun onResponse(call: Call, response: Response) {
                if (continuation.isActive) continuation.resume(response) { response.close() }
                else response.close()
            }
        })
    }

    private fun requireSuccess(response: Response) {
        if (response.isSuccessful) return
        // Never expose raw upstream errors, which can contain audio or credential-bearing data.
        val message = when (response.code) {
            401, 403 -> "مفتاح OpenRouter غير مقبول أو لا يسمح بهذا النموذج."
            402, 429 -> "تحقق من رصيد OpenRouter وحد الاستخدام ثم أعد المحاولة."
            else -> "تعذرت خدمة الصوت (HTTP ${response.code}). الرد المكتوب ورسالتك محفوظان."
        }
        throw IOException(message)
    }

    private fun readLimited(response: Response, limit: Int): ByteArray {
        val body = response.body ?: throw IOException("لم تصل بيانات من خدمة الصوت.")
        if (body.contentLength() > limit) throw IOException("بيانات الصوت أكبر من الحد المسموح.")
        val output = ByteArrayOutputStream()
        body.byteStream().use { stream ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                if (output.size() + count > limit) throw IOException("بيانات الصوت أكبر من الحد المسموح.")
                output.write(buffer, 0, count)
            }
        }
        return output.toByteArray()
    }

    companion object {
        const val TRANSCRIPTION_MODEL = "elevenlabs/scribe-v2"
        const val SPEECH_MODEL = "elevenlabs/eleven-v4-turbo"
        const val DEFAULT_VOICE = "sarah"
        const val MAX_UPLOAD_BYTES = 2 * 1024 * 1024
        const val TAG = "DaleelakVoice"
        private val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS).callTimeout(75, TimeUnit.SECONDS).build()
    }
}
