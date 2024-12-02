package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.environmentalCommitmentTransformer
import com.kickstarter.services.transformers.projectFaqTransformer
import com.kickstarter.type.EnvironmentalCommitmentCategory
import org.junit.Test

class TransformersTest : KSRobolectricTestCase() {

    @Test
    fun projectFaqTransformerTest() {
        // - fragment.Faq is a generated DataStructure by Apollo
        val fragmentFaq = com.kickstarter.fragment.Faq("UHJvamVjdEZhcS0zNTgwNTE=", "answer", null, "question")
        val faq = projectFaqTransformer(fragmentFaq)

        assertTrue(faq.id == decodeRelayId("UHJvamVjdEZhcS0zNTgwNTE="))
        assertTrue(faq.answer == "answer")
        assertTrue(faq.question == "question")
        assertTrue(faq.createdAt == null)
        assertTrue(faq.question == "question")
    }

    @Test
    fun environmentCommitmentsTransformerTest() {
        val fragmentEnvCom = com.kickstarter.fragment.EnvironmentalCommitment(
            EnvironmentalCommitmentCategory.environmentally_friendly_factories,
            "Description",
            "RW52aXJvbm1lbnRhbENvbW1pdG1lbnQtMTA5Njk0",
        )

        val envCommitment = environmentalCommitmentTransformer(fragmentEnvCom)

        assertTrue(envCommitment.id == decodeRelayId("RW52aXJvbm1lbnRhbENvbW1pdG1lbnQtMTA5Njk0"))
        assertTrue(envCommitment.description == "Description")
        assertTrue(envCommitment.category == EnvironmentalCommitmentCategory.environmentally_friendly_factories.name)
    }
}
