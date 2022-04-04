package com.kickstarter.models

import com.kickstarter.services.apirequests.LoginWithFacebookBody
import junit.framework.TestCase
import org.junit.Test

class LoginWithFacebookBodyTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val accessToken = "test"
        val loginWithFacebookBody = LoginWithFacebookBody.builder()
            .accessToken(accessToken)
            .code("123")
            .build()

        assertEquals(loginWithFacebookBody.accessToken(), accessToken)
        assertEquals(loginWithFacebookBody.code(), "123")
    }

    @Test
    fun testLoginWithFacebookBody_equalFalse() {
        val loginWithFacebookBody = LoginWithFacebookBody.builder().build()
        val loginWithFacebookBody2 = LoginWithFacebookBody.builder().code("123").build()

        assertFalse(loginWithFacebookBody == loginWithFacebookBody2)
    }

    @Test
    fun testLoginWithFacebookBody_equalTrue() {
        val loginWithFacebookBody1 = LoginWithFacebookBody.builder().build()
        val loginWithFacebookBody2 = LoginWithFacebookBody.builder().build()

        assertEquals(loginWithFacebookBody1, loginWithFacebookBody2)
    }

    @Test
    fun testLoginWithFacebookBodyToBuilder() {
        val code = "test"
        val loginWithFacebookBody = LoginWithFacebookBody.builder().build().toBuilder()
            .code(code).build()

        assertEquals(loginWithFacebookBody.code(), code)
    }
}
