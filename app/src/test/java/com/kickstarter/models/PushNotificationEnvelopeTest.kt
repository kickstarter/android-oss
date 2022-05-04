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

        val message = PushNotificationEnvelope.Message.builder()
            .projectId(12L)
            .messageThreadId(13L)
            .build()

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
            .message(message)
            .build()

        assertEquals(pushNotificationEnvelope.gcm(), gcm)
        assertEquals(pushNotificationEnvelope.activity(), activity)
        assertEquals(pushNotificationEnvelope.project(), project)
        assertEquals(pushNotificationEnvelope.erroredPledge(), erroredPledge)
        assertEquals(pushNotificationEnvelope.survey(), survey)
        assertEquals(pushNotificationEnvelope.message(), message)

        assertEquals(survey.id(), 1L)
        assertEquals(survey.projectId(), 12L)

        assertEquals(project.id(), 12L)
        assertEquals(project.photo(), "https://www.kickstarter.com/avatars/12345678")

        assertEquals(erroredPledge.projectId(), 12)

        assertEquals(message.messageThreadId(), 13L)
        assertEquals(message.projectId(), 12L)
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
    fun testSurveyToBuilder() {
        val survey = PushNotificationEnvelope.Survey.builder()
            .build().toBuilder()
            .id(1L)
            .projectId(12L)
            .build()

        assertEquals(survey.id(), 1L)
        assertEquals(survey.projectId(), 12L)
    }

    @Test
    fun testErroredPledgeToBuilder() {
        val erroredPledge = PushNotificationEnvelope.ErroredPledge.builder()
            .build().toBuilder()
            .projectId(12L)
            .build()

        assertEquals(erroredPledge.projectId(), 12)
    }

    @Test
    fun testMessageToBuilder() {
        val message = PushNotificationEnvelope.Message.builder()
            .build().toBuilder()
            .projectId(12L)
            .messageThreadId(13L)
            .build()

        assertEquals(message.messageThreadId(), 13L)
        assertEquals(message.projectId(), 12L)
    }

    @Test
    fun testProjectToBuilder() {
        val project = PushNotificationEnvelope.Project.builder()
            .build().toBuilder()
            .id(12L)
            .photo(
                "https://www.kickstarter.com/avatars/12345678"
            ).build()

        assertEquals(project.id(), 12L)
        assertEquals(project.photo(), "https://www.kickstarter.com/avatars/12345678")
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

    @Test
    fun testPushNotificationEnvelope_isErroredPledge() {
        val pushNotificationEnvelope = PushNotificationEnvelope.builder().build()

        assertFalse(pushNotificationEnvelope.isErroredPledge())

        val erroredPledge = PushNotificationEnvelope.ErroredPledge.builder()
            .projectId(12L)
            .build()

        val isErroredPledge = pushNotificationEnvelope.toBuilder()
            .erroredPledge(erroredPledge)
            .build()
            .isErroredPledge()

        assertTrue(isErroredPledge)
    }

    @Test
    fun testPushNotificationEnvelope_isFriendFollow() {
        val pushNotificationEnvelope = PushNotificationEnvelope.builder().build()

        assertFalse(pushNotificationEnvelope.isFriendFollow())

        val activity = Activity.builder()
            .commentId(12L)
            .projectId(123L)
            .category(com.kickstarter.models.Activity.CATEGORY_FOLLOW)
            .projectPhoto("https://www.kickstarter.com/projects/123")
            .updateId(1L)
            .userPhoto(
                "https://www.kickstarter.com/avatars/12345678"
            ).build()

        val isFriendFollow = pushNotificationEnvelope.toBuilder()
            .activity(activity)
            .build()
            .isFriendFollow()

        assertTrue(isFriendFollow)
    }

    @Test
    fun testPushNotificationEnvelope_isMessage() {
        val pushNotificationEnvelope = PushNotificationEnvelope.builder().build()

        assertFalse(pushNotificationEnvelope.isMessage())

        val message = PushNotificationEnvelope.Message.builder()
            .projectId(12L)
            .messageThreadId(13L)
            .build()

        val isMessage = pushNotificationEnvelope.toBuilder()
            .message(message)
            .build()
            .isMessage()

        assertTrue(isMessage)
    }

    @Test
    fun testPushNotificationEnvelope_isProjectActivity() {
        val pushNotificationEnvelope = PushNotificationEnvelope.builder().build()

        assertFalse(pushNotificationEnvelope.isProjectActivity())

        val activity = Activity.builder()
            .commentId(12L)
            .projectId(123L)
            .category(com.kickstarter.models.Activity.CATEGORY_FOLLOW)
            .projectPhoto("https://www.kickstarter.com/projects/123")
            .updateId(1L)
            .userPhoto(
                "https://www.kickstarter.com/avatars/12345678"
            ).build()

        val isProjectActivity = pushNotificationEnvelope.toBuilder()
            .activity(activity)
            .build()
            .isProjectActivity()

        assertFalse(isProjectActivity)

        val isProjectActivity2 = pushNotificationEnvelope.toBuilder()
            .activity(activity.toBuilder().category(com.kickstarter.models.Activity.CATEGORY_BACKING).build())
            .build()
            .isProjectActivity()

        assertTrue(isProjectActivity2)

        val isProjectActivity3 = pushNotificationEnvelope.toBuilder()
            .activity(activity.toBuilder().category(com.kickstarter.models.Activity.CATEGORY_CANCELLATION).build())
            .build()
            .isProjectActivity()

        assertTrue(isProjectActivity3)

        val isProjectActivity4 = pushNotificationEnvelope.toBuilder()
            .activity(activity.toBuilder().category(com.kickstarter.models.Activity.CATEGORY_FAILURE).build())
            .build()
            .isProjectActivity()

        assertTrue(isProjectActivity4)

        val isProjectActivity5 = pushNotificationEnvelope.toBuilder()
            .activity(activity.toBuilder().category(com.kickstarter.models.Activity.CATEGORY_LAUNCH).build())
            .build()
            .isProjectActivity()

        assertTrue(isProjectActivity5)

        val isProjectActivity6 = pushNotificationEnvelope.toBuilder()
            .activity(activity.toBuilder().category(com.kickstarter.models.Activity.CATEGORY_SUCCESS).build())
            .build()
            .isProjectActivity()

        assertTrue(isProjectActivity6)
    }

    @Test
    fun testPushNotificationEnvelope_isProjectReminder() {
        val pushNotificationEnvelope = PushNotificationEnvelope.builder().build()

        assertFalse(pushNotificationEnvelope.isProjectReminder())

        val project = PushNotificationEnvelope.Project.builder()
            .id(12L)
            .photo(
                "https://www.kickstarter.com/avatars/12345678"
            ).build()

        val isProjectReminder = pushNotificationEnvelope.toBuilder()
            .project(project)
            .build()
            .isProjectReminder()

        assertTrue(isProjectReminder)
    }

    @Test
    fun testPushNotificationEnvelope_isProjectUpdateActivity() {
        val pushNotificationEnvelope = PushNotificationEnvelope.builder().build()

        assertFalse(pushNotificationEnvelope.isProjectUpdateActivity())

        val activity = Activity.builder()
            .projectId(123L)
            .category(com.kickstarter.models.Activity.CATEGORY_UPDATE)
            .build()

        val isProjectActivity = pushNotificationEnvelope.toBuilder()
            .activity(activity)
            .build()
            .isProjectUpdateActivity()

        assertTrue(isProjectActivity)
    }

    @Test
    fun testPushNotificationEnvelope_isSurvey() {
        val pushNotificationEnvelope = PushNotificationEnvelope.builder().build()

        assertFalse(pushNotificationEnvelope.isSurvey())

        val survey = PushNotificationEnvelope.Survey.builder()
            .id(1L)
            .projectId(12L)
            .build()

        val isSurvey = pushNotificationEnvelope.toBuilder()
            .survey(survey)
            .build()
            .isSurvey()

        assertTrue(isSurvey)
    }
}
