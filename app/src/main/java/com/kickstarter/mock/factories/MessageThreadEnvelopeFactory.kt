package com.kickstarter.mock.factories

import com.kickstarter.mock.factories.MessageFactory.message
import com.kickstarter.mock.factories.MessageThreadFactory.messageThread
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.services.apiresponses.MessageThreadEnvelope
import com.kickstarter.services.apiresponses.MessageThreadEnvelope.Companion.builder

object MessageThreadEnvelopeFactory {
    @JvmStatic
    fun empty(): MessageThreadEnvelope {
        return messageThreadEnvelope()
            .toBuilder()
            .messages(null)
            .build()
    }

    @JvmStatic
    fun messageThreadEnvelope(): MessageThreadEnvelope {
        return builder()
            .messages(listOf(message()))
            .messageThread(messageThread())
            .participants(listOf(user()))
            .build()
    }
}
