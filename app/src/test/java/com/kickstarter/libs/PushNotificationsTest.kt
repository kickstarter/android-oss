package com.kickstarter.libs

import android.content.Context
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.MessageThreadEnvelopeFactory
import com.kickstarter.mock.factories.PushNotificationEnvelopeFactory
import com.kickstarter.ui.IntentKey
import org.junit.Test

class PushNotificationsTest : KSRobolectricTestCase() {
    lateinit var context: Context

    override fun setUp() {
        super.setUp()
        context = application()
    }

    @Test
    fun messageThreadIntent() {
        val envelope = PushNotificationEnvelopeFactory.envelope()
        val messageThread = MessageThreadEnvelopeFactory.messageThreadEnvelope().messageThread()
        val pushNotifications = PushNotifications(context, requireNotNull(environment().apiClient()), requireNotNull(environment().optimizely()))

        messageThread?.let {
            pushNotifications.messageThreadIntent(envelope, messageThread)

            val messageThreadIntent = pushNotifications.messageThreadIntent

            assertEquals(messageThread, messageThreadIntent?.extras?.get(IntentKey.MESSAGE_THREAD))
            assertEquals(MessagePreviousScreenType.PUSH, messageThreadIntent?.extras?.get(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT))
        }
    }
}
