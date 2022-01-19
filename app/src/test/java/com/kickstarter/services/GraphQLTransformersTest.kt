package com.kickstarter.services

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.services.transformers.decodeRelayId
import com.kickstarter.services.transformers.projectFaqTransformer
import fragment.Faq
import org.junit.Test
import org.mockito.Mockito

class GraphQLTransformersTest: KSRobolectricTestCase() {

    @Test
    fun testProjectFaqTransformer() {
        val faq: Faq = Mockito.mock(Faq::class.java)
        Mockito.`when`(faq.id()).thenReturn("UHJvamVjdEZhcS0zODE3Mzk=")
        Mockito.`when`(faq.answer()).thenReturn("answer")
        Mockito.`when`(faq.question()).thenReturn("question")

        val projectFaq = projectFaqTransformer(faq)

        assertTrue(projectFaq.id == decodeRelayId(faq.id()))
        assertTrue(projectFaq.answer == "answer")
        assertTrue(projectFaq.question == "question")
        assertTrue(projectFaq.createdAt == null)
    }
}