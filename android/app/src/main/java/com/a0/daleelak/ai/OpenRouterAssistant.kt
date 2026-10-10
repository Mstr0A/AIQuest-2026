package com.a0.daleelak.ai

import com.a0.daleelak.data.ReviewedCatalog
import com.a0.daleelak.domain.AssistantGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.HttpURLConnection
import java.net.URL

/** Document-grounded conversational reasoning with validated references to procedural cards. */
class OpenRouterAssistant(
    private val catalog: ReviewedCatalog,
    private val apiKey: String,
    private val onDecision: ((String) -> Unit)? = null,
) : AssistantGateway {
    override suspend fun respond(request: AssistantRequest): AssistantResponse = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "مفتاح OpenRouter غير متوفر في هذا البناء." }
        val conversation = GroundedConversation(catalog)
        val messages = org.json.JSONArray().put(JSONObject().put("role", "system")
            .put("content", GroundedConversation.SYSTEM_PROMPT + "\nDOCUMENTS:\n" + conversation.context() + "\nRESPONSE_SCHEMA:\n" + catalog.schema.toString()))
        // Keep recent dialogue plus durable accepted state; don't repeatedly send an unbounded local history.
        request.messages.takeLast(24).forEach { item -> messages.put(JSONObject()
            .put("role", if (item.fromUser) "user" else "assistant").put("content", item.text.take(6000))) }
        messages.put(JSONObject().put("role", "user").put("content", JSONObject()
            .put("accepted_state", JSONObject(request.answers))
            .put("active_plan", request.existingPlan?.let { plan -> JSONObject()
                .put("service_id", plan.serviceId).put("title", plan.title)
                .put("step_ids", org.json.JSONArray(plan.steps.map { it.id })) } ?: JSONObject.NULL)
            .put("pending_questions", org.json.JSONArray(request.pendingQuestions.map { it.text }))
            .put("latest_message", request.message.take(6000)).toString()))
        var lastError: Exception? = null
        repeat(2) { attempt ->
            val content = complete(messages)
            onDecision?.invoke(content)
            try { return@withContext conversation.accept(content) }
            catch (error: Exception) {
                lastError = error
                if (attempt == 0) {
                    messages.put(JSONObject().put("role", "assistant").put("content", content))
                    messages.put(JSONObject().put("role", "user").put("content",
                        "Your structured response failed validation: ${error.message}. Correct only the JSON response using reviewed context. Never classify uncertain prerequisites as unsupported; use clarification or a conditional reviewed plan."))
                }
            }
        }
        android.util.Log.w("DaleelakAI", "Grounded response rejected: ${lastError?.message}")
        throw ProviderFailure("تعذر التحقق من رد المساعد. بقيت رسالتك كما هي؛ حاول مجدداً.", lastError)
    }

    private fun complete(messages: org.json.JSONArray): String {
        val connection = (URL("https://openrouter.ai/api/v1/chat/completions").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            val input = JSONObject().put("model", MODEL).put("temperature", 0)
                .put("max_tokens", 16384)
                .put("reasoning", JSONObject().put("effort", "high").put("exclude", true))
                .put("response_format", JSONObject().put("type", "json_schema").put("json_schema", JSONObject()
                    .put("name", "daleelak_response").put("strict", true).put("schema", catalog.schema)))
                .put("messages", messages)
            connection.outputStream.use { it.write(input.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            if (status !in 200..299) {
                val message = when (status) {
                    401, 403 -> "مفتاح OpenRouter غير مقبول. راجعه ثم أعد المحاولة."
                    429 -> "وصلت لحد الاستخدام في OpenRouter. تحقق من رصيد المفتاح أو جرّب لاحقاً."
                    else -> "تعذر الاتصال بمساعد OpenRouter (HTTP $status). بقيت رسالتك كما هي."
                }
                throw ProviderFailure(message)
            }
            val envelope = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val choice = envelope.getJSONArray("choices").getJSONObject(0)
            val content = choice.getJSONObject("message").getString("content")
            android.util.Log.i("DaleelakAI", "Grounded model=$MODEL finish=${choice.optString("finish_reason")} characters=${content.length}")
            require(choice.optString("finish_reason") == "stop") { "Incomplete model response" }
            return content
        } catch (error: ProviderFailure) {
            throw error
        } catch (_: SocketTimeoutException) {
            throw ProviderFailure("انتهت مهلة الاتصال بمساعد OpenRouter. بقيت رسالتك؛ حاول مجدداً.")
        } catch (_: IOException) {
            throw ProviderFailure("تعذر الاتصال بمساعد OpenRouter. تحقق من الإنترنت ثم حاول مجدداً.")
        } catch (error: Exception) {
            android.util.Log.w("DaleelakAI", "Grounded response rejected: ${error.javaClass.simpleName}")
            throw ProviderFailure("تعذر التحقق من رد المساعد. بقيت رسالتك كما هي؛ حاول مجدداً.")
        } finally {
            connection.disconnect()
        }
    }

    private class ProviderFailure(message: String, cause: Throwable? = null) : IOException(message, cause)

    companion object {
        // Reasoning-enabled multilingual conversation; reviewed cards remain the procedural authority.
        const val MODEL = "z-ai/glm-5.3-flash"
    }
}
