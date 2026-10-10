package com.a0.daleelak.ai

import com.a0.daleelak.data.ReviewedCatalog

/** Retain accepted conversational text; source-validated procedural cards are resolved before rendering. */
object ResponsePresentation {
    @Suppress("UNUSED_PARAMETER")
    fun message(response: AssistantResponse, catalog: ReviewedCatalog): String = buildString {
        append(response.message)
        if (response.kind == ResponseKind.CLARIFICATION) response.questions.forEach { question ->
            if (!response.message.contains(question.text)) append("\n${question.text}")
        }
        if (response.kind == ResponseKind.PLAN) response.uncertainties.forEach { gap ->
            if (!response.message.contains(gap)) append("\n$gap")
        }
    }
}
