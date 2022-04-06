package com.kickstarter.models

import com.kickstarter.services.apirequests.XauthBody
import junit.framework.TestCase
import org.junit.Test

class XauthBodyTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val email = "test"
        val password = "123"
        val xauthBody = XauthBody.builder()
            .email(email)
            .password(password)
            .build()

        assertEquals(xauthBody.email(), email)
        assertEquals(xauthBody.password(), password)
        assertEquals(xauthBody.code(), null)
    }

    @Test
    fun testXauthBody_equalFalse() {
        val email = "test"
        val xauthBody = XauthBody.builder().build()
        val xauthBody2 = XauthBody.builder().email(email).build()
        val xauthBody3 = XauthBody.builder().code("code").build()
        val xauthBody4 = XauthBody.builder().password("123").build()

        assertFalse(xauthBody == xauthBody2)
        assertFalse(xauthBody == xauthBody3)
        assertFalse(xauthBody == xauthBody4)

        assertFalse(xauthBody3 == xauthBody2)
        assertFalse(xauthBody3 == xauthBody4)
    }

    @Test
    fun testXauthBody_equalTrue() {
        val xauthBody1 = XauthBody.builder().build()
        val xauthBody2 = XauthBody.builder().build()

        assertEquals(xauthBody1, xauthBody2)
    }

    @Test
    fun testXauthBodyToBuilder() {
        val email = "test"
        val xauthBody = XauthBody.builder().build().toBuilder()
            .email(email).build()

        assertEquals(xauthBody.email(), email)
    }
}
