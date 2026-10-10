package com.a0.daleelak.ai

import com.a0.daleelak.data.ReviewedCatalog
import com.a0.daleelak.domain.AssistantGateway
import java.util.Locale

/** Bounded, offline intent matcher pending provider choice; not advertised as live AI. */
class LocalReviewedAssistant(private val catalog: ReviewedCatalog) : AssistantGateway {
    override suspend fun respond(request: AssistantRequest): AssistantResponse {
        AdditionalReviewedAssistant(catalog).respond(request)?.let { return it }
        val text = normalize(request.message)
        val answers = request.answers.toMutableMap()
        val mentionsBook = ("دفتر" in text && ("عائل" in text || "عيل" in text)) || "family book" in text || "family booklet" in text
        val pendingIssue = request.pendingQuestions.any { it.id == "issue" }
        val pendingReport = request.pendingQuestions.any { it.id == "police_report" }
        val lost = listOf("ضاع", "ضايع", "مفقود", "فقدان", "lost", "loss", "فقدت").any { it in text }
        val damaged = listOf("تالف", "تلف", "damaged").any { it in text }
        when {
            damaged && (mentionsBook || answers["issue"] == "lost" || pendingIssue) -> answers["issue"] = "damaged"
            mentionsBook && lost -> answers["issue"] = "lost"
            pendingIssue && lost -> answers["issue"] = "lost"
            pendingIssue && text in setOf("نعم", "اه", "yes") -> answers["issue"] = "lost"
            listOf("خدمه ثانيه", "خدمة ثانية", "passport", "جواز", "رخصه", "بطاقه", "هويه").any { it in text } -> {
                // Another lost identity document alongside the pilot is a source gap, not a guessed branch.
                if (mentionsBook || answers["issue"] == "lost") return partialWithGap(answers,
                    "لا تتوفر قاعدة مراجعة لحالة وثيقة إضافية؛ لا أستطيع إضافة متطلبات أو تغيير الإجراء بناءً عليها.")
                answers["issue"] = "other"
            }
            mentionsBook && !lost && answers["issue"] == null -> Unit
        }
        if ("بلاغ" in text || "police report" in text || pendingReport) {
            val report = when {
                listOf("مش متاكد", "مو متاكد", "ما بعرف", "not sure", "unknown").any { it in text } -> "unknown"
                listOf("ما عندي", "بدون", "ما معي", "لسه", "don't have", "do not have", "no report").any { it in text } || text == "لا" || text == "no" -> "no"
                listOf("عندي", "معي", "جهزت", "have", "نعم").any { it in text } || text == "yes" -> "yes"
                else -> null
            }
            if (report != null) answers["police_report"] = report
        }
        val summary = summary(answers)
        if (answers["issue"] == "lost" && listOf("رسوم", "fee", "اطبع", "print", "صوره", "portrait", "خارج", "abroad", "قبل هيك", "repeat").any { it in text }) {
            return partialWithGap(answers)
        }
        if (answers["issue"] in listOf("damaged", "other")) return AssistantResponse(kind = ResponseKind.UNSUPPORTED,
            message = "لا يوجد مسار مراجع لهذه الخدمة في السجل المرفق.",
            caseSummary = summary, suggestedPrompts = catalog.startPrompts,
            uncertainties = listOf("لا يوجد سجل قواعد مراجع للخدمة المطلوبة."))
        if (answers["issue"] != "lost") return AssistantResponse(kind = ResponseKind.CLARIFICATION,
            message = "خلينا نحدد الخدمة التي تحتاجها ضمن الوثائق المراجعة.", caseSummary = summary,
            questions = listOf(catalog.goalQuestion), suggestedPrompts = catalog.startPrompts)
        if (answers["police_report"] == null) return AssistantResponse(kind = ResponseKind.CLARIFICATION,
            message = catalog.reportQuestion.text + "\n" + catalog.reportQuestion.reason, caseSummary = summary,
            questions = listOf(catalog.reportQuestion), suggestedPrompts = catalog.reportQuestion.options,
            sourceIds = catalog.sourceIds, uncertainties = catalog.gaps)
        return partialWithGap(answers)
    }

    private fun partialWithGap(answers: Map<String, String>, extraGap: String? = null): AssistantResponse {
        if (answers["issue"] != "lost") return AssistantResponse(kind = ResponseKind.CLARIFICATION,
            message = catalog.goalQuestion.text, caseSummary = summary(answers), questions = listOf(catalog.goalQuestion), suggestedPrompts = catalog.startPrompts)
        val next = if (answers["police_report"] == "yes") "حسب إجابتك البلاغ متوفر؛ الجزء التالي الموثق هو مراجعة القناة الإلكترونية."
            else "المتطلب السابق الموثق هو بلاغ الشرطة؛ صيغة تقديمه غير مثبتة في السجل المتاح."
        return AssistantResponse(kind = ResponseKind.PLAN,
            message = "$next\n${catalog.supportedPlan.summary}\n${(catalog.gaps + listOfNotNull(extraGap)).joinToString("\n")}",
            caseSummary = summary(answers), plan = catalog.supportedPlan,
            suggestedPrompts = listOf("عندي بلاغ فقدان", "لسه ما عندي بلاغ", "مش متأكد إذا عندي بلاغ"),
            sourceIds = catalog.sourceIds, uncertainties = catalog.gaps + listOfNotNull(extraGap) +
                if (answers["police_report"] == "unknown" || answers["police_report"] == null) listOf("توفر بلاغ الشرطة لم يتأكد من المستخدم.") else emptyList())
    }
    private fun summary(answers: Map<String, String>) = CaseSummary(when (answers["issue"]) {
        "lost" -> "تعويض دفتر عائلة مفقود"
        "damaged" -> "دفتر عائلة تالف؛ خارج السجل المراجع المتاح"
        "other" -> "خدمة خارج السجل المراجع المتاح"
        else -> "تحديد المشكلة المطلوبة"
    },
        answers.filterKeys { it in setOf("issue", "police_report") }.map { IssueFact(it.key, it.value) },
        if (answers["issue"] == null) listOf("نوع المشكلة") else if (answers["issue"] == "lost" && answers["police_report"] == null) listOf("توفر بلاغ الشرطة") else emptyList())
    private fun normalize(text: String) = text.lowercase(Locale.ROOT).replace(Regex("[\u064B-\u065F\u0670]"), "")
        .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ة', 'ه').trim()
}
