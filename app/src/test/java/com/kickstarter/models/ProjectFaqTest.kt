package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import org.joda.time.DateTime
import org.junit.Test

class ProjectFaqTest : KSRobolectricTestCase() {

    @Test
    fun testProjectFaq_DefaultValues() {
        val faq = ProjectFaq.builder().build()

        assertTrue(faq.id == -1L)
        assertTrue(faq.answer == "")
        assertTrue(faq.createdAt == null)
        assertTrue(faq.question == "")
    }

    @Test
    fun testProjectFaq_SetValues() {
        val faq = ProjectFaq.builder()
            .id(7L)
            .question("question")
            .build()

        assertTrue(faq.id == 7L)
        assertTrue(faq.answer == "")
        assertTrue(faq.createdAt == null)
        assertTrue(faq.question == "question")

        val now = DateTime.now()
        val faq2 = ProjectFaq.builder()
            .answer("answer")
            .createdAt(now)
            .build()

        assertTrue(faq2.id == -1L)
        assertTrue(faq2.answer == "answer")
        assertTrue(faq2.createdAt == now)
        assertTrue(faq2.question == "")
    }

    @Test
    fun testProjectFaq_EqualsFalse() {
        val faq = ProjectFaq.builder()
            .build()
        val faq1 = ProjectFaq.builder()
            .question("q")
            .answer("q")
            .id(2L)
            .createdAt(DateTime.now())
            .build()
        val faq2 = ProjectFaq.builder()
            .question("q")
            .build()
        val faq3 = ProjectFaq.builder()
            .answer("q")
            .build()
        val faq4 = ProjectFaq.builder()
            .id(2L)
            .build()
        val faq5 = ProjectFaq.builder()
            .createdAt(DateTime.now())
            .build()

        assertFalse(faq == faq2)
        assertFalse(faq == faq3)
        assertFalse(faq == faq4)
        assertFalse(faq == faq5)

        assertFalse(faq1 == faq2)
        assertFalse(faq1 == faq3)
        assertFalse(faq1 == faq3)
        assertFalse(faq1 == faq5)
    }

    @Test
    fun testProjectFaq_EqualTrue() {
        val faq1 = ProjectFaq.builder()
            .question("q")
            .answer("q")
            .id(2L)
            .createdAt(DateTime.now())
            .build()

        val faq2 = faq1.toBuilder()
            .question("q")
            .build()

        assertTrue(faq1 == faq2)
    }
}
