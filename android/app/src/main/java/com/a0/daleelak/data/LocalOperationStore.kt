package com.a0.daleelak.data

import android.content.Context
import com.a0.daleelak.domain.*
import org.json.JSONArray
import org.json.JSONObject

class LocalOperationStore(context: Context) {
    private val preferences = context.getSharedPreferences("daleelak_operations_v1", Context.MODE_PRIVATE)

    fun load(): List<Operation> {
        val raw = preferences.getString("operations", null) ?: return emptyList()
        val root = JSONObject(raw)
        require(root.getInt("version") == 1) { "Unsupported local data version" }
        val entries = root.getJSONArray("operations")
        return (0 until entries.length()).map { index ->
            val entry = entries.getJSONObject(index)
            val plan = entry.getJSONObject("plan")
            val steps = plan.getJSONArray("steps")
            val answers = entry.getJSONObject("answers")
            val events = entry.getJSONArray("demoEvents")
            Operation(
                id = entry.getString("id"), title = entry.getString("title"),
                plan = GuidancePlan(plan.getString("serviceId"), plan.getString("version"), plan.getString("title"),
                    (0 until steps.length()).map { stepIndex ->
                        val step = steps.getJSONObject(stepIndex)
                        val requirements = step.getJSONArray("requirements")
                        PlanStep(step.getString("id"), step.getString("title"), step.getString("explanation"),
                            step.getJSONArray("dependsOn").strings(),
                            (0 until requirements.length()).map { itemIndex ->
                                val item = requirements.getJSONObject(itemIndex)
                                Requirement(item.getString("id"), item.getString("label"),
                                    ChecklistCategory.valueOf(item.getString("category")),
                                    RequirementFormat.valueOf(item.getString("format")), item.getJSONArray("sourceIds").strings())
                            }, step.getJSONArray("sourceIds").strings(), step.getJSONArray("placeIds").strings())
                    }, plan.getBoolean("illustrative")),
                answers = answers.keys().asSequence().associateWith { answers.getString(it) },
                completedStepIds = entry.getJSONArray("completedSteps").strings().toSet(),
                checkedRequirementIds = entry.getJSONArray("checkedRequirements").strings().toSet(),
                status = OperationStatus.valueOf(entry.getString("status")),
                demoEvents = (0 until events.length()).map { eventIndex ->
                    val event = events.getJSONObject(eventIndex)
                    DemoEvent(event.getString("reference"), event.getString("message"), event.getLong("createdAt"))
                }, updatedAt = entry.getLong("updatedAt"))
        }
    }

    fun save(operations: List<Operation>) {
        val entries = JSONArray()
        operations.forEach { operation ->
            val steps = JSONArray()
            operation.plan.steps.forEach { step ->
                val requirements = JSONArray()
                step.requirements.forEach { item -> requirements.put(JSONObject()
                    .put("id", item.id).put("label", item.label).put("category", item.category.name)
                    .put("format", item.format.name).put("sourceIds", JSONArray(item.sourceIds))) }
                steps.put(JSONObject().put("id", step.id).put("title", step.title).put("explanation", step.explanation)
                    .put("dependsOn", JSONArray(step.dependsOn)).put("requirements", requirements)
                    .put("sourceIds", JSONArray(step.sourceIds)).put("placeIds", JSONArray(step.placeIds)))
            }
            val events = JSONArray()
            operation.demoEvents.forEach { events.put(JSONObject().put("reference", it.reference)
                .put("message", it.message).put("createdAt", it.createdAt)) }
            entries.put(JSONObject().put("id", operation.id).put("title", operation.title)
                .put("plan", JSONObject().put("serviceId", operation.plan.serviceId).put("version", operation.plan.version)
                    .put("title", operation.plan.title).put("illustrative", operation.plan.illustrative).put("steps", steps))
                .put("answers", JSONObject(operation.answers)).put("completedSteps", JSONArray(operation.completedStepIds.toList()))
                .put("checkedRequirements", JSONArray(operation.checkedRequirementIds.toList()))
                .put("status", operation.status.name).put("demoEvents", events).put("updatedAt", operation.updatedAt))
        }
        check(preferences.edit().putString("operations", JSONObject().put("version", 1).put("operations", entries).toString()).commit()) {
            "Could not persist operations"
        }
    }

    private fun JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }
}
