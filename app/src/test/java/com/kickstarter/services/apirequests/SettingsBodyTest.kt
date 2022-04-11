package com.kickstarter.services.apirequests

import junit.framework.TestCase
import org.junit.Test

class SettingsBodyTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val settingsBody = SettingsBody.builder().showPublicProfile(1).notifyMobileOfUpdates(true).build()

        assertEquals(settingsBody.optedOutOfRecommendations(), 0)
        assertEquals(settingsBody.notifyMobileOfBackings(), false)
        assertEquals(settingsBody.notifyMobileOfComments(), false)
        assertEquals(settingsBody.notifyMobileOfCreatorEdu(), false)
        assertEquals(settingsBody.notifyMobileOfFollower(), false)
        assertEquals(settingsBody.notifyMobileOfFriendActivity(), false)
        assertEquals(settingsBody.notifyMobileOfMessages(), false)
        assertEquals(settingsBody.notifyMobileOfPostLikes(), false)
        assertEquals(settingsBody.notifyMobileOfUpdates(), true)
        assertEquals(settingsBody.notifyMobileOfMarketingUpdate(), false)
        assertEquals(settingsBody.notifyOfBackings(), false)
        assertEquals(settingsBody.notifyOfComments(), false)
        assertEquals(settingsBody.notifyOfCommentReplies(), false)
        assertEquals(settingsBody.notifyOfCreatorDigest(), false)
        assertEquals(settingsBody.notifyOfCreatorEdu(), false)
        assertEquals(settingsBody.notifyOfFollower(), false)
        assertEquals(settingsBody.notifyOfFriendActivity(), false)
        assertEquals(settingsBody.notifyOfMessages(), false)
        assertEquals(settingsBody.notifyOfUpdates(), false)
        assertEquals(settingsBody.showPublicProfile(), 1)
        assertEquals(settingsBody.social(), 0)
        assertEquals(settingsBody.alumniNewsletter(), 0)
        assertEquals(settingsBody.artsCultureNewsletter(), 0)
        assertEquals(settingsBody.filmNewsletter(), 0)
        assertEquals(settingsBody.gamesNewsletter(), 0)
        assertEquals(settingsBody.happeningNewsletter(), 0)
        assertEquals(settingsBody.inventNewsletter(), 0)
        assertEquals(settingsBody.musicNewsletter(), 0)
        assertEquals(settingsBody.promoNewsletter(), 0)
        assertEquals(settingsBody.publishingNewsletter(), 0)
        assertEquals(settingsBody.weeklyNewsletter(), 0)
    }

    @Test
    fun testEquals_whenFieldsDontMatch_returnsFalse() {
        val settingsBody1 =
            SettingsBody
                .builder()
                .notifyMobileOfComments(true)
                .notifyMobileOfUpdates(true)
                .notifyMobileOfMarketingUpdate(true)
                .notifyOfCommentReplies(true)
                .alumniNewsletter(1)
                .showPublicProfile(1)
                .build()
        val settingsBody2 =
            settingsBody1
                .toBuilder()
                .notifyOfCommentReplies(true)
                .notifyOfComments(true)
                .build()
        val settingsBody3 =
            settingsBody1
                .toBuilder()
                .musicNewsletter(1)
                .happeningNewsletter(1)
                .notifyMobileOfMessages(true)
                .build()

        assertFalse(settingsBody1 == settingsBody2)
        assertFalse(settingsBody1 == settingsBody3)
        assertFalse(settingsBody2 == settingsBody3)
    }

    @Test
    fun testEquals_whenFieldsMatch_returnsTrue() {
        val settingsBody1 =
            SettingsBody
                .builder()
                .notifyMobileOfComments(true)
                .notifyMobileOfUpdates(true)
                .notifyMobileOfMarketingUpdate(true)
                .notifyOfCommentReplies(true)
                .alumniNewsletter(1)
                .showPublicProfile(1)
                .build()
        val settingsBody2 = settingsBody1

        assertTrue(settingsBody1 == settingsBody2)
    }

    @Test
    fun testToBuilder() {
        val settingsBody = SettingsBody.builder().build().toBuilder().showPublicProfile(1).notifyMobileOfBackings(true).build()

        assertEquals(settingsBody.showPublicProfile(),1)
        assertEquals(settingsBody.notifyMobileOfBackings(), true)
    }
}