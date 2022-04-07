package com.kickstarter.mock.factories

import com.kickstarter.mock.factories.MessageThreadFactory.messageThread
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope

object MessageThreadsEnvelopeFactory {
    @JvmStatic
    fun messageThreadsEnvelope(): MessageThreadsEnvelope {
        return MessageThreadsEnvelope.builder()
            .urls(
                MessageThreadsEnvelope.UrlsEnvelope.builder()
                    .api(
                        MessageThreadsEnvelope.UrlsEnvelope.ApiEnvelope.builder()
                            .moreMessageThreads("http://kck.str/message_threads/more")
                            .build()
                    )
                    .build()
            )
            .messageThreads(listOf(messageThread()))
            .build()
    }
}
