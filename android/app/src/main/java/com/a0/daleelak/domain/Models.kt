package com.a0.daleelak.domain

enum class OperationStatus { NEW, ONGOING, COMPLETED }
enum class ChecklistCategory { DOCUMENTS, ACTIONS, PAYMENTS_AND_COMMITMENTS, VISITS }
enum class RequirementFormat { ORIGINAL, PAPER_COPY, DIGITAL_UPLOAD, OFFICIAL_ELECTRONIC_RECORD, UNSPECIFIED }

data class Requirement(
    val id: String,
    val label: String,
    val category: ChecklistCategory,
    val format: RequirementFormat = RequirementFormat.UNSPECIFIED,
    val sourceIds: List<String> = emptyList(),
    val necessity: String = "required",
    val condition: String? = null,
)
data class PlanStep(
    val id: String,
    val title: String,
    val explanation: String,
    val dependsOn: List<String> = emptyList(),
    val requirements: List<Requirement> = emptyList(),
    val sourceIds: List<String> = emptyList(),
    val placeIds: List<String> = emptyList(),
    val actor: String = "user",
    val completionEvidence: String = "تأكيد المستخدم فقط",
)
data class GuidancePlan(
    val serviceId: String,
    val version: String,
    val title: String,
    val steps: List<PlanStep>,
    val illustrative: Boolean = true,
    val summary: String = "",
    val route: String = "undetermined",
    val uncertainties: List<String> = emptyList(),
    val sourceVersions: Map<String, String> = emptyMap(),
)
data class DemoEvent(val reference: String, val message: String, val createdAt: Long)
data class Operation(
    val id: String,
    val title: String,
    val plan: GuidancePlan,
    val answers: Map<String, String> = emptyMap(),
    val completedStepIds: Set<String> = emptySet(),
    val checkedRequirementIds: Set<String> = emptySet(),
    val status: OperationStatus = OperationStatus.NEW,
    val demoEvents: List<DemoEvent> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis(),
    val conversation: List<ChatMessage> = emptyList(),
    val acceptedResponseJson: String? = null,
    val contextResponseJson: String? = null,
)
data class ChatMessage(val text: String, val fromUser: Boolean = false)

// Coordinates/capabilities/hours must come from a reviewed local catalog, never the model.
data class Place(
    val id: String, val name: String, val serviceIds: Set<String>,
    val latitude: Double, val longitude: Double, val mapUrl: String,
    val sourceId: String, val verifiedAt: String,
)

interface AssistantGateway {
    /** Providers must return the v1.0 contract; the app validates before accepting it. */
    suspend fun respond(request: com.a0.daleelak.ai.AssistantRequest): com.a0.daleelak.ai.AssistantResponse
}
interface TranscriptionGateway {
    // Audio remains transient. Return text to the editable composer, never auto-send.
    suspend fun transcribe(audio: ByteArray, mimeType: String): String
}
