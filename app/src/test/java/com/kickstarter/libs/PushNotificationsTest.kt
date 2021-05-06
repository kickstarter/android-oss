package com.kickstarter.libs

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.TaskStackBuilder
import androidx.test.core.app.ApplicationProvider
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.MessageThreadEnvelopeFactory
import com.kickstarter.mock.factories.PushNotificationEnvelopeFactory
import com.kickstarter.models.MessageThread
import com.kickstarter.services.apiresponses.PushNotificationEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.MessagesActivity
import org.junit.Test

class PushNotificationsTest : KSRobolectricTestCase() {
    private val application: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun messageThreadIntent() {
        val envelope = PushNotificationEnvelopeFactory.envelope()
        val messageThread = MessageThreadEnvelopeFactory.messageThreadEnvelope().messageThread()
        val pushNotifications = PushNotifications(application, environment().apiClient())

        messageThread?.let {
            val pendingIntent = pushNotifications.messageThreadIntent(envelope, messageThread)
            val mockedPendingIntent = getMockedPushedMessagePendingIntent(messageThread, envelope)

            assertEquals(mockedPendingIntent, pendingIntent)
        }
    }

    private fun getMockedPushedMessagePendingIntent(messageThread: MessageThread?, envelope: PushNotificationEnvelope): PendingIntent? {
        val messageThreadIntent = Intent(application, MessagesActivity::class.java)
            .putExtra(IntentKey.MESSAGE_THREAD, messageThread)
            .putExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT, MessagePreviousScreenType.PUSH)

        return TaskStackBuilder.create(application)
            .addNextIntentWithParentStack(messageThreadIntent)
            .getPendingIntent(envelope.signature(), PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
