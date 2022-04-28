package com.kickstarter.models

import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.services.apiresponses.StarEnvelope
import junit.framework.TestCase
import org.junit.Test

class StarEnvelopeTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val project = ProjectFactory.project()
        val user = UserFactory.canadianUser()
        val loginWithFacebookBody = StarEnvelope.builder()
            .project(project)
            .user(user)
            .build()

        assertEquals(loginWithFacebookBody.project(), project)
        assertEquals(loginWithFacebookBody.user(), user)
    }

    @Test
    fun testStarEnvelopeToBuilder() {
        val user = UserFactory.canadianUser()
        val loginWithFacebookBody = StarEnvelope.builder().build().toBuilder()
            .user(user).build()

        assertEquals(loginWithFacebookBody.user(), user)
    }

    fun testEquals_whenSecondEnvelopNull_returnFalse() {
        val envelopA = StarEnvelope.builder().user(UserFactory.user()).project(ProjectFactory.caProject()).build()
        val envelopB: StarEnvelope? = null

        assertFalse(envelopA == envelopB)
    }

    fun testEquals_whenEqualsEnvelopes_returnTrue() {
        val user = UserFactory.user()
        val project = ProjectFactory.caProject()
        val envelopA = StarEnvelope.builder().user(user).project(project).build()
        val envelopB = StarEnvelope.builder().user(user).project(project).build()

        assertTrue(envelopA == envelopB)
    }

    fun testEquals_whenSameUserDifferentToken_returnFalse() {
        val user = UserFactory.user()
        val envelopA = StarEnvelope.builder().user(user).project(ProjectFactory.allTheWayProject()).build()
        val envelopB = StarEnvelope.builder().user(user).project(ProjectFactory.caProject()).build()

        assertFalse(envelopA == envelopB)
    }

    fun testEquals_whenSameTokenDifferentUser_returnFalse() {
        val project = ProjectFactory.caProject()
        val userA = UserFactory.user()
        val userB = UserFactory.germanUser()
        val envelopA = StarEnvelope.builder().user(userA).project(project).build()
        val envelopB = StarEnvelope.builder().user(userB).project(project).build()

        assertFalse(envelopA == envelopB)
    }
}
