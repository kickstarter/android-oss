package com.kickstarter.models

import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.MessageThreadEnvelopeFactory
import com.kickstarter.mock.factories.MessageThreadFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.services.apiresponses.MessageThreadEnvelope
import junit.framework.TestCase
import org.joda.time.DateTime
import org.junit.Test

class MessageThreadEnvelopeTest : TestCase() {

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

        val messages = listOf(Message.builder().build())

        val participants = listOf(UserFactory.user())

        val messageThreadEnvelope = MessageThreadEnvelope.builder()
            .messageThread(messageThread)
            .messages(messages)
            .participants(participants)
            .build()

        assertEquals(messageThreadEnvelope.messages(), messages)
        assertEquals(messageThreadEnvelope.participants(), participants)
        assertEquals(messageThreadEnvelope.messageThread(), messageThread)
    }

    @Test
    fun testMessageThreadEnvelope_equalFalse() {
        val messageThread = MessageThreadFactory.messageThread()
        val messages = listOf(Message.builder().build())
        val participants = listOf(UserFactory.user())

        val messageThreadEnvelope = MessageThreadEnvelope.builder().build()
        val messageThreadEnvelope2 = MessageThreadEnvelope.builder().messageThread(messageThread).build()
        val messageThreadEnvelope3 = MessageThreadEnvelope.builder().messages(messages).build()
        val messageThreadEnvelope4 = MessageThreadEnvelope.builder().participants(participants).build()

        assertFalse(messageThreadEnvelope == messageThreadEnvelope2)
        assertFalse(messageThreadEnvelope == messageThreadEnvelope3)
        assertFalse(messageThreadEnvelope == messageThreadEnvelope4)

        assertFalse(messageThreadEnvelope3 == messageThreadEnvelope2)
        assertFalse(messageThreadEnvelope3 == messageThreadEnvelope4)
    }

    @Test
    fun testMessageThreadEnvelope_equalTrue() {
        val messageThreadEnvelope1 = MessageThreadEnvelope.builder().build()
        val messageThreadEnvelope2 = MessageThreadEnvelope.builder().build()

        assertEquals(messageThreadEnvelope1, messageThreadEnvelope2)
    }

    @Test
    fun testMessageThreadEnvelopeToBuilder() {
        val participants = listOf(UserFactory.user())

        val messageThreadEnvelope = MessageThreadEnvelopeFactory.messageThreadEnvelope().toBuilder()
            .participants(participants).build()

        assertEquals(messageThreadEnvelope.participants(), participants)
    }
}
