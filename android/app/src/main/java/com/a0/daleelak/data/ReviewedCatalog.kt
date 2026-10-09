package com.a0.daleelak.data

import android.content.res.AssetManager
import com.a0.daleelak.ai.*
import com.a0.daleelak.domain.*
import org.json.JSONObject

data class ReviewedSource(
    val id: String, val title: String, val url: String, val version: String,
    val accessedAt: String, val provenance: String, val reviewedExcerpt: String,
    val supportedRules: List<String>, val gaps: List<String>,
)

/** Only the supplied source summary is bundled; no remote document is fetched at runtime. */
class ReviewedCatalog(assets: AssetManager) {
    val schema: JSONObject = JSONObject(assets.open("guidance/ai-response.schema.json").bufferedReader().use { it.readText() })
    private val root = JSONObject(assets.open("guidance/reviewed-sources.json").bufferedReader().use { it.readText() })
    val version: String = root.getString("version")
    val serviceId: String = root.getString("service_id")
    val sources: List<ReviewedSource> = root.getJSONArray("sources").let { entries ->
        (0 until entries.length()).map { index -> entries.getJSONObject(index).let { source ->
            ReviewedSource(source.getString("id"), source.getString("title"), source.getString("url"),
                source.getString("version"), source.getString("accessed_at"), source.getString("provenance"),
                source.getString("reviewed_excerpt"), source.getJSONArray("supported_rules").strings(), source.getJSONArray("gaps").strings())
        } }
    }
    val places: List<Place> = emptyList()
    val sourceIds = sources.map { it.id }
    val gaps = sources.flatMap { it.gaps }
    val startPrompts = listOf("ضاع دفتر العيلة، شو أعمل؟", "شو الأوراق المطلوبة لتعويض دفتر العائلة؟", "عندي بلاغ فقدان، شو الخطوة الجاية؟")
    val reportQuestion = ClarificationQuestion("police_report", "هل عندك بلاغ فقدان من الشرطة؟",
        listOf("عندي بلاغ فقدان", "لسه ما عندي بلاغ", "مش متأكد"),
        "المصدر المراجع يذكر البلاغ كمتطلب سابق؛ الإجابة تحدد إذا نبدأ بالبلاغ أو بالقناة الإلكترونية.")
    val goalQuestion = ClarificationQuestion("issue", "هل المطلوب تعويض دفتر عائلة مفقود؟",
        listOf("نعم، دفتر عائلة مفقود", "دفتر تالف", "خدمة ثانية"), "لتحديد إذا المشكلة ضمن الخدمة التي تغطيها الوثائق المراجعة.")
    val approvedQuestions = listOf(goalQuestion, reportQuestion)

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
            uncertainties = response.uncertainties, sourceVersions = sources.associate { it.id to it.version })
    }
    private fun checklists(documents: List<ChecklistItemDto> = emptyList(), actions: List<ChecklistItemDto> = emptyList()) =
        linkedMapOf("documents" to documents, "actions" to actions, "payments_and_commitments" to emptyList(), "visits" to emptyList())
}

internal fun org.json.JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }
