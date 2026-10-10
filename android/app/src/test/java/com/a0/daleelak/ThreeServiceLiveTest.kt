package com.a0.daleelak

import com.a0.daleelak.ai.*
import com.a0.daleelak.data.ReviewedCatalog
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.json.JSONObject
import java.io.File

/** Opt-in direct API calls through the real Android gateway, codec, catalog and validator. */
class ThreeServiceLiveTest {
    private fun catalog(): ReviewedCatalog {
        val assets = File("src/main/assets/guidance")
        return ReviewedCatalog.fromReviewedJson(File(assets, "ai-response.schema.json").readText(),
            File(assets, "reviewed-sources.json").readText(), File(assets, "additional-services.json").readText())
    }
    private fun exercise(id: String, text: String, count: Int) = runBlocking {
        assumeTrue(System.getenv("DALEELAK_ALLOW_PAID_SERVICE_TESTS") == "true")
        assertTrue("Demo APK must have the embedded key", BuildConfig.OPENROUTER_DEMO_KEY.isNotBlank())
        val catalog = catalog()
        val request = AssistantRequest(text, emptyList(), emptyMap(), null, emptyList())
        val response = OpenRouterAssistant(catalog, BuildConfig.OPENROUTER_DEMO_KEY).respond(request)
        val codec = ContractCodec(catalog.schema)
        val accepted = codec.decode(codec.encode(response))
        ResponseValidator(catalog).validate(accepted)
        assertEquals(ResponseKind.PLAN, accepted.kind)
        assertTrue("Clear first message must skip clarification", accepted.questions.isEmpty())
        assertEquals(id, accepted.plan!!.serviceId)
        assertEquals(count, accepted.plan.steps.size)
        assertEquals(catalog.additionalServices.first { it.id == id }.response.plan, accepted.plan)
        val domain = catalog.toDomain(accepted)!!
        assertFalse(domain.illustrative)
        assertTrue(domain.sourceVersions.keys.contains("cspd-2024-" + id))
        val output = File("build/service-evaluation")
        output.mkdirs()
        File(output, "$id.json").writeText(JSONObject(codec.encode(accepted))
            .put("test_scope", "Real OpenRouterAssistant classification, local reviewed plan, codec and validator; JVM execution, no Android UI walkthrough")
            .toString(2))
    }
    @Test fun birthCertificate() = exercise("birth_certificate",
        "أنا أردني بالغ وبدي شهادة ولادة لنفسي، والواقعة مسجلة حاسوبياً لدى الأحوال المدنية. كيف أطلع الشهادة؟", 6)
    @Test fun documentAttestation() = exercise("document_attestation",
        "بدي أصدق صورة شهادة الزواج الصادرة عن دائرة الأحوال المدنية والجوازات. معي الأصل والصورة وهويتي.", 5)
    @Test fun declaredAddress() = exercise("declared_address",
        "أنا رب الأسرة وبدي أحدث العنوان المصرح به للتبليغات بعد تغيير عنواني. شو الخطوات؟", 4)

    @Test fun missingFactsAskOnlyReviewedQuestions() = runBlocking {
        val c = catalog()
        for (s in c.additionalServices) {
            val r = LocalReviewedAssistant(c).respond(AssistantRequest("وضح المطلوب", emptyList(), mapOf("issue" to s.id), null, emptyList()))
            assertEquals(ResponseKind.CLARIFICATION, r.kind)
            assertEquals(listOf(s.question), r.questions)
            ResponseValidator(c).validate(r)
        }
    }
    @Test fun unsupportedBranchesDoNotReuseHappyPlan() = runBlocking {
        val c = catalog()
        for (s in c.additionalServices) {
            val negative = s.allowedValues.first { it != s.supportedValue }
            val r = LocalReviewedAssistant(c).respond(AssistantRequest("وضح المطلوب", emptyList(),
                mapOf("issue" to s.id, s.requiredFact to negative), null, emptyList()))
            assertEquals(ResponseKind.UNSUPPORTED, r.kind)
            assertNull(r.plan)
            ResponseValidator(c).validate(r)
        }
    }
    @Test fun legacyLostBookStillProducesItsReviewedPlan() = runBlocking {
        val c = catalog()
        val r = LocalReviewedAssistant(c).respond(AssistantRequest("ضاع دفتر العيلة ولسه ما عندي بلاغ", emptyList(), emptyMap(), null, emptyList()))
        ResponseValidator(c).validate(r)
        assertEquals(c.supportedPlan, r.plan)
    }
    @Test fun fabricatedProcedureCannotPassWithValidReference() {
        val c = catalog()
        val s = c.additionalServices.first()
        val valid = s.response.copy(caseSummary = s.response.caseSummary.copy(knownFacts = listOf(
            IssueFact("issue", s.id), IssueFact(s.requiredFact, s.supportedValue))))
        val forged = valid.copy(plan = valid.plan!!.copy(title = "ادفع 100 دينار"))
        assertThrows(IllegalArgumentException::class.java) { ResponseValidator(c).validate(forged) }
    }
}
