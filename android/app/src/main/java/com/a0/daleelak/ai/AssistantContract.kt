package com.a0.daleelak.ai

import com.a0.daleelak.domain.ChatMessage
import com.a0.daleelak.domain.GuidancePlan

data class IssueFact(val key: String, val value: String, val origin: String = "user", val sourceIds: List<String> = emptyList())
data class CaseSummary(val goal: String, val knownFacts: List<IssueFact>, val unresolvedFacts: List<String>)
data class ClarificationQuestion(val id: String, val text: String, val options: List<String>, val reason: String)
data class ChecklistItemDto(
    val id: String, val label: String, val necessity: String, val condition: String?,
    val format: String, val sourceIds: List<String>,
)
data class StepDto(
    val id: String, val title: String, val explanation: String, val actor: String,
    val dependsOn: List<String>, val checklists: Map<String, List<ChecklistItemDto>>,
    val completionEvidence: String, val placeIds: List<String>, val sourceIds: List<String>,
)
data class PlanDto(val title: String, val serviceId: String, val route: String, val summary: String, val steps: List<StepDto>)
enum class ResponseKind(val wireValue: String) { CLARIFICATION("clarification"), PLAN("plan"), UNSUPPORTED("unsupported") }
data class AssistantResponse(
    val schemaVersion: String = "1.0", val kind: ResponseKind, val message: String,
    val caseSummary: CaseSummary, val questions: List<ClarificationQuestion> = emptyList(),
    val plan: PlanDto? = null, val suggestedPrompts: List<String>,
    val sourceIds: List<String> = emptyList(), val uncertainties: List<String> = emptyList(),
)
data class AssistantRequest(
    val message: String, val messages: List<ChatMessage>, val answers: Map<String, String>,
    val existingPlan: GuidancePlan?, val pendingQuestions: List<ClarificationQuestion>,
    val preferredLanguage: String = "ar",
)
