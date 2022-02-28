package com.kickstarter.models

import com.kickstarter.mock.factories.UserFactory
import junit.framework.TestCase
import org.joda.time.DateTime
import org.junit.Test

class MessageTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val message = Message.builder().createdAt(DateTime.now()).build()

        assertTrue(message.id() == 0L)
        assertTrue(message.body() == "")
        assertTrue(message.createdAt() == DateTime.now())
        assertTrue(message.recipient() == User.builder().build())
        assertTrue(message.sender() == User.builder().build())
    }

    @Test
    fun testMessage_equalFalse() {
        val message = Message.builder().build()
        val message2 = Message.builder().body("body2").createdAt(DateTime.now()).id(1234L).sender(User.builder().build()).build()
        val message3 = Message.builder().body("body3").createdAt(DateTime.now().plusDays(1)).id(5678L).sender(UserFactory.creator()).build()
        val message4 = Message.builder().body("body4").createdAt(DateTime.now().plusDays(3)).id(1234L).recipient(UserFactory.germanUser()).sender(UserFactory.creator()).build()

        assertFalse(message == message2)
        assertFalse(message == message3)
        assertFalse(message == message4)

        assertFalse(message3 == message2)
        assertFalse(message3 == message4)
    }

    @Test
    fun testMessage_equalTrue() {
        val message1 = Message.builder().body("body2").createdAt(DateTime.now()).id(1234L).sender(User.builder().build()).build()
        val message2 = message1.toBuilder().body("body2").build()

        assertTrue(message1 == message2)
    }
}
