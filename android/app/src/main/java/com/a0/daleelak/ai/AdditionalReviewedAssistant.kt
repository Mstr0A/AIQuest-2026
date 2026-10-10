package com.a0.daleelak.ai

import com.a0.daleelak.data.ReviewedCatalog
import java.util.Locale

/** Routes three separately reviewed historical service cards; never authors new procedures. */
class AdditionalReviewedAssistant(private val catalog: ReviewedCatalog, private val matchMessageIntent: Boolean = true) {
    fun respond(request: AssistantRequest): AssistantResponse? {
        val text = request.message.lowercase(Locale.ROOT).replace('أ', 'ا').replace('إ', 'ا').replace('ة', 'ه')
        if (matchMessageIntent && ("جامع" in text || "توجيهي" in text || "university" in text || "high school" in text)) return null
        val explicit = if (matchMessageIntent) when {
            ("جواز" in text || "passport" in text) && listOf("ضاع", "ضايع", "مفقود", "فقد", "lost").any { it in text } -> "lost_passport"
            "شهاده" in text && ("ولاد" in text || "ميلاد" in text) || "birth certificate" in text -> "birth_certificate"
            ("صدق" in text || "تصديق" in text || "attest" in text || "certif" in text) && ("وثيق" in text || "صوره" in text || "copy" in text || "document" in text) -> "document_attestation"
            "عنوان" in text && ("مصرح" in text || "تبليغ" in text) || "declared address" in text -> "declared_address"
            else -> null
        }
        else null
        val id = explicit ?: request.answers["issue"]
        val service = catalog.additionalServices.firstOrNull { it.id == id } ?: return null
        val answers = request.answers.filterKeys { it == "issue" || it == service.requiredFact }.toMutableMap()
        answers["issue"] = service.id
        val pending = request.pendingQuestions.any { it.id == service.requiredFact }
        // Offline fallback for explicit statements. The live classifier supplies the same bounded facts.
        val value = when (service.id) {
            "lost_passport" -> when {
                "مؤقت" in text || "temporary" in text -> "temporary"
                "مش متاكد" in text || "ما بعرف" in text || "not sure" in text -> "unknown"
                "عادي" in text || "ordinary" in text || "regular" in text -> "ordinary"
                else -> null
            }
            "birth_certificate" -> when {
                "غير مسجل" in text || "مش مسجل" in text || "not registered" in text -> "no"
                "مش متاكد" in text || "not sure" in text -> "unknown"
                "مسجل" in text && "حاسوب" in text || "already registered" in text -> "yes"
                pending && text.trim() in setOf("نعم", "yes") -> "yes"
                pending && text.trim() in setOf("لا", "no") -> "no"
                else -> null
            }
            "document_attestation" -> when {
                "ترجمه" in text || "translation" in text -> "translation"
                "جهه ثانيه" in text || "another agency" in text -> "other"
                "مش متاكد" in text -> "unknown"
                "صادر" in text && "احوال" in text || "issued by cspd" in text || pending && text.trim() == "نعم" -> "cspd"
                else -> null
            }
            else -> when {
                "مش متاكد" in text -> "unknown"
                "مش رب" in text || pending && text.trim() == "لا" -> "other"
                "رب الاسره" in text || "انوب" in text || "head of household" in text -> "authorized"
                else -> null
            }
        }
        // Do not let the fallback override a fact already extracted from this turn by the live model.
        if (matchMessageIntent && value != null && request.answers[service.requiredFact] == null) answers[service.requiredFact] = value
        val facts = answers.map { IssueFact(it.key, it.value) }
        val answer = answers[service.requiredFact]
        val summary = CaseSummary(service.response.plan!!.title, facts,
            if (answer == null) listOf(service.question.text) else emptyList())
        if (answer == null) return service.response.copy(kind = ResponseKind.CLARIFICATION,
            message = service.question.text, caseSummary = summary, questions = listOf(service.question), plan = null)
        if (answer != service.supportedValue) return service.response.copy(kind = ResponseKind.UNSUPPORTED,
            message = "آسف، ما عندي مسار موثق يغطي تفاصيل هذه الحالة. راجع الجهة الرسمية لتحديد الإجراء الصحيح.", caseSummary = summary, plan = null)
        return service.response.copy(caseSummary = summary)
    }
}
