package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.environmentalCommitmentTransformer
import com.kickstarter.services.transformers.projectFaqTransformer
import org.junit.Test
import type.EnvironmentalCommitmentCategory

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

    @Test
    fun environmentCommitmentsTransformerTest() {
        val fragmentEnvCom = fragment.EnvironmentalCommitment(
            "",
            EnvironmentalCommitmentCategory.ENVIRONMENTALLY_FRIENDLY_FACTORIES,
            "Description",
            "RW52aXJvbm1lbnRhbENvbW1pdG1lbnQtMTA5Njk0",
        )

        val envCommitment = environmentalCommitmentTransformer(fragmentEnvCom)

        assertTrue(envCommitment.id == decodeRelayId("RW52aXJvbm1lbnRhbENvbW1pdG1lbnQtMTA5Njk0"))
        assertTrue(envCommitment.description == "Description")
        assertTrue(envCommitment.category == EnvironmentalCommitmentCategory.ENVIRONMENTALLY_FRIENDLY_FACTORIES.name)
    }
}
