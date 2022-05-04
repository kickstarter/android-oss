package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.PushNotificationEnvelopeFactory
import com.kickstarter.models.pushdata.Activity
import com.kickstarter.models.pushdata.GCM
import com.kickstarter.services.apiresponses.PushNotificationEnvelope
import org.junit.Test

class PushNotificationEnvelopeTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val project = PushNotificationEnvelope.Project.builder()
            .id(12L)
            .photo(
                "https://www.kickstarter.com/avatars/12345678"
            ).build()

        val erroredPledge = PushNotificationEnvelope.ErroredPledge.builder()
            .projectId(12L)
            .build()

        val activity = Activity.builder()
            .commentId(12L)
            .projectId(123L)
            .projectPhoto("https://www.kickstarter.com/projects/123")
            .updateId(1L)
            .userPhoto(
                "https://www.kickstarter.com/avatars/12345678"
            ).build()

        val gcm = GCM.builder()
            .alert("You've received a new push notification")
            .title("Hello")
            .build()

        val survey = PushNotificationEnvelope.Survey.builder()
            .id(1L)
            .projectId(12L)
            .build()

        val pushNotificationEnvelope = PushNotificationEnvelope.builder()
            .gcm(gcm)
            .activity(activity)
            .project(project)
            .erroredPledge(erroredPledge)
            .survey(survey)
            .build()

        assertEquals(pushNotificationEnvelope.gcm(), gcm)
        assertEquals(pushNotificationEnvelope.activity(), activity)
        assertEquals(pushNotificationEnvelope.project(), project)
        assertEquals(pushNotificationEnvelope.erroredPledge(), erroredPledge)
        assertEquals(pushNotificationEnvelope.survey(), survey)

        assertEquals(survey.id(), 1L)
        assertEquals(survey.projectId(), 12L)

        assertEquals(project.id(), 12L)
        assertEquals(project.photo(), "https://www.kickstarter.com/avatars/12345678")

        assertEquals(erroredPledge.projectId(), 12)
    }

    @Test
    fun testDefaultToBuilder() {
        val survey = PushNotificationEnvelope.Survey.builder()
            .id(1L)
            .projectId(12L)
            .build()

        val pushNotificationEnvelope = PushNotificationEnvelopeFactory.envelope().toBuilder()
            .survey(survey)
            .build()

        assertEquals(pushNotificationEnvelope.survey(), survey)
    }

    @Test
    fun testPushNotificationEnvelope_equalFalse() {
        val project = PushNotificationEnvelope.Project.builder()
            .id(12L)
            .photo(
                "https://www.kickstarter.com/avatars/12345678"
            ).build()

        val erroredPledge = PushNotificationEnvelope.ErroredPledge.builder()
            .projectId(12L)
            .build()

        val activity = Activity.builder()
            .commentId(12L)
            .projectId(123L)
            .projectPhoto("https://www.kickstarter.com/projects/123")
            .updateId(1L)
            .userPhoto(
                "https://www.kickstarter.com/avatars/12345678"
            ).build()

        val gcm = GCM.builder()
            .alert("You've received a new push notification")
            .title("Hello")
            .build()

        val survey = PushNotificationEnvelope.Survey.builder()
            .id(1L)
            .projectId(12L)
            .build()

        val pushNotificationEnvelope = PushNotificationEnvelopeFactory.envelope()
        val pushNotificationEnvelope2 = PushNotificationEnvelope.builder()
            .gcm(gcm)
            .activity(activity)
            .survey(survey)
            .build()

        val pushNotificationEnvelope3 = PushNotificationEnvelope.builder()
            .gcm(gcm)
            .project(project)
            .erroredPledge(erroredPledge)
            .build()

        val pushNotificationEnvelope4 = PushNotificationEnvelope.builder()
            .gcm(gcm)
            .activity(activity)
            .project(project)
            .erroredPledge(erroredPledge)
            .survey(survey)
            .build()

        assertFalse(pushNotificationEnvelope == pushNotificationEnvelope2)
        assertFalse(pushNotificationEnvelope == pushNotificationEnvelope3)
        assertFalse(pushNotificationEnvelope == pushNotificationEnvelope4)

        assertFalse(pushNotificationEnvelope3 == pushNotificationEnvelope2)
        assertFalse(pushNotificationEnvelope3 == pushNotificationEnvelope4)
    }

    @Test
    fun testPushNotificationEnvelope_equalTrue() {
        val pushNotificationEnvelope1 = PushNotificationEnvelope.builder().build()
        val pushNotificationEnvelope2 = PushNotificationEnvelope.builder().build()

        assertEquals(pushNotificationEnvelope1, pushNotificationEnvelope2)
    }
}
