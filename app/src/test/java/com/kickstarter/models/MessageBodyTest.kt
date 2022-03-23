package com.kickstarter.models

import com.kickstarter.services.apirequests.MessageBody
import junit.framework.TestCase
import org.junit.Test

class MessageBodyTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val message = "test"
        val messageBody = MessageBody.builder()
            .body(message)
            .build()

        assertEquals(messageBody.body(), message)
    }

    @Test
    fun testMessageBody_equalFalse() {
        val messageBody = MessageBody.builder().build()
        val messageBody2 = MessageBody.builder().body("test").build()

        assertFalse(messageBody == messageBody2)
    }

    @Test
    fun testMessageBody_equalTrue() {
        val messageBody1 = MessageBody.builder().build()
        val messageBody2 = MessageBody.builder().build()

        assertEquals(messageBody1, messageBody2)
    }

    @Test
    fun testMessageBodyToBuilder() {
        val message = "test"
        val messageBody = MessageBody.builder().build().toBuilder()
            .body(message).build()

        assertEquals(messageBody.body(), message)
    }
}
