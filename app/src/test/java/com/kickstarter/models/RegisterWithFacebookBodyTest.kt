package com.kickstarter.models

import com.kickstarter.services.apirequests.RegisterWithFacebookBody
import junit.framework.TestCase
import org.junit.Test

class RegisterWithFacebookBodyTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val accessToken = "test"
        val registerWithFacebookBody = RegisterWithFacebookBody.builder()
            .accessToken(accessToken)
            .newsletterOptIn(true)
            .sendNewsletters(true)
            .build()

        assertEquals(registerWithFacebookBody.accessToken(), accessToken)
        assertTrue(registerWithFacebookBody.newsletterOptIn())
        assertTrue(registerWithFacebookBody.sendNewsletters())
    }

    @Test
    fun testRegisterWithFacebookBody_equalFalse() {
        val registerWithFacebookBody = RegisterWithFacebookBody.builder().build()
        val registerWithFacebookBody2 = RegisterWithFacebookBody.builder().sendNewsletters(true).build()
        val registerWithFacebookBody3 = RegisterWithFacebookBody.builder().newsletterOptIn(true).build()

        assertFalse(registerWithFacebookBody == registerWithFacebookBody2)
        assertFalse(registerWithFacebookBody == registerWithFacebookBody3)
        assertFalse(registerWithFacebookBody2 == registerWithFacebookBody3)
    }

    @Test
    fun testRegisterWithFacebookBody_equalTrue() {
        val registerWithFacebookBody1 = RegisterWithFacebookBody.builder().build()
        val registerWithFacebookBody2 = RegisterWithFacebookBody.builder().build()

        assertEquals(registerWithFacebookBody1, registerWithFacebookBody2)
    }

    @Test
    fun testRegisterWithFacebookBodyToBuilder() {
        val registerWithFacebookBody = RegisterWithFacebookBody.builder().build().toBuilder()
            .newsletterOptIn(true).build()

        assertTrue(registerWithFacebookBody.newsletterOptIn())
    }
}
