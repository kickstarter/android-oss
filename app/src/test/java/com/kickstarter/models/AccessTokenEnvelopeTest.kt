package com.kickstarter.models

import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import junit.framework.TestCase

class AccessTokenEnvelopeTest : TestCase() {

    fun testEquals_whenSecondEnvelopNull_returnFalse() {
        val envelopA = AccessTokenEnvelope.builder().user(UserFactory.user()).accessToken("SomeToken").build()
        val envelopB: AccessTokenEnvelope? = null

        assertFalse(envelopA == envelopB)
    }

    fun testEquals_whenEqualsEnvelopes_returnTrue() {
        val user = UserFactory.user()
        val token = "SameToken"
        val envelopA = AccessTokenEnvelope.builder().user(user).accessToken(token).build()
        val envelopB = AccessTokenEnvelope.builder().user(user).accessToken(token).build()

        assertTrue(envelopA == envelopB)
    }

    fun testEquals_whenSameUserDifferentToken_returnFalse() {
        val user = UserFactory.user()
        val envelopA = AccessTokenEnvelope.builder().user(user).accessToken("SomeToken").build()
        val envelopB = AccessTokenEnvelope.builder().user(user).accessToken("OtherToken").build()

        assertFalse(envelopA == envelopB)
    }

    fun testEquals_whenSameTokenDifferentUser_returnFalse() {
        val token = "SameToken"
        val userA = UserFactory.user()
        val userB = UserFactory.germanUser()
        val envelopA = AccessTokenEnvelope.builder().user(userA).accessToken(token).build()
        val envelopB = AccessTokenEnvelope.builder().user(userB).accessToken(token).build()

        assertFalse(envelopA == envelopB)
    }
}
