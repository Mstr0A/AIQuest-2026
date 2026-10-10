package com.a0.daleelak.data

import android.content.res.AssetManager
import com.a0.daleelak.ai.*
import com.a0.daleelak.domain.*
import org.json.JSONObject
import org.json.JSONArray

data class ReviewedSource(
    val id: String, val title: String, val url: String, val version: String,
    val accessedAt: String, val provenance: String, val reviewedExcerpt: String,
    val supportedRules: List<String>, val gaps: List<String>,
    val gapsEnglish: List<String> = emptyList(),
)

data class AdditionalService(val id: String, val requiredFact: String, val allowedValues: List<String>,
    val supportedValue: String, val question: ClarificationQuestion, val response: AssistantResponse)

/** Bundled reviewed source extracts and legacy cards; no remote document fetch at runtime. */
class ReviewedCatalog private constructor(schemaText: String, sourceText: String, additionalText: String) {
    constructor(assets: AssetManager) : this(
        assets.open("guidance/ai-response.schema.json").bufferedReader().use { it.readText() },
        assets.open("guidance/reviewed-sources.json").bufferedReader().use { it.readText() },
        assets.open("guidance/additional-services.json").bufferedReader().use { it.readText() })
    companion object {
        fun fromReviewedJson(schema: String, sources: String, additional: String) = ReviewedCatalog(schema, sources, additional)
    }
    val schema: JSONObject = JSONObject(schemaText)
    private val root = JSONObject(sourceText)
    private val additionalRoot = JSONObject(additionalText)
    val version: String = root.getString("version") + "+" + additionalRoot.getString("version")
    val serviceId: String = root.getString("service_id")
    val sources: List<ReviewedSource> = JSONArray().also { all ->
        listOf(root, additionalRoot).forEach { record -> record.getJSONArray("sources").let { entries ->
            (0 until entries.length()).forEach { all.put(entries.getJSONObject(it)) }
        } }
    }.let { entries ->
        (0 until entries.length()).map { index -> entries.getJSONObject(index).let { source ->
            ReviewedSource(source.getString("id"), source.getString("title"), source.getString("url"),
                source.getString("version"), source.getString("accessed_at"), source.getString("provenance"),
                source.getString("reviewed_excerpt"), source.getJSONArray("supported_rules").strings(), source.getJSONArray("gaps").strings(),
                source.optJSONArray("gaps_en")?.strings().orEmpty())
        } }
    }
    fun conditionalGap(id: String): String {
        val service = additionalServices.first { it.id == id }
        val prerequisite = when (id) {
            "lost_passport" -> "الجواز أردني عادي"
            "birth_certificate" -> "واقعة الولادة مسجلة حاسوبياً لدى الأحوال المدنية"
            "document_attestation" -> "أصل الوثيقة صادر عن دائرة الأحوال المدنية والجوازات"
            "declared_address" -> "المستدعي رب الأسرة أو من ينوب عنه"
            else -> error("Unknown conditional service")
        }
        return "هذه خطة مشروطة لمسار: ${service.response.plan!!.title}. شرط المسار: $prerequisite. لم يتأكد انطباقه على حالتك؛ لا تعتبر هذه الخطة تأكيد أهلية."
    }

