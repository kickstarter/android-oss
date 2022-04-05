package com.kickstarter.models

import com.kickstarter.services.apirequests.ResetPasswordBody
import junit.framework.TestCase
import org.junit.Test

class ResetPasswordBodyTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val email = "test"
        val resetPasswordBody = ResetPasswordBody.builder()
            .email(email)
            .build()

        assertEquals(resetPasswordBody.email(), email)
    }

    @Test
    fun testResetPasswordBody_equalFalse() {
        val resetPasswordBody = ResetPasswordBody.builder().build()
        val resetPasswordBody2 = ResetPasswordBody.builder().email("test").build()

        assertFalse(resetPasswordBody == resetPasswordBody2)
    }

    @Test
    fun testResetPasswordBody_equalTrue() {
        val resetPasswordBody1 = ResetPasswordBody.builder().build()
        val resetPasswordBody2 = ResetPasswordBody.builder().build()

        assertEquals(resetPasswordBody1, resetPasswordBody2)
    }

    @Test
    fun testResetPasswordBodyToBuilder() {
        val email = "test"
        val resetPasswordBody = ResetPasswordBody.builder().build().toBuilder()
            .email(email).build()

        assertEquals(resetPasswordBody.email(), email)
    }
}
