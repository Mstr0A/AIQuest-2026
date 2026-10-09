package com.a0.daleelak.ai

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

/** Validator for the keyword subset used by the bundled, unchanged v1.0 schema. */
class ContractCodec(private val schema: JSONObject) {
    fun decode(raw: String): AssistantResponse {
        require(raw.length <= 131_072) { "Response too large" }
        val tokenizer = JSONTokener(raw)
        val root = tokenizer.nextValue() as? JSONObject ?: error("Expected one JSON object")
        require(tokenizer.nextClean() == '\u0000') { "Trailing response content" }
        validate(root, schema, "response", 0)
        val summary = root.getJSONObject("case_summary")
        return AssistantResponse(root.getString("schema_version"),
            ResponseKind.entries.first { it.wireValue == root.getString("kind") }, root.getString("message"),
            CaseSummary(summary.getString("goal"), summary.getJSONArray("known_facts").objects().map { fact ->
                IssueFact(fact.getString("key"), fact.getString("value"), fact.getString("origin"), fact.getJSONArray("source_ids").strings())
            }, summary.getJSONArray("unresolved_facts").strings()),
            root.getJSONArray("questions").objects().map { question -> ClarificationQuestion(question.getString("id"),
                question.getString("text"), question.getJSONArray("options").strings(), question.getString("reason")) },
            if (root.isNull("plan")) null else root.getJSONObject("plan").let { plan ->
                PlanDto(plan.getString("title"), plan.getString("service_id"), plan.getString("route"), plan.getString("summary"),
                    plan.getJSONArray("steps").objects().map { step ->
                        val checklists = step.getJSONObject("checklists")
                        StepDto(step.getString("id"), step.getString("title"), step.getString("explanation"), step.getString("actor"),
                            step.getJSONArray("depends_on").strings(), categories.associateWith { category ->
                                checklists.getJSONArray(category).objects().map { item -> ChecklistItemDto(item.getString("id"), item.getString("label"),
                                    item.getString("necessity"), if (item.isNull("condition")) null else item.getString("condition"),
                                    item.getString("format"), item.getJSONArray("source_ids").strings()) }
                            }, step.getString("completion_evidence"), step.getJSONArray("place_ids").strings(), step.getJSONArray("source_ids").strings())
                    })
            }, root.getJSONArray("suggested_prompts").strings(), root.getJSONArray("source_ids").strings(), root.getJSONArray("uncertainties").strings())
    }

    fun encode(response: AssistantResponse): String = JSONObject()
        .put("schema_version", response.schemaVersion).put("kind", response.kind.wireValue).put("message", response.message)
        .put("case_summary", JSONObject().put("goal", response.caseSummary.goal)
            .put("known_facts", array(response.caseSummary.knownFacts.map { fact -> JSONObject().put("key", fact.key)
                .put("value", fact.value).put("origin", fact.origin).put("source_ids", JSONArray(fact.sourceIds)) }))
            .put("unresolved_facts", JSONArray(response.caseSummary.unresolvedFacts)))
        .put("questions", array(response.questions.map { question -> JSONObject().put("id", question.id).put("text", question.text)
            .put("options", JSONArray(question.options)).put("reason", question.reason) }))
        .put("plan", response.plan?.let { plan -> JSONObject().put("title", plan.title).put("service_id", plan.serviceId)
            .put("route", plan.route).put("summary", plan.summary).put("steps", array(plan.steps.map { step ->
                val checklists = JSONObject()
                step.checklists.forEach { (category, items) -> checklists.put(category, array(items.map { item -> JSONObject()
                    .put("id", item.id).put("label", item.label).put("necessity", item.necessity)
                    .put("condition", item.condition ?: JSONObject.NULL).put("format", item.format).put("source_ids", JSONArray(item.sourceIds)) })) }
                JSONObject().put("id", step.id).put("title", step.title).put("explanation", step.explanation).put("actor", step.actor)
                    .put("depends_on", JSONArray(step.dependsOn)).put("checklists", checklists).put("completion_evidence", step.completionEvidence)
                    .put("place_ids", JSONArray(step.placeIds)).put("source_ids", JSONArray(step.sourceIds))
            })) } ?: JSONObject.NULL)
        .put("suggested_prompts", JSONArray(response.suggestedPrompts)).put("source_ids", JSONArray(response.sourceIds))
        .put("uncertainties", JSONArray(response.uncertainties)).toString()

    private fun validate(value: Any?, rule: JSONObject, path: String, depth: Int) {
        require(depth < 40) { "Response too deeply nested" }
        if (rule.has("\$ref")) {
            val reference = rule.getString("\$ref")
            require(reference.startsWith("#/\$defs/")) { "Unsupported schema reference" }
            validate(value, schema.getJSONObject("\$defs").getJSONObject(reference.substringAfterLast('/')), path, depth + 1)
            return
        }
        if (rule.has("anyOf")) {
            val alternatives = rule.getJSONArray("anyOf")
            require((0 until alternatives.length()).any { index ->
                runCatching { validate(value, alternatives.getJSONObject(index), path, depth + 1) }.isSuccess
            }) { "$path does not match any allowed shape" }
            return
        }
        val type = when (value) { null, JSONObject.NULL -> "null"; is JSONObject -> "object"; is JSONArray -> "array"; is String -> "string"; else -> "other" }
        val types = rule.opt("type")
        require(if (types is JSONArray) types.strings().contains(type) else types == type) { "$path has wrong type" }
        if (rule.has("enum")) {
            val allowed = rule.getJSONArray("enum")
            require((0 until allowed.length()).any { allowed.get(it) == value }) { "$path has unsupported value" }
        }
        when (value) {
            is JSONObject -> {
                val properties = rule.getJSONObject("properties")
                rule.getJSONArray("required").strings().forEach { require(value.has(it)) { "$path missing $it" } }
                if (!rule.optBoolean("additionalProperties", true)) require(value.keys().asSequence().all { properties.has(it) }) { "$path has extra keys" }
                value.keys().forEach { key -> if (properties.has(key)) validate(value.get(key), properties.getJSONObject(key), "$path.$key", depth + 1) }
            }
            is JSONArray -> {
                require(value.length() >= rule.optInt("minItems", 0) && value.length() <= rule.optInt("maxItems", Int.MAX_VALUE)) { "$path has wrong item count" }
                if (rule.optBoolean("uniqueItems")) require((0 until value.length()).map { value.get(it).toString() }.distinct().size == value.length()) { "$path contains duplicates" }
                (0 until value.length()).forEach { validate(value.get(it), rule.getJSONObject("items"), "$path[$it]", depth + 1) }
            }
            is String -> require(value.codePointCount(0, value.length) >= rule.optInt("minLength", 0)) { "$path is empty" }
        }
    }
    private fun array(objects: List<JSONObject>) = JSONArray().also { result -> objects.forEach { result.put(it) } }
    private fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
    private fun JSONArray.strings() = (0 until length()).map { getString(it) }
    companion object { val categories = listOf("documents", "actions", "payments_and_commitments", "visits") }
}