    val places: List<Place> = emptyList()
    val sourceIds = root.getJSONArray("sources").let { entries -> (0 until entries.length()).map { entries.getJSONObject(it).getString("id") } }
    val gaps = sources.filter { it.id in sourceIds }.flatMap { it.gaps }
    val additionalServices = additionalRoot.getJSONArray("services").let { entries ->
        val codec = ContractCodec(schema)
        (0 until entries.length()).map { index -> entries.getJSONObject(index).let { service ->
            val q = service.getJSONObject("question")
            AdditionalService(service.getString("id"), service.getString("required_fact"),
                service.getJSONArray("allowed_values").strings(), service.getString("supported_value"),
                ClarificationQuestion(q.getString("id"), q.getString("text"), q.getJSONArray("options").strings(), q.getString("reason")),
                codec.decode(service.getJSONObject("response").toString()))
        } }
    }
    val allowedPrompts get() = startPrompts + legacyStartPrompts + reportQuestion.options + listOf("مش متأكد إذا عندي بلاغ") +
        additionalServices.flatMap { it.response.suggestedPrompts + it.question.options }
    private val legacyStartPrompts = listOf("ضاع دفتر العيلة، شو أعمل؟", "شو الأوراق المطلوبة لتعويض دفتر العائلة؟", "عندي بلاغ فقدان، شو الخطوة الجاية؟")
    val startPrompts = listOf("بدي أوضح المشكلة أكثر", "بدي أبدأ طلب جديد", "بدي أصحح معلومة ذكرتها")
    val reportQuestion = ClarificationQuestion("police_report", "هل عندك بلاغ فقدان من الشرطة؟",
        listOf("عندي بلاغ فقدان", "لسه ما عندي بلاغ", "مش متأكد"),
        "المصدر المراجع يذكر البلاغ كمتطلب سابق؛ الإجابة تحدد إذا نبدأ بالبلاغ أو بالقناة الإلكترونية.")
    val goalQuestion = ClarificationQuestion("issue", "شو المشكلة أو المعاملة اللي بدك مساعدة فيها؟",
        emptyList(), "احكيلي شو بدك تعمل حتى أفهم طلبك بدون تخمين.")
    private val legacyMultiGoalQuestion = ClarificationQuestion("issue", "أي مشكلة تريد حلها؟",
        listOf("شهادة ولادة لواقعة مسجلة", "تصديق صورة وثيقة", "تحديث العنوان المصرح به", "دفتر عائلة مفقود", "خدمة ثانية"), "لتحديد الخدمة التي تغطيها الوثائق المراجعة دون تخمين.")
    private val legacyGoalQuestion = ClarificationQuestion("issue", "هل المطلوب تعويض دفتر عائلة مفقود؟",
        listOf("نعم، دفتر عائلة مفقود", "دفتر تالف", "خدمة ثانية"), "لتحديد إذا المشكلة ضمن الخدمة التي تغطيها الوثائق المراجعة.")
    val approvedQuestions = listOf(goalQuestion, reportQuestion, legacyGoalQuestion, legacyMultiGoalQuestion)

    // These are narrow app-authored paraphrases of the supplied recorded inspection.
    // Full procedure/branch/fee records are NOT implied by these templates.
    val supportedPlan = PlanDto("تعويض دفتر عائلة مفقود · الجزء الموثق", serviceId, "electronic",
        "السجل المراجع يثبت وجود مسار إلكتروني وبلاغ شرطة كمتطلب سابق. هذه خطة جزئية؛ التفاصيل غير المتاحة مبينة صراحة.", listOf(
            StepDto("police_report", "بلاغ فقدان من الشرطة", "المصدر المراجع يذكر بلاغ الشرطة كمتطلب سابق. صيغة البلاغ وطريقة تقديمه غير محددتين في الملفات المتاحة.",
                "user", emptyList(), checklists(documents = listOf(ChecklistItemDto("police_report_document", "بلاغ فقدان من الشرطة",
                    "required", null, "unspecified", sourceIds))), "تأكيد المستخدم أن البلاغ متوفر؛ ليس تأكيداً حكومياً", emptyList(), sourceIds),
            StepDto("electronic_channel", "مراجعة القناة الإلكترونية للخدمة", "المصدر المراجع يثبت وجود مسار إلكتروني. راجع بطاقة الخدمة الرسمية لتفاصيل التقديم والمتابعة؛ لا يوجد إرسال طلب من التطبيق.",
                "user", listOf("police_report"), checklists(actions = listOf(ChecklistItemDto("review_service_card", "مراجعة بطاقة الخدمة الرسمية",
                    "helpful", null, "unspecified", sourceIds))), "تأكيد المستخدم أنه راجع القناة الرسمية؛ ليس تأكيد إرسال أو موافقة", emptyList(), sourceIds),
        ))

    fun toDomain(response: AssistantResponse): GuidancePlan? = response.plan?.let { plan ->
        GuidancePlan(plan.serviceId, version, plan.title, plan.steps.map { step ->
            PlanStep(step.id, step.title, step.explanation, step.dependsOn,
                step.checklists.flatMap { (category, items) -> items.map { item ->
                    Requirement(item.id, item.label, ChecklistCategory.valueOf(category.uppercase()),
                        RequirementFormat.valueOf(item.format.uppercase()), item.sourceIds, item.necessity, item.condition)
                } }, step.sourceIds, step.placeIds, step.actor, step.completionEvidence)
        }, illustrative = false, summary = plan.summary, route = plan.route,
            uncertainties = response.uncertainties, sourceVersions = sources.filter { it.id in response.sourceIds }.associate { it.id to it.version })
    }
    private fun checklists(documents: List<ChecklistItemDto> = emptyList(), actions: List<ChecklistItemDto> = emptyList()) =
        linkedMapOf("documents" to documents, "actions" to actions, "payments_and_commitments" to emptyList(), "visits" to emptyList())
}

internal fun org.json.JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }
