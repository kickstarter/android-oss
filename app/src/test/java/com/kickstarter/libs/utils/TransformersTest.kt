package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.services.decodeRelayId
import com.kickstarter.services.transformers.projectFaqTransformer
import org.junit.Test

class TransformersTest : KSRobolectricTestCase() {

    @Test
    fun projectFaqTransformerTest() {
        // - fragment.Faq is a generated DataStructure by Apollo
        val fragmentFaq = fragment.Faq("", "UHJvamVjdEZhcS0zNTgwNTE=", "answer", null, "question")
        val faq = projectFaqTransformer(fragmentFaq)

        assertTrue(faq.id == decodeRelayId("UHJvamVjdEZhcS0zNTgwNTE="))
        assertTrue(faq.answer == "answer")
        assertTrue(faq.question == "question")
        assertTrue(faq.createdAt == null)
        assertTrue(faq.question == "question")
    }
}
