package com.a0.daleelak.ai

import com.a0.daleelak.data.ReviewedCatalog

/** Source IDs alone are insufficient: pilot procedural content must match reviewed templates. */
class ResponseValidator(private val catalog: ReviewedCatalog) {
    fun validate(response: AssistantResponse) {
        require(response.suggestedPrompts.size == 3 && response.suggestedPrompts.all { it.isNotBlank() })
        require(response.suggestedPrompts.all { it in catalog.startPrompts || it in catalog.reportQuestion.options ||
            it == "مش متأكد إذا عندي بلاغ" }) { "Unreviewed prompt" }
        fun sources(ids: List<String>) { require(ids.all { it in catalog.sourceIds }) { "Unknown source ID" } }
        sources(response.sourceIds)
        require(response.caseSummary.knownFacts.map { it.key }.distinct().size == response.caseSummary.knownFacts.size)
        response.caseSummary.knownFacts.forEach { fact ->
            sources(fact.sourceIds)
            require(fact.origin == "user" && fact.sourceIds.isEmpty()) { "No additional reviewed case facts are authorized" }
            require(fact.key in setOf("issue", "police_report")) { "Case contains unrelated profile facts" }
            require(when (fact.key) { "issue" -> fact.value in listOf("lost", "damaged", "other"); else -> fact.value in listOf("yes", "no", "unknown") })
        }
        require(response.questions.map { it.id }.distinct().size == response.questions.size)
        response.questions.forEach { question ->
            require(question in catalog.approvedQuestions) { "Question does not have a reviewed purpose" }
            if (question.id == "police_report") require(response.sourceIds.containsAll(catalog.sourceIds))
        }
        when (response.kind) {
            ResponseKind.CLARIFICATION -> require(response.plan == null && response.questions.isNotEmpty())
            ResponseKind.UNSUPPORTED -> require(response.plan == null && response.questions.isEmpty())
            ResponseKind.PLAN -> {
                val plan = requireNotNull(response.plan)
                require(response.questions.isEmpty())
                require(plan.serviceId == catalog.serviceId) { "Unsupported service ID" }
                require(response.caseSummary.knownFacts.any { it.key == "issue" && it.value == "lost" })
                require(response.uncertainties.containsAll(catalog.gaps)) { "Known source gaps cannot be omitted" }
                require(response.uncertainties.all { it in catalog.gaps || it == "توفر بلاغ الشرطة لم يتأكد من المستخدم." ||
                    it == "لا تتوفر قاعدة مراجعة لحالة وثيقة إضافية؛ لا أستطيع إضافة متطلبات أو تغيير الإجراء بناءً عليها." }) { "Unreviewed uncertainty" }
                require(response.sourceIds.containsAll(catalog.sourceIds))
                val ids = plan.steps.map { it.id }
                require(ids.distinct().size == ids.size) { "Duplicate step IDs" }
                val requirementIds = plan.steps.flatMap { it.checklists.values.flatten() }.map { it.id }
                require(requirementIds.distinct().size == requirementIds.size) { "Duplicate checklist IDs" }
                plan.steps.forEach { step ->
                    sources(step.sourceIds); require(step.sourceIds.isNotEmpty())
                    require(step.dependsOn.all { it in ids && it != step.id }) { "Invalid dependency" }
                    require(step.placeIds.size <= 3 && step.placeIds.all { id -> catalog.places.any { it.id == id && plan.serviceId in it.serviceIds } }) { "Unreviewed place" }
                    step.checklists.values.flatten().forEach { item ->
                        sources(item.sourceIds); require(item.sourceIds.isNotEmpty())
                        if (item.necessity == "conditional") require(!item.condition.isNullOrBlank())
                    }
                }
                val visiting = mutableSetOf<String>()
                val visited = mutableSetOf<String>()
                fun visit(id: String) {
                    if (id in visited) return
                    require(visiting.add(id)) { "Cyclic plan" }
                    plan.steps.first { it.id == id }.dependsOn.forEach(::visit)
                    visiting.remove(id); visited.add(id)
                }
                ids.forEach(::visit)
                // Also rejects fabricated requirements, fees, formats, outcomes and allegedly sourced steps.
                require(plan == catalog.supportedPlan) { "Procedural content is not in the supplied reviewed record" }
            }
        }
    }
}
