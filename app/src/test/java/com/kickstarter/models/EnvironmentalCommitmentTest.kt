package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class EnvironmentalCommitmentTest : KSRobolectricTestCase() {
    @Test
    fun testEnvironmentalCommitment_DefaultValues() {
        val env = EnvironmentalCommitment.builder().build()

        assertTrue(env.id == -1L)
        assertTrue(env.description == "")
        assertTrue(env.category == "")
    }

    @Test
    fun testEnvironmentalCommitment_SetValues() {
        val env = EnvironmentalCommitment.builder()
            .id(7L)
            .description("Description")
            .category("cat")
            .build()

        assertTrue(env.id == 7L)
        assertTrue(env.description == "Description")
        assertTrue(env.category == "cat")
    }

    @Test
    fun testEnvironmentalCommitment_EqualTrue() {
        val env = EnvironmentalCommitment.builder()
            .id(7L)
            .description("Description")
            .category("cat")
            .build()

        val env1 = EnvironmentalCommitment.builder()
            .id(7L)
            .description("Description")
            .category("cat")
            .build()

        assertTrue(env == env1)
    }

    @Test
    fun testEnvironmentalCommitment_EqualFalse() {
        val env = EnvironmentalCommitment.builder()
            .id(7L)
            .description("")
            .build()

        val env1 = EnvironmentalCommitment.builder()
            .id(7L)
            .description("Description")
            .category("cat")
            .build()

        val env2 = EnvironmentalCommitment.builder()
            .id(7L)
            .description("Description")
            .category("Cat")
            .build()

        assertFalse(env == env1)
        assertFalse(env1 == env2)
    }
}
