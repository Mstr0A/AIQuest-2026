package com.a0.daleelak.ai

import com.a0.daleelak.data.ReviewedCatalog

/** Render factual text from reviewed records; arbitrary provider prose is not procedural evidence. */
object ResponsePresentation {
    fun message(response: AssistantResponse, catalog: ReviewedCatalog): String = when (response.kind) {
        ResponseKind.CLARIFICATION -> response.questions.joinToString("\n") { "${it.text}\n${it.reason}" }
        ResponseKind.UNSUPPORTED -> "لا تتضمن الوثائق المراجعة مساراً لهذه المشكلة. التغطية الحالية لدفتر العائلة المفقود فقط."
        ResponseKind.PLAN -> {
            val report = response.caseSummary.knownFacts.firstOrNull { it.key == "police_report" }?.value
            val next = if (report == "yes") "حسب إجابتك البلاغ متوفر؛ الخطوة التالية في الجزء الموثق هي مراجعة القناة الإلكترونية."
                else "المتطلب السابق الموثق هو بلاغ الشرطة؛ صيغة تقديمه غير مثبتة في السجل المتاح."
            "$next\n${catalog.supportedPlan.summary}\n${response.uncertainties.joinToString("\n") }"
        }
    }
}
