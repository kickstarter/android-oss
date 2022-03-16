package com.kickstarter.models

import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.MessageThreadFactory
import com.kickstarter.mock.factories.ProjectFactory
import junit.framework.TestCase
import org.joda.time.DateTime
import org.junit.Test

class MessageThreadTest : TestCase() {

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

        assertEquals(messageThread.id(), 0L)
        assertEquals(messageThread.closed(), false)
        assertEquals(messageThread.lastMessage(), message)
        assertEquals(messageThread.backing(), backing)
        assertEquals(messageThread.participant(), user)
        assertEquals(messageThread.project(), project)
        assertEquals(messageThread.unreadMessagesCount(), 0)
    }

    @Test
    fun testMessageThread_equalFalse() {
        val messageThread = MessageThread.builder().build()
        val messageThread2 = MessageThread.builder().project(ProjectFactory.backedProject()).build()
        val messageThread3 = MessageThread.builder().project(ProjectFactory.allTheWayProject()).id(5678L).build()
        val messageThread4 = MessageThread.builder().project(ProjectFactory.allTheWayProject()).build()

        assertFalse(messageThread == messageThread2)
        assertFalse(messageThread == messageThread3)
        assertFalse(messageThread == messageThread4)

        assertFalse(messageThread3 == messageThread2)
        assertFalse(messageThread3 == messageThread4)
    }

    @Test
    fun testMessageThread_equalTrue() {
        val messageThread1 = MessageThread.builder().build()
        val messageThread2 = MessageThread.builder().build()

        assertEquals(messageThread1, messageThread2)
    }

    @Test
    fun testMessageThreadToBuilder() {
        val project = ProjectFactory.allTheWayProject()
        val messageThread = MessageThreadFactory.messageThread().toBuilder()
            .project(project).build()

        assertEquals(messageThread.project(), project)
    }
}
