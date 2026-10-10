package com.a0.daleelak.ai

import com.a0.daleelak.data.ReviewedCatalog

/** Structural and citation validation. The AI derives requirements from documents, not enum gates. */
class ResponseValidator(private val catalog: ReviewedCatalog) {
    fun validate(response: AssistantResponse) {
        require(response.schemaVersion == "1.0")
        require(response.message.isNotBlank())
        require(response.suggestedPrompts.size == 3)
        fun sources(ids: List<String>, required: Boolean = false) {
            require(ids.distinct().size == ids.size) { "Duplicate source IDs" }
            require(!required || ids.isNotEmpty()) { "Procedural content needs a source" }
            require(ids.all { id -> catalog.sources.any { it.id == id } }) { "Unknown source ID" }
            require(ids.all { it in response.sourceIds }) { "Citation missing from response source list" }
        }
        sources(response.sourceIds)
        val facts = response.caseSummary.knownFacts
        require(facts.map { it.key }.distinct().size == facts.size) { "Duplicate fact keys" }
        facts.forEach { fact ->
            require(fact.key.isNotBlank() && fact.value.isNotBlank())
            require(fact.origin in setOf("user", "reviewed_source"))
            sources(fact.sourceIds, required = fact.origin == "reviewed_source")
        }
        require(response.questions.size <= 3 && response.questions.map { it.id }.distinct().size == response.questions.size)
        response.questions.forEach { require(it.id.isNotBlank() && it.text.isNotBlank() && it.reason.isNotBlank()) }
        when (response.kind) {
            ResponseKind.CLARIFICATION -> require(response.plan == null && response.questions.isNotEmpty())
            ResponseKind.UNSUPPORTED -> require(response.plan == null && response.questions.isEmpty())
            ResponseKind.PLAN -> {
                val plan = requireNotNull(response.plan)
                require(response.questions.isEmpty())
                require(plan.serviceId.isNotBlank() && plan.title.isNotBlank() && plan.summary.isNotBlank())
                require(plan.steps.isNotEmpty() && plan.steps.size <= 24)
                sources(response.sourceIds, required = true)
                val ids = plan.steps.map { it.id }
                require(ids.distinct().size == ids.size) { "Duplicate step IDs" }
                val itemIds = plan.steps.flatMap { it.checklists.values.flatten() }.map { it.id }
                require(itemIds.distinct().size == itemIds.size) { "Duplicate checklist IDs" }
                plan.steps.forEach { step ->
                    require(step.id.isNotBlank() && step.title.isNotBlank() && step.explanation.isNotBlank())
                    sources(step.sourceIds, required = true)
                    require(step.dependsOn.all { it in ids && it != step.id }) { "Invalid dependency" }
                    require(step.placeIds.isEmpty()) { "No verified place catalog" }
                    require(step.checklists.keys == ContractCodec.categories.toSet())
                    step.checklists.values.flatten().forEach { item ->
                        require(item.id.isNotBlank() && item.label.isNotBlank())
                        sources(item.sourceIds, required = true)
                        if (item.necessity == "conditional") require(!item.condition.isNullOrBlank())
                    }
                }
                val visiting = mutableSetOf<String>(); val visited = mutableSetOf<String>()
                fun visit(id: String) {
                    if (id in visited) return
                    require(visiting.add(id)) { "Cyclic plan" }
                    plan.steps.first { it.id == id }.dependsOn.forEach(::visit)
                    visiting.remove(id); visited.add(id)
                }
                ids.forEach(::visit)
            }
        }
    }
}
