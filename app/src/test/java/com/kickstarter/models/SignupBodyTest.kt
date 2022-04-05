package com.kickstarter.models

import com.kickstarter.services.apirequests.SignupBody
import junit.framework.TestCase
import org.junit.Test

class SignupBodyTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val email = "test@test.123"
        val name = "test"
        val password = "123"

        val signupBody = SignupBody.builder()
            .email(email)
            .name(name)
            .passwordConfirmation(password)
            .password(password)
            .newsletterOptIn(false)
            .sendNewsletters(true)
            .build()

        assertEquals(signupBody.email(), email)
        assertEquals(signupBody.name(), name)
        assertEquals(signupBody.password(), password)
        assertEquals(signupBody.passwordConfirmation(), password)
        assertFalse(signupBody.newsletterOptIn())
        assertTrue(signupBody.sendNewsletters())
    }

    @Test
    fun testSignupBody_equalFalse() {
        val email = "test@test.123"
        val name = "test"
        val password = "123"
        
        val signupBody = SignupBody.builder().build()
        val signupBody2 = SignupBody.builder().password(password).build()
        val signupBody3 = SignupBody.builder().name(name).build()
        val signupBody4 = SignupBody.builder().email(email).build()
        
        assertFalse(signupBody == signupBody2)
        assertFalse(signupBody == signupBody3)
        assertFalse(signupBody == signupBody4)

        assertFalse(signupBody3 == signupBody2)
        assertFalse(signupBody3 == signupBody4)
    }

    @Test
    fun testSignupBody_equalTrue() {
        val signupBody1 = SignupBody.builder().build()
        val signupBody2 = SignupBody.builder().build()

        assertEquals(signupBody1, signupBody2)
    }

    @Test
    fun testSignupBodyToBuilder() {
        val email = "test"
        val signupBody = SignupBody.builder().build().toBuilder()
            .email(email).build()

        assertEquals(signupBody.email(), email)
    }
}
