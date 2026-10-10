package com.a0.daleelak

import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.SystemClock
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.a0.daleelak.app.DaleelakViewModel
import com.a0.daleelak.app.VoiceState
import com.a0.daleelak.voice.OpenRouterSpeech
import com.a0.daleelak.voice.PcmRecording
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.atomic.AtomicReference

/** Opt-in live app-flow checks. Keys come from runner arguments, never an APK or an asset. */
@RunWith(AndroidJUnit4::class)
class VoiceIntegrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var model: DaleelakViewModel
    private lateinit var key: String

    @Before fun launch() {
        val args = InstrumentationRegistry.getArguments()
        key = args.getString("runtime_key").orEmpty()
        assumeTrue("Live voice tests require explicit opt-in and a runtime key",
            args.getString("allow_paid_voice_tests") == "true" && key.isNotBlank())
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { model = ViewModelProvider(it)[DaleelakViewModel::class.java] }
        await("App loads saved state") { read { model.readyForInput } }
        ui { model.newConversation(); model.configureOpenRouterApiKey(key); model.notice = null }
    }

    @After fun cleanup() {
        if (::model.isInitialized) ui { model.configureOpenRouterApiKey("") }
        if (::scenario.isInitialized) scenario.close()
    }

    @Test fun mixedSpeechStaysUnsentThenProducesGroundedPlanAndAutomaticPlayback() = runBlocking {
        val bytes = OpenRouterSpeech(key).synthesize(
            "ضاع دفتر العيلة، ولسه ما عندي بلاغ. Can I start online? شو الـ requirements؟")
        assertDecodableAudio(bytes)
        val messageCount = read { model.messages.size }
        ui { model.transcribeClip(bytes, "audio/mpeg") }
        await("Mixed speech transcript") { read { model.voiceState == VoiceState.IDLE } }
        val transcript = read { model.draft }
        assertTrue("Arabic must remain present", transcript.any { it in '\u0600'..'\u06ff' })
        assertTrue("English must remain present", transcript.any { it in 'A'..'Z' || it in 'a'..'z' })
        assertEquals("Transcription must not auto-send", messageCount, read { model.messages.size })
        assertNotNull("A review notice is shown", read { model.notice })
        ui { model.send() }
        await("Grounded reply finishes") { read { !model.isResponding } }
        val plan = read { model.currentPlan }
        assertNotNull("Lost-book plan is accepted; notice=${read { model.notice }}", plan)
        assertFalse("Plan must use reviewed guidance", requireNotNull(plan).illustrative)
        assertTrue("Sources remain attached", plan.sourceVersions.isNotEmpty())
        assertTrue("Negative report answer must survive speech and routing",
            read { model.messages.last().text }.contains("المتطلب السابق الموثق"))
        await("Automatic audio playback") { read { model.voiceState == VoiceState.PLAYING_REPLY } }
        val reply = read { model.messages.last().text }
        ui { model.stopVoice() }
        assertEquals(VoiceState.IDLE, read { model.voiceState })
        // Replay should use the last in-memory clip, not buy another synthesis.
        ui { model.playReply(reply) }
        await("Cached replay") { read { model.voiceState == VoiceState.PLAYING_REPLY } }
        ui { model.startCloudDictation() }
        assertEquals("Recording interrupts playback", VoiceState.RECORDING, read { model.voiceState })
        ui { model.newConversation() }
        Thread.sleep(600)
        assertEquals("Cancelled capture cannot write into a new conversation", "", read { model.draft })
        assertEquals(VoiceState.IDLE, read { model.voiceState })
    }

    @Test fun englishSpeechRoutesPositivePoliceReport() = runBlocking {
        ui { model.configureReadReplies(false) }
        val bytes = OpenRouterSpeech(key).synthesize(
            "I lost my family booklet. I have a police report. What are the next steps?")
        ui { model.transcribeClip(bytes, "audio/mpeg") }
        await("English transcript") { read { model.voiceState == VoiceState.IDLE } }
        assertTrue(read { model.draft }.contains("police", ignoreCase = true))
        ui { model.send() }
        await("English grounded reply finishes") { read { !model.isResponding } }
        assertNotNull("English plan; notice=${read { model.notice }}", read { model.currentPlan })
        assertTrue("Positive report answer changes the next action",
            read { model.messages.last().text }.contains("حسب إجابتك البلاغ متوفر"))
        assertEquals("Muted reply must not trigger TTS", VoiceState.IDLE, read { model.voiceState })
    }

    @Test fun failedTranscriptionKeepsDraftAndNeverSendsIt() = runBlocking {
        ui { model.draft = "existing draft" }
        val messageCount = read { model.messages.size }
        ui { model.transcribeClip(byteArrayOf(), "audio/wav") }
        await("Empty clip rejected") { read { model.voiceState == VoiceState.IDLE } }
        assertEquals("existing draft", read { model.draft })
        assertEquals(messageCount, read { model.messages.size })
        assertNotNull(read { model.notice })
        val bytes = OpenRouterSpeech(key).synthesize("مرحبا، هذا فحص قصير للصوت.")
        ui { model.configureOpenRouterApiKey("invalid-for-integration-test"); model.notice = null }
        ui { model.transcribeClip(bytes, "audio/mpeg") }
        await("Rejected runtime credential") { read { model.voiceState == VoiceState.IDLE } }
        assertEquals("existing draft", read { model.draft })
        assertEquals(messageCount, read { model.messages.size })
        assertTrue(read { model.notice }.orEmpty().contains("مفتاح OpenRouter"))
    }

    @Test fun microphoneCaptureStopsAndBackgroundCancelsVoice() = runBlocking {
        val recording = PcmRecording()
        val capture = async { recording.capture() }
        delay(600)
        recording.stop()
        val clip = withTimeout(5000) { capture.await() }
        assertEquals("RIFF", String(clip.wav.copyOfRange(0, 4), Charsets.US_ASCII))
        assertEquals("WAVE", String(clip.wav.copyOfRange(8, 12), Charsets.US_ASCII))
        assertTrue("Actual microphone samples must be captured", clip.wav.size > 44)
        assertTrue(clip.wav.size <= 44 + PcmRecording.MAX_PCM_BYTES)
        ui { model.startCloudDictation() }
        assertEquals(VoiceState.RECORDING, read { model.voiceState })
        scenario.moveToState(Lifecycle.State.CREATED)
        await("Background cancels capture") { read { model.voiceState == VoiceState.IDLE } }
        Thread.sleep(500)
        assertEquals("", read { model.draft })
    }

    private fun assertDecodableAudio(bytes: ByteArray) {
        val file = File.createTempFile("voice-test-", ".mp3", instrumentation.targetContext.cacheDir)
        val extractor = MediaExtractor()
        try {
            file.writeBytes(bytes)
            extractor.setDataSource(file.absolutePath)
            assertTrue(extractor.trackCount > 0)
            val format = extractor.getTrackFormat(0)
            assertTrue(format.getString(MediaFormat.KEY_MIME).orEmpty().startsWith("audio/"))
            assertTrue(format.getLong(MediaFormat.KEY_DURATION) > 0)
        } finally { extractor.release(); file.delete() }
    }

    private fun ui(action: () -> Unit) { instrumentation.runOnMainSync(action) }
    private fun <T> read(action: () -> T): T {
        val result = AtomicReference<T>()
        ui { result.set(action()) }
        return result.get()
    }
    private fun await(description: String, predicate: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + 90_000
        while (SystemClock.elapsedRealtime() < deadline) {
            if (predicate()) return
            Thread.sleep(100)
        }
        fail("Timed out: $description; notice=${read { model.notice }}")
    }
}
