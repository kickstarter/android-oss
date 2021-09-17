package com.kickstarter.models

import junit.framework.TestCase
import org.junit.Test

class ProjectFaqTest: TestCase(){

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
    }

}
