package com.a0.daleelak.domain

/** Preserve work only when the task's meaning, requirements and dependencies are unchanged. */
object PlanReconciler {
    fun reconcile(operation: Operation, next: GuidancePlan, nextAnswers: Map<String, String> = operation.answers): Operation {
        require(operation.plan.serviceId == next.serviceId) { "Cannot replace an operation with another service" }
        val old = operation.plan.steps.associateBy { it.id }
        val unchanged = next.steps.filter { step -> old[step.id] == step }.map { it.id }.toSet()
        val reportCorrected = operation.answers["police_report"] != nextAnswers["police_report"] &&
            nextAnswers["police_report"] in listOf("no", "unknown")
        val invalidated = if (reportCorrected) setOf("police_report") else emptySet()
        var completed = operation.completedStepIds.intersect(unchanged) - invalidated
        while (true) {
            val invalid = next.steps.filter { it.id in completed && !completed.containsAll(it.dependsOn) }.map { it.id }.toSet()
            if (invalid.isEmpty()) break
            completed = completed - invalid
        }
        val oldRequirements = operation.plan.steps.flatMap { it.requirements }.associateBy { it.id }
        val stableRequirements = next.steps.filterNot { it.id in invalidated }.flatMap { it.requirements }
            .filter { oldRequirements[it.id] == it }.map { it.id }.toSet()
        val changed = completed != operation.completedStepIds || next.steps != operation.plan.steps
        return operation.copy(plan = next, title = next.title, completedStepIds = completed,
            checkedRequirementIds = operation.checkedRequirementIds.intersect(stableRequirements),
            status = if (changed && operation.status == OperationStatus.COMPLETED) OperationStatus.ONGOING else operation.status)
    }
}
