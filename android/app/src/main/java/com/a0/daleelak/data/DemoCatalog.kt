package com.a0.daleelak.data

import com.a0.daleelak.domain.*

object DemoCatalog {
    val prompts = listOf("ضاع دفتر العيلة، شو أعمل؟", "شو الأوراق المطلوبة لتعويض دفتر العائلة؟", "عندي بلاغ فقدان، شو الخطوة الجاية؟")
    // Layout fixture only. Replace with source-reviewed records before procedural guidance.
    val plan = GuidancePlan("lost_family_book", "layout-demo-1", "تعويض دفتر عائلة مفقود", listOf(
        PlanStep("case", "١. مراجعة الحالة", "راجع إجاباتك قبل متابعة الدليل التجريبي.", requirements = listOf(
            Requirement("review_answers", "راجعت ملخص الحالة", ChecklistCategory.ACTIONS))),
        PlanStep("requirements", "٢. مراجعة المتطلبات", "ستظهر هنا المتطلبات الموثقة ومراجعها بعد ربط بيانات الخدمة.", listOf("case")),
        PlanStep("channel", "٣. متابعة القناة الرسمية", "مكان مخصص لتعليمات القناة الرسمية؛ لا يتم إرسال أي طلب من هذا النموذج.", listOf("requirements")),
    ))
    val places: List<Place> = emptyList() // No fabricated centres, distances or opening hours.
}
