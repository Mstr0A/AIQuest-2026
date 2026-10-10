package com.a0.daleelak

import com.a0.daleelak.ai.*
import com.a0.daleelak.data.ReviewedCatalog
import com.a0.daleelak.domain.ChatMessage
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/** Real app gateway tests: semantic document selection, not predetermined question/step scripts. */
class ThreeServiceLiveTest {
    private fun catalog(): ReviewedCatalog {
        val assets = File("src/main/assets/guidance")
        return ReviewedCatalog.fromReviewedJson(File(assets, "ai-response.schema.json").readText(),
            File(assets, "reviewed-sources.json").readText(), File(assets, "additional-services.json").readText())
    }
    private class Dialogue(val catalog: ReviewedCatalog, val name: String) {
        var messages = emptyList<ChatMessage>()
        var facts = emptyMap<String, String>()
        var questions = emptyList<ClarificationQuestion>()
        val evidence = JSONArray()
        var decision = 0
        val gateway = OpenRouterAssistant(catalog, BuildConfig.OPENROUTER_DEMO_KEY) { raw ->
            val output = File("build/document-ai-debug"); output.mkdirs()
            File(output, "$name-${++decision}.txt").writeText(raw)
        }
        suspend fun turn(text: String): AssistantResponse {
            val codec = ContractCodec(catalog.schema)
            val r = codec.decode(codec.encode(gateway.respond(AssistantRequest(text, messages, facts, null, questions))))
            ResponseValidator(catalog).validate(r)
            facts = r.caseSummary.knownFacts.filter { it.origin == "user" }.associate { it.key to it.value }
            questions = r.questions
            messages = messages + ChatMessage(text, true) + ChatMessage(ResponsePresentation.message(r, catalog))
            evidence.put(JSONObject().put("input", text).put("response", JSONObject(codec.encode(r))))
            val output = File("build/document-ai-evaluation"); output.mkdirs()
            File(output, "$name.json").writeText(evidence.toString(2))
            return r
        }
    }
    private fun paid() {
        assumeTrue(System.getenv("DALEELAK_ALLOW_PAID_SERVICE_TESTS") == "true")
        assertTrue(BuildConfig.OPENROUTER_DEMO_KEY.isNotBlank())
    }
    @Test fun englishAndMixedArabicUseTheDominantLanguage() = runBlocking {
        paid(); val d = Dialogue(catalog(), "response-language")
        val english = d.turn("I am Jordanian and need my birth certificate. My birth is already registered in the civil status computer system. Please show me the steps and required documents in English.")
        assertEquals(english.message, ResponseKind.PLAN, english.kind)
        assertTrue(english.message.any { it in 'a'..'z' })
        assertFalse(english.message.any { it in '\u0600'..'\u06ff' })
        assertTrue(english.uncertainties.none { it.any { c -> c in '\u0600'..'\u06ff' } })
        val mixed = d.turn("أنا أردني وبدي birth certificate إلي، ولادتي مسجلة بحاسوب الأحوال. ورجيني الخطوات والأوراق والرسوم.")
        assertEquals(mixed.message, ResponseKind.PLAN, mixed.kind)
        assertTrue(mixed.message.count { it in '\u0600'..'\u06ff' } > mixed.message.count { it in 'a'..'z' || it in 'A'..'Z' })
    }
    @Test fun lostNationalIdReachesDocumentSteps() = runBlocking {
        paid(); val d = Dialogue(catalog(), "lost-national-id")
        val start = d.turn("هويتي ضاعت، شو أعمل؟")
        assertTrue(start.message, start.kind != ResponseKind.UNSUPPORTED)
        val plan = d.turn("بطاقتي الشخصية الأردنية الذكية، أول مرة بتضيع وأنا بالغ، مش عسكري وما عندي بطاقة جسور. ورجيني الخطوات والأوراق والرسوم حسب الدليل.")
        assertEquals(plan.message, ResponseKind.PLAN, plan.kind)
        assertTrue(plan.sourceIds.contains("cspd-2024-lost_national_id"))
        assertTrue(plan.plan!!.steps.size >= 4)
        val text = ContractCodec(d.catalog.schema).encode(plan)
        assertTrue(text.contains("5") || text.contains("٥") || text.contains("خمسة"))
        assertTrue(text.contains("أسبوعين") || text.contains("اسبوعين") || text.contains("14") || text.contains("١٤"))
        assertNotNull(d.catalog.toDomain(plan))
    }
    @Test fun threeDistinctDocumentProcedures() = runBlocking {
        paid(); val d = Dialogue(catalog(), "three-procedures")
        val scenarios = listOf(
            "أنا أردني وبدي birth certificate لإلي، واقعة الولادة مسجلة بحاسوب الأحوال. ورجيني خطوات إصدارها." to "birth_certificate",
            "بدي أصدق صورة شهادة زواج صادرة عن الأحوال المدنية، الأصل معي. شو الخطوات؟" to "document_attestation",
            "أنا رب الأسرة وبدي أغير العنوان المصرح به للتبليغات. أعطيني الخطوات." to "declared_address")
        for ((query, source) in scenarios) {
            val r = d.turn(query)
            assertEquals(r.message, ResponseKind.PLAN, r.kind)
            assertTrue(r.sourceIds.contains("cspd-2024-$source"))
            assertTrue(r.plan!!.steps.size >= 3)
            assertNotNull(d.catalog.toDomain(r))
        }
    }
    @Test fun naturalPassportConversationReachesStepsWithoutMagicWords() = runBlocking {
        paid(); val d = Dialogue(catalog(), "passport-natural-conversation")
        val start = d.turn("يا زلمة جوازي ضاع ومش عارف من وين أبلش")
        assertTrue(start.message, start.kind != ResponseKind.UNSUPPORTED)
        val overview = d.turn("مش متأكد من كل التفاصيل، ورجيني خطوات بدل فاقد للجواز الأردني العادي مبدئياً وبينلي شو اللي لازم أتأكد منه")
        assertEquals(overview.message, ResponseKind.PLAN, overview.kind)
        assertTrue(overview.sourceIds.contains("cspd-2024-lost_passport"))
        assertTrue(overview.plan!!.steps.size >= 4)
        val fees = d.turn("هو العادي وأول مرة بضيع مني. كم رسومه؟ وهل الكفالة نفس الرسوم؟")
        assertEquals(fees.message, ResponseKind.PLAN, fees.kind)
        val text = fees.message + ContractCodec(d.catalog.schema).encode(fees)
        assertTrue(text.contains("125"))
        assertTrue(text.contains("كفالة"))
        assertTrue(fees.sourceIds.contains("cspd-2024-lost_passport"))
    }
    @Test fun familyBookAndEducationCoverage() = runBlocking {
        paid(); val d = Dialogue(catalog(), "book-and-unsupported-education")
        val book = d.turn("دفتر العيلة ضاع ومعي بلاغ فقدان، ورجيني الخطوات اللي بتعرفها من الوثائق")
        assertEquals(book.message, ResponseKind.PLAN, book.kind)
        assertTrue(book.sourceIds.any { it in d.catalog.sourceIds })
        assertTrue(book.uncertainties.isNotEmpty())
        val education = d.turn("موضوع ثاني: بدي أوثق شهادة التوجيهي. كيف؟")
        assertEquals(education.message, ResponseKind.UNSUPPORTED, education.kind)
        assertNull(education.plan)
        assertTrue(education.questions.isEmpty())
    }
    @Test fun arbitraryDocumentQuestionsAndFactsAreAccepted() {
        val c = catalog()
        val r = AssistantResponse(kind = ResponseKind.CLARIFICATION, message = "وين صار فقدان الجواز؟",
            caseSummary = CaseSummary("استبدال الجواز", listOf(IssueFact("loss_place_description", "أثناء السفر")), listOf("مكان الفقد")),
            questions = listOf(ClarificationQuestion("where_it_was_lost", "هل فقدته داخل الأردن أو خارجه؟", emptyList(),
                "الدليل يذكر وثائق إضافية للفقد خارج المملكة.")), suggestedPrompts = c.startPrompts,
            sourceIds = listOf("cspd-2024-lost_passport"))
        val decoded = ContractCodec(c.schema).decode(ContractCodec(c.schema).encode(r))
        ResponseValidator(c).validate(decoded)
    }
    @Test fun unknownCitationsAndDependencyCyclesAreRejected() {
        val c = catalog(); val response = c.additionalServices.first().response
        val unknown = response.copy(sourceIds = listOf("made-up-source"))
        assertThrows(IllegalArgumentException::class.java) { ResponseValidator(c).validate(unknown) }
        val plan = response.plan!!
        val bad = response.copy(plan = plan.copy(steps = plan.steps.mapIndexed { index, step ->
            if (index == 0) step.copy(dependsOn = listOf(plan.steps.last().id)) else step
        }))
        assertThrows(IllegalArgumentException::class.java) { ResponseValidator(c).validate(bad) }
    }
}
