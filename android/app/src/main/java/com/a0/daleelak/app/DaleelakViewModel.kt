package com.a0.daleelak.app

import android.content.Context
import com.a0.daleelak.BuildConfig
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a0.daleelak.ai.*
import com.a0.daleelak.data.LocalOperationStore
import com.a0.daleelak.domain.*
import com.a0.daleelak.voice.OpenRouterSpeech
import com.a0.daleelak.voice.PcmRecording
import com.a0.daleelak.voice.ReplyAudioPlayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

enum class Destination { ASSISTANT, OPERATIONS, LOCATIONS }
enum class VoiceState { IDLE, RECORDING, TRANSCRIBING, PREPARING_REPLY, PLAYING_REPLY }

/** Owns accepted guidance and user-reported progress; providers cannot own either. */
class DaleelakViewModel(
    private val store: LocalOperationStore,
    private val gateway: AssistantGateway = LocalReviewedAssistant(store.catalog),
    private val voiceContext: Context? = null,
) : ViewModel() {
    private val catalog = store.catalog
    private val codec = ContractCodec(catalog.schema)
    private val validator = ResponseValidator(catalog)
    private val writes = Mutex()
    private var responseJob: Job? = null
    private var voiceJob: Job? = null
    private var recording: PcmRecording? = null
    private var voiceGeneration = 0
    private var voiceForeground = false
    private var replyCache: Pair<String, ByteArray>? = null
    private var draftSaveJob: Job? = null
    private var generation = 0
    private var loading = true
    private var storageReadable = true
    private var activeOperationId: String? = null
    private var acceptedResponseJson: String? = null
    private var contextResponseJson: String? = null
    private var answers: Map<String, String> = emptyMap()
    private var questions: List<ClarificationQuestion> = emptyList()
    private var selection by mutableStateOf<String?>(null)

    var destination by mutableStateOf(Destination.ASSISTANT)
    private var draftValue by mutableStateOf("")
    var draft: String
        get() = draftValue
        set(value) {
            draftValue = value
            draftSaveJob?.cancel()
            if (!loading) draftSaveJob = viewModelScope.launch { delay(300); saveSession() }
        }
    var messages by mutableStateOf(listOf(ChatMessage("أهلاً! بإمكاني إرشادك لشهادة ولادة مسجلة، تصديق وثيقة، تحديث عنوان التبليغات أو الجزء الموثق لدفتر مفقود. المصدر دليل 2024؛ تحقق من القواعد الحالية.")))
        private set
    var operations by mutableStateOf(emptyList<Operation>())
        private set
    var notice by mutableStateOf<String?>(null)
    /** The local demo debug build can supply an embedded credential; UI overrides remain session-only. */
    var openRouterApiKey by mutableStateOf(BuildConfig.OPENROUTER_DEMO_KEY)
        private set
    var voiceState by mutableStateOf(VoiceState.IDLE)
        private set
    var playingReplyText by mutableStateOf<String?>(null)
        private set
    var readReplies by mutableStateOf(true)
        private set
    var voicePreset by mutableStateOf(OpenRouterSpeech.DEFAULT_VOICE)
        private set
    val isDictating: Boolean get() = voiceState == VoiceState.RECORDING || voiceState == VoiceState.TRANSCRIBING
    var currentPlan by mutableStateOf<GuidancePlan?>(null)
        private set
    var suggestedPrompts by mutableStateOf(catalog.startPrompts)
        private set
    var isResponding by mutableStateOf(false)
        private set
    val showPlan: Boolean get() = currentPlan != null
    val hasOpenRouterKey: Boolean get() = openRouterApiKey.isNotBlank()
    val readyForInput: Boolean get() = !loading
    val selected: Operation? get() = operations.firstOrNull { it.id == selection }
    var selectedId: String?
        get() = selection
        set(value) {
            selection = value
            if (value != null) operations.firstOrNull { it.id == value }?.let(::restoreOperation)
        }

    init {
        viewModelScope.launch {
            try {
                val loaded = withContext(Dispatchers.IO) { store.load() to store.loadSession() }
                operations = loaded.first
                loaded.second?.let(::restoreSession)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                storageReadable = false
                notice = "تعذر قراءة المحفوظات. لم نحذفها؛ الحفظ معطل لمنع استبدال بيانات غير مقروءة."
            } finally {
                loading = false
            }
        }
    }

    fun configureOpenRouterApiKey(value: String) {
        stopVoice()
        replyCache = null
        openRouterApiKey = value.trim()
        notice = if (openRouterApiKey.isBlank()) "عاد المساعد للوضع المحلي المحدود."
            else "تم تفعيل فهم الرسائل عبر OpenRouter لهذه الجلسة فقط؛ لا يُحفظ المفتاح."
    }

    fun configureVoicePreset(value: String) {
        val preset = value.trim().ifEmpty { OpenRouterSpeech.DEFAULT_VOICE }
        if (!preset.matches(Regex("[a-zA-Z0-9_-]{1,80}"))) {
            notice = "اسم الصوت غير صالح. استخدم اسماً أو معرّفاً من ElevenLabs."
            return
        }
        stopVoice()
        replyCache = null
        voicePreset = preset
    }

    fun configureReadReplies(enabled: Boolean) {
        readReplies = enabled
        if (!enabled && voiceState in setOf(VoiceState.PREPARING_REPLY, VoiceState.PLAYING_REPLY)) stopVoice()
    }

    fun onVoiceForegroundChanged(enabled: Boolean) {
        voiceForeground = enabled
        if (!enabled) stopVoice()
    }

    fun startCloudDictation() {
        if (!voiceForeground || !hasOpenRouterKey || isResponding) return
        stopVoice()
        val ticket = voiceGeneration
        val key = openRouterApiKey
        val before = draft
        val capture = PcmRecording()
        recording = capture
        voiceState = VoiceState.RECORDING
        voiceJob = viewModelScope.launch {
            try {
                val clip = capture.capture()
                if (!clip.hasSignal) throw IOException("ما وصلني كلام واضح. لم نرسل التسجيل؛ جرّب مرة ثانية أو اكتب رسالتك.")
                acceptTranscript(clip.wav, "audio/wav", key, before, ticket)
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                if (ticket == voiceGeneration) notice = error.message ?: "تعذر الإملاء. بقي نصك؛ تقدر تكتب رسالتك."
            } finally {
                if (ticket == voiceGeneration) { voiceState = VoiceState.IDLE; recording = null }
            }
        }
    }

    fun finishCloudDictation() {
        if (voiceState != VoiceState.RECORDING) return
        voiceState = VoiceState.TRANSCRIBING
        recording?.stop()
    }

    /** Same editable-composer boundary for an already captured clip and instrumentation fixtures. */
    fun transcribeClip(audio: ByteArray, mimeType: String) {
        if (!voiceForeground || !hasOpenRouterKey || isResponding) return
        stopVoice()
        val ticket = voiceGeneration
        val key = openRouterApiKey
        val before = draft
        voiceState = VoiceState.TRANSCRIBING
        voiceJob = viewModelScope.launch {
            try { acceptTranscript(audio, mimeType, key, before, ticket) }
            catch (error: CancellationException) { throw error }
            catch (error: Exception) { if (ticket == voiceGeneration) notice = error.message ?: "تعذر تفريغ التسجيل؛ بقي النص." }
            finally { if (ticket == voiceGeneration) voiceState = VoiceState.IDLE }
        }
    }

    private suspend fun acceptTranscript(audio: ByteArray, mimeType: String, key: String, before: String, ticket: Int) {
        if (ticket != voiceGeneration) return
        voiceState = VoiceState.TRANSCRIBING
        val transcript = OpenRouterSpeech(key).transcribe(audio, mimeType)
        if (ticket != voiceGeneration) return
        // Preserve edits made while the request was pending; never auto-send captured speech.
        val existing = if (draft == before) before else draft
        draft = listOf(existing.trim(), transcript.trim()).filter(String::isNotEmpty).joinToString(" ")
        notice = "راجع النص أو عدّله، ثم اضغط إرسال."
    }

    fun playReply(text: String) {
        val context = voiceContext ?: return
        if (!voiceForeground) return
        if (!hasOpenRouterKey) { notice = "فعّل مفتاح OpenRouter للاستماع للرد."; return }
        if (playingReplyText == text && voiceState in setOf(VoiceState.PREPARING_REPLY, VoiceState.PLAYING_REPLY)) {
            stopVoice()
            return
        }
        stopVoice()
        val ticket = voiceGeneration
        val key = openRouterApiKey
        val voice = voicePreset
        playingReplyText = text
        voiceState = VoiceState.PREPARING_REPLY
        voiceJob = viewModelScope.launch {
            try {
                val audio = replyCache?.takeIf { it.first == text }?.second ?: OpenRouterSpeech(key).synthesize(text, voice)
                if (ticket != voiceGeneration) return@launch
                replyCache = text to audio
                ReplyAudioPlayer(context).play(audio) { if (ticket == voiceGeneration) voiceState = VoiceState.PLAYING_REPLY }
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) { if (ticket == voiceGeneration) notice = error.message ?: "تعذر الاستماع؛ الرد المكتوب متاح." }
            finally {
                if (ticket == voiceGeneration) { voiceState = VoiceState.IDLE; playingReplyText = null }
            }
        }
    }

    fun stopVoice() {
        voiceGeneration++
        recording?.stop()
        recording = null
        voiceJob?.cancel()
        voiceJob = null
        voiceState = VoiceState.IDLE
        playingReplyText = null
    }

    override fun onCleared() {
        stopVoice()
        replyCache = null
        openRouterApiKey = ""
        super.onCleared()
    }

    /** Draft stays editable and is cleared only after a validated response succeeds. */
    fun send(text: String = draft) {
        if (text.isBlank() || isResponding) return
        if (loading) { notice = "جارٍ تحميل المحفوظات، حاول بعد لحظة."; return }
        stopVoice()
        val submitted = text.trim()
        val previousDraft = draft
        val ticket = generation
        val request = AssistantRequest(submitted, messages, answers, currentPlan, questions)
        val activeGateway = openRouterApiKey.takeIf { it.isNotBlank() }
            ?.let { OpenRouterAssistant(catalog, it) } ?: gateway
        isResponding = true
        responseJob = viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.Default) {
                    // Even typed gateways must pass the unchanged JSON schema and semantic checks.
                    val parsed = codec.decode(codec.encode(activeGateway.respond(request)))
                    validator.validate(parsed)
                    parsed
                }
                if (ticket != generation) return@launch
                val encoded = codec.encode(response)
                answers = response.caseSummary.knownFacts.filter { it.origin == "user" }.associate { it.key to it.value }
                questions = response.questions
                suggestedPrompts = response.suggestedPrompts
                contextResponseJson = encoded
                val acceptedText = ResponsePresentation.message(response, catalog)
                messages = messages + ChatMessage(submitted, true) + ChatMessage(acceptedText)
                if (draft == previousDraft) draft = ""
                when (response.kind) {
                    ResponseKind.PLAN -> {
                        currentPlan = catalog.toDomain(response)
                        acceptedResponseJson = encoded
                    }
                    ResponseKind.CLARIFICATION -> {
                        // A correction that makes the goal unclear must not keep presenting an old recommendation.
                        currentPlan = null; acceptedResponseJson = null
                    }
                    ResponseKind.UNSUPPORTED -> {
                        currentPlan = null
                        acceptedResponseJson = null
                        // Preserve the existing operation snapshot; do not replace it with another service.
                        activeOperationId = null
                    }
                }
                saveActiveContext()
                saveSession()
                if (readReplies && hasOpenRouterKey) playReply(acceptedText)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (ticket == generation) {
                    if (draft == previousDraft && previousDraft.isBlank()) draft = submitted
                    notice = if (error is IOException && !error.message.isNullOrBlank()) error.message
                        else "تعذر قبول الرد. بقيت رسالتك والخطة السابقة؛ حاول مجدداً. هذا خطأ استجابة وليس خدمة غير مدعومة."
                }
            } finally {
                if (ticket == generation) isResponding = false
            }
        }
    }

    fun newConversation() {
        stopVoice()
        generation++; responseJob?.cancel(); isResponding = false
        activeOperationId = null; answers = emptyMap(); questions = emptyList()
        acceptedResponseJson = null; contextResponseJson = null; currentPlan = null
        suggestedPrompts = catalog.startPrompts; draft = ""
        messages = listOf(ChatMessage("محادثة جديدة. احكيلي عن المشكلة؛ سأستخدم فقط الجزء المثبت في الوثائق المتاحة."))
        saveSession()
    }

    fun savePlan() {
        if (isResponding || loading) return
        val plan = currentPlan ?: run { notice = "لا توجد خطة مقبولة للحفظ بعد."; return }
        val context = conversationSnapshot()
        val accepted = acceptedResponseJson
        val responseContext = contextResponseJson
        val facts = answers.toMap()
        val existingId = activeOperationId ?: operations.firstOrNull {
            it.plan.serviceId == plan.serviceId && it.status != OperationStatus.COMPLETED
        }?.id
        writeOperations { latest ->
            val existing = latest.firstOrNull { it.id == existingId }
            val operation = if (existing == null) Operation(UUID.randomUUID().toString(), plan.title, plan,
                answers = facts, conversation = context, acceptedResponseJson = accepted, contextResponseJson = responseContext)
            else reconcilePlan(existing, plan, facts).copy(answers = facts, conversation = context,
                acceptedResponseJson = accepted, contextResponseJson = responseContext, updatedAt = System.currentTimeMillis())
            activeOperationId = operation.id
            selection = operation.id
            destination = Destination.OPERATIONS
            latest.filterNot { it.id == operation.id } + operation
        }
    }

    fun toggleStep(id: String) = update { operation ->
        val step = operation.plan.steps.firstOrNull { it.id == id } ?: return@update operation
        if (operation.status == OperationStatus.COMPLETED) return@update operation
        val done = id in operation.completedStepIds
        if (!done && !operation.completedStepIds.containsAll(step.dependsOn)) {
            notice = "أكمل الخطوات السابقة أولاً."; operation
        } else {
            var completed = if (done) operation.completedStepIds - id else operation.completedStepIds + id
            while (true) {
                val invalid = operation.plan.steps.filter { it.id in completed && !completed.containsAll(it.dependsOn) }.map { it.id }.toSet()
                if (invalid.isEmpty()) break
                completed = completed - invalid
            }
            operation.copy(completedStepIds = completed, status = OperationStatus.ONGOING)
        }
    }
    fun toggleRequirement(id: String) = update { operation ->
        if (operation.plan.steps.none { step -> step.requirements.any { it.id == id } } || operation.status == OperationStatus.COMPLETED) operation
        else operation.copy(checkedRequirementIds = if (id in operation.checkedRequirementIds) operation.checkedRequirementIds - id else operation.checkedRequirementIds + id,
            status = OperationStatus.ONGOING)
    }
    fun setCompleted() = update { operation ->
        if (!operation.completedStepIds.containsAll(operation.plan.steps.map { it.id })) {
            notice = "راجع كل الخطوات أولاً."; operation
        } else {
            notice = "اكتملت متابعة الجزء المحفوظ حسب تأكيدك فقط؛ ليست موافقة حكومية أو إثبات اكتمال الإجراء الكامل."
            operation.copy(status = OperationStatus.COMPLETED)
        }
    }
    fun reopen() = update { it.copy(status = OperationStatus.ONGOING) }
    fun bookDemo() = update { operation ->
        val event = DemoEvent("DEMO-${UUID.randomUUID().toString().take(8)}", "حجز تجريبي محلي فقط. لم يُنشأ موعد رسمي ولا يدل على توافر أو اشتراط الحجز.", System.currentTimeMillis())
        operation.copy(demoEvents = operation.demoEvents + event)
    }
    fun deleteSelected() {
        val id = selection ?: return
        writeOperations { latest ->
            if (activeOperationId == id) activeOperationId = null
            selection = null
            latest.filterNot { it.id == id }
        }
    }
    private fun update(transform: (Operation) -> Operation) {
        val id = selection ?: return
        writeOperations { latest -> latest.map { if (it.id == id) transform(it).copy(updatedAt = System.currentTimeMillis()) else it } }
    }
    private fun saveActiveContext() {
        val id = activeOperationId ?: return
        val plan = currentPlan
        val facts = answers.toMap()
        val context = conversationSnapshot()
        val accepted = acceptedResponseJson
        val response = contextResponseJson
        writeOperations { latest -> latest.map { operation ->
            if (operation.id != id) operation
            else (if (plan != null && plan.serviceId == operation.plan.serviceId) reconcilePlan(operation, plan, facts) else operation)
                .copy(answers = facts, conversation = context, acceptedResponseJson = accepted ?: operation.acceptedResponseJson,
                    contextResponseJson = response, updatedAt = System.currentTimeMillis())
        } }
    }
    private fun reconcilePlan(operation: Operation, plan: GuidancePlan, facts: Map<String, String>): Operation {
        val reconciled = PlanReconciler.reconcile(operation, plan, facts)
        if (reconciled.completedStepIds != operation.completedStepIds || reconciled.checkedRequirementIds != operation.checkedRequirementIds) {
            notice = "تغيرت إجابة أو مهمة تؤثر على التقدم؛ أُعيد فتح الجزء المتأثر فقط وبقي العمل غير المتأثر محفوظاً."
        }
        return reconciled
    }
    private fun writeOperations(transform: (List<Operation>) -> List<Operation>) {
        if (loading || !storageReadable) { notice = "الحفظ غير متاح حتى تُقرأ البيانات المحلية بنجاح."; return }
        viewModelScope.launch {
            writes.withLock {
                val previousId = activeOperationId
                val previousSelection = selection
                val previousDestination = destination
                val next = transform(operations)
                try {
                    withContext(Dispatchers.IO) { store.save(next) }
                    operations = next
                    saveSession()
                } catch (error: CancellationException) { throw error }
                catch (error: Exception) {
                    activeOperationId = previousId; selection = previousSelection; destination = previousDestination
                    notice = "فشل الحفظ؛ بقيت الخطة والمحادثة ظاهرتين والبيانات المحفوظة السابقة محفوظة."
                }
            }
        }
    }

    private fun restoreOperation(operation: Operation) {
        stopVoice()
        generation++; responseJob?.cancel(); isResponding = false
        activeOperationId = operation.id
        answers = operation.answers
        messages = operation.conversation.ifEmpty { listOf(ChatMessage("تم استئناف المعاملة المحفوظة. يمكن تصحيح إجاباتها دون حذف التقدم غير المتأثر.")) }
        currentPlan = operation.plan
        acceptedResponseJson = operation.acceptedResponseJson
        contextResponseJson = operation.contextResponseJson ?: operation.acceptedResponseJson
        questions = emptyList(); suggestedPrompts = catalog.startPrompts
        contextResponseJson?.let { raw ->
            try {
                val response = codec.decode(raw); validator.validate(response)
                questions = response.questions; suggestedPrompts = response.suggestedPrompts
                if (response.kind == ResponseKind.UNSUPPORTED || response.kind != ResponseKind.PLAN) currentPlan = null
            } catch (error: Exception) { notice = "الخطة المحفوظة معروضة كما كانت؛ سياق الرد يحتاج مراجعة قبل استخدامه." }
        }
        saveSession()
    }
    private fun conversationSnapshot(): List<ChatMessage> = messages.toList()
    private fun saveSession() {
        if (loading || !storageReadable) return
        val raw = JSONObject().put("version", 1).put("activeOperationId", activeOperationId ?: JSONObject.NULL)
            .put("draft", draft).put("acceptedResponseJson", acceptedResponseJson ?: JSONObject.NULL)
            .put("contextResponseJson", contextResponseJson ?: JSONObject.NULL).put("answers", JSONObject(answers))
            .put("messages", JSONArray().also { array -> messages.forEach { array.put(JSONObject().put("text", it.text).put("fromUser", it.fromUser)) } }).toString()
        viewModelScope.launch {
            writes.withLock {
                try { withContext(Dispatchers.IO) { store.saveSession(raw) } }
                catch (error: CancellationException) { throw error }
                catch (error: Exception) { notice = "تعذر حفظ سياق المحادثة؛ المعاملات المحفوظة لم تُحذف." }
            }
        }
    }
    private fun restoreSession(raw: String) {
        val session = JSONObject(raw)
        require(session.getInt("version") == 1)
        activeOperationId = if (session.isNull("activeOperationId")) null else session.getString("activeOperationId").takeIf { id -> operations.any { it.id == id } }
        draft = session.optString("draft")
        val storedAnswers = session.getJSONObject("answers")
        answers = storedAnswers.keys().asSequence().associateWith { storedAnswers.getString(it) }
        val storedMessages = session.getJSONArray("messages")
        messages = (0 until storedMessages.length()).map { storedMessages.getJSONObject(it).let { item -> ChatMessage(item.getString("text"), item.getBoolean("fromUser")) } }
        acceptedResponseJson = if (session.isNull("acceptedResponseJson")) null else session.getString("acceptedResponseJson")
        contextResponseJson = if (session.isNull("contextResponseJson")) null else session.getString("contextResponseJson")
        acceptedResponseJson?.let { val accepted = codec.decode(it); validator.validate(accepted); currentPlan = catalog.toDomain(accepted) }
        contextResponseJson?.let { val context = codec.decode(it); validator.validate(context)
            questions = context.questions; suggestedPrompts = context.suggestedPrompts
            if (context.kind == ResponseKind.UNSUPPORTED || context.caseSummary.knownFacts.none { it.key == "issue" && it.value == "lost" }) currentPlan = null
        }
    }
}
