package com.a0.daleelak.ai

/** Coverage explanations authored from the reviewed CSPD guide, never procedural guesses. */
object UnsupportedGuidance {
    const val EDUCATION = "آسف، دليل الأحوال المدنية المتاح ما يغطي توثيق شهادات الجامعة أو التوجيهي. ما عندي مصدر موثق يحدد الجهة والخطوات لهذه المعاملة."
    const val PASSPORT = "آسف، دليل 2024 فيه خدمة جواز السفر بدل فاقد، لكن تفاصيل هذا المسار لسه مش محملة ضمن إجراءات التطبيق. ما بقدر أعطيك خطوات كاملة موثوقة حالياً."
    const val GENERIC = "آسف، ما عندي تعليمات موثقة كافية لهذه الحالة، فما بقدر أعطيك خطوات موثوقة. راجع الجهة الرسمية المسؤولة عن معاملتك."
    fun forMessage(message: String): String {
        val text = message.lowercase()
        return when {
            listOf("جامعة", "الجامعه", "جامعي", "توجيهي", "university", "graduation", "high school").any { it in text } -> EDUCATION
            ("جواز" in text || "passport" in text) && listOf("ضاع", "ضايع", "مفقود", "فاقد", "lost").any { it in text } -> PASSPORT
            else -> GENERIC
        }
    }
    fun acceptedExplanation(uncertainties: List<String>) = uncertainties.firstOrNull { it == EDUCATION || it == PASSPORT } ?: GENERIC
}
