package com.a0.daleelak.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.a0.daleelak.data.DemoCatalog
import com.a0.daleelak.data.LocalOperationStore
import com.a0.daleelak.domain.*
import java.util.UUID

enum class Destination { ASSISTANT, OPERATIONS, LOCATIONS }

class DaleelakViewModel(private val store: LocalOperationStore) : ViewModel() {
    var destination by mutableStateOf(Destination.ASSISTANT)
    var draft by mutableStateOf("")
    var messages by mutableStateOf(listOf(ChatMessage("أهلاً! احكيلي شو بتحتاج. هذه محادثة تجريبية، وليست استجابة من AI متصل.")))
        private set
    var operations by mutableStateOf(emptyList<Operation>())
        private set
    var selectedId by mutableStateOf<String?>(null)
    var notice by mutableStateOf<String?>(null)
    var showPlan by mutableStateOf(false)
        private set
    private var answers: Map<String, String> = emptyMap()
    private var stage = 0
    val selected: Operation? get() = operations.firstOrNull { it.id == selectedId }

    init {
        runCatching { store.load() }.onSuccess { operations = it }
            .onFailure { notice = "تعذر قراءة البيانات المحفوظة. لم يتم حذفها أو استبدالها." }
    }

    fun send(text: String = draft) {
        if (text.isBlank()) return
        messages = messages + ChatMessage(text.trim(), true)
        draft = ""
        val reply = when (stage) {
            0 -> { answers = answers + ("goal" to text); stage = 1; "للتجربة: ضاع داخل الأردن ولا خارجها؟" }
            1 -> { answers = answers + ("loss_location" to text); stage = 2; "للتجربة: أول مرة يضيع ولا تكرر الفقدان؟" }
            else -> { answers = answers + ("loss_history" to text); showPlan = true; "هذا ملخص إجاباتك ونموذج شكل الخطة. المتطلبات الرسمية ستُربط لاحقاً بالمصادر المراجعة." }
        }
        messages = messages + ChatMessage(reply)
    }

    fun newConversation() {
        stage = 0; answers = emptyMap(); showPlan = false; draft = ""
        messages = listOf(ChatMessage("محادثة تجريبية جديدة. كيف أقدر أساعدك؟"))
    }

    fun savePlan() {
        val existing = operations.firstOrNull { it.plan.serviceId == DemoCatalog.plan.serviceId && it.status != OperationStatus.COMPLETED }
        if (existing != null) { selectedId = existing.id; destination = Destination.OPERATIONS; return }
        val operation = Operation(UUID.randomUUID().toString(), DemoCatalog.plan.title, DemoCatalog.plan, answers)
        if (persist(operations + operation)) { selectedId = operation.id; destination = Destination.OPERATIONS }
    }

    fun toggleStep(id: String) = update { operation ->
        val step = operation.plan.steps.first { it.id == id }
        val done = id in operation.completedStepIds
        if (!done && !operation.completedStepIds.containsAll(step.dependsOn)) {
            notice = "أكمل الخطوات السابقة أولاً."; operation
        } else {
            var completed = if (done) operation.completedStepIds - id else operation.completedStepIds + id
            // Reopening a dependency also reopens dependent steps, preserving unrelated work.
            while (true) {
                val invalid = operation.plan.steps.filter { it.id in completed && !completed.containsAll(it.dependsOn) }.map { it.id }.toSet()
                if (invalid.isEmpty()) break
                completed = completed - invalid
            }
            operation.copy(completedStepIds = completed, status = OperationStatus.ONGOING)
        }
    }
    fun toggleRequirement(id: String) = update { operation -> operation.copy(
        checkedRequirementIds = if (id in operation.checkedRequirementIds) operation.checkedRequirementIds - id else operation.checkedRequirementIds + id,
        status = OperationStatus.ONGOING) }
    fun setCompleted() = update { operation ->
        if (operation.completedStepIds.size != operation.plan.steps.size) { notice = "راجع كل الخطوات أولاً."; operation }
        else operation.copy(status = OperationStatus.COMPLETED)
    }
    fun reopen() = update { it.copy(status = OperationStatus.ONGOING) }
    fun bookDemo() = update { operation ->
        val event = DemoEvent("DEMO-${UUID.randomUUID().toString().take(8)}", "تم إنشاء حجز تجريبي محلي فقط. لا يوجد موعد رسمي أو توافر حقيقي.", System.currentTimeMillis())
        operation.copy(demoEvents = operation.demoEvents + event) // Never alters official/user-reported progress.
    }
    fun deleteSelected() {
        if (persist(operations.filterNot { it.id == selectedId })) selectedId = null
    }
    private fun update(transform: (Operation) -> Operation) {
        val id = selectedId ?: return
        persist(operations.map { if (it.id == id) transform(it).copy(updatedAt = System.currentTimeMillis()) else it })
    }
    private fun persist(next: List<Operation>): Boolean = runCatching { store.save(next) }
        .onSuccess { operations = next }
        .onFailure { notice = "فشل الحفظ. التغييرات السابقة ما زالت محفوظة؛ حاول مجدداً." }.isSuccess
}
