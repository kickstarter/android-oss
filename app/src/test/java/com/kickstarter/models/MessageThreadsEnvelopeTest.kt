package com.kickstarter.models

import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.MessageThreadFactory
import com.kickstarter.mock.factories.MessageThreadsEnvelopeFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope
import junit.framework.TestCase
import org.joda.time.DateTime
import org.junit.Test

class MessageThreadsEnvelopeTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val dateTime: DateTime = DateTime.now().plusMillis(300)
        val message = Message.builder().createdAt(dateTime).build()
        val project = ProjectFactory.allTheWayProject()
        val user = User.builder().build()
        val backing = BackingFactory.backing(user)

        val messageThread = MessageThread.builder()
            .lastMessage(message)
            .unreadMessagesCount(0)
            .participant(user)
            .backing(backing)
            .project(project)
            .build()

        val apiEnvelope = MessageThreadsEnvelope.UrlsEnvelope.ApiEnvelope.builder()
            .moreMessageThreads("http://kck.str/message_threads/more")
            .build()

        val urlsEnvelope = MessageThreadsEnvelope.UrlsEnvelope.builder()
            .api(apiEnvelope)
            .build()

        val messageThreadsEnvelope = MessageThreadsEnvelope.builder()
            .messageThreads(listOf(messageThread))
            .urls(urlsEnvelope)
            .build()

        assertEquals(messageThreadsEnvelope.messageThreads(), listOf(messageThread))
        assertEquals(messageThreadsEnvelope.urls(), urlsEnvelope)
        assertEquals(urlsEnvelope.api(), apiEnvelope)
        assertEquals(apiEnvelope.moreMessageThreads(), "http://kck.str/message_threads/more")
    }

    @Test
    fun testMessageThreadsEnvelope_equalFalse() {

        val messageThreadsEnvelope = MessageThreadsEnvelopeFactory.messageThreadsEnvelope()
        val messageThreadsEnvelope2 = MessageThreadsEnvelope.builder().messageThreads(listOf(MessageThreadFactory.messageThread())).build()
        val messageThreadsEnvelope3 = MessageThreadsEnvelope.builder().urls(
            MessageThreadsEnvelope.UrlsEnvelope.builder()
                .api(
                    MessageThreadsEnvelope.UrlsEnvelope.ApiEnvelope.builder()
                        .moreMessageThreads("http://kck.str/message_threads/more")
                        .build()
                )
                .build()
        ).build()

        assertFalse(messageThreadsEnvelope == messageThreadsEnvelope2)
        assertFalse(messageThreadsEnvelope == messageThreadsEnvelope3)

        assertFalse(messageThreadsEnvelope3 == messageThreadsEnvelope2)
    }

    @Test
    fun testMessageThreadsEnvelope_equalTrue() {
        val messageThreadsEnvelope1 = MessageThreadsEnvelope.builder().build()
        val messageThreadsEnvelope2 = MessageThreadsEnvelope.builder().build()

        assertEquals(messageThreadsEnvelope1, messageThreadsEnvelope2)
    }

    @Test
    fun testMessageThreadsEnvelopeToBuilder() {
        val apiEnvelope = MessageThreadsEnvelope.UrlsEnvelope.ApiEnvelope.builder().build()
            .toBuilder()
            .moreMessageThreads("http://kck.str/message_threads/more")
            .build()

        val urlsEnvelope = MessageThreadsEnvelope.UrlsEnvelope.builder().build()
            .toBuilder()
            .api(apiEnvelope)
            .build()

        val messageThreadsEnvelope = MessageThreadsEnvelope.builder().build()
            .toBuilder()
            .urls(urlsEnvelope)
            .build()

        assertEquals(messageThreadsEnvelope.urls(), urlsEnvelope)
        assertEquals(urlsEnvelope.api(), apiEnvelope)
        assertEquals(apiEnvelope.moreMessageThreads(), "http://kck.str/message_threads/more")
    }
}
