package com.kickstarter.services.apirequests

import junit.framework.TestCase
import org.junit.Test

class PushTokenBodyTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val pushTokenBody = PushTokenBody.builder()
            .pushServer("push_server")
            .token("this_is_a_token")
            .build()

        assertEquals(pushTokenBody.pushServer(), "push_server")
        assertEquals(pushTokenBody.token(), "this_is_a_token")
    }

    @Test
    fun testEquals_whenFieldsDontMatch_returnFalse() {
        val pushTokenBody1 = PushTokenBody.builder()
            .pushServer("push_server")
            .token("this_is_a_token")
            .build()

        val pushTokenBody2 = pushTokenBody1.toBuilder()
            .pushServer("push_server2")
            .build()

        val pushTokenBody3 = pushTokenBody1.toBuilder().token("this_is_a_token2").build()

        assertFalse(pushTokenBody1 == pushTokenBody2)
        assertFalse(pushTokenBody1 == pushTokenBody3)
        assertFalse(pushTokenBody2 == pushTokenBody3)
    }

    @Test
    fun testEquals_whenFieldsMatch_returnTrue() {
        val pushTokenBody1 = PushTokenBody.builder()
            .pushServer("push_server")
            .token("this_is_a_token")
            .build()

        val pushTokenBody2 = pushTokenBody1

        assertTrue(pushTokenBody1 == pushTokenBody2)
    }

    @Test
    fun testToBuilder() {
        val pushTokenBody = PushTokenBody.builder()
            .token("first_token")
            .build().toBuilder().token("changed_token").build()

        assertEquals(pushTokenBody.token(), "changed_token")
        assertNull(pushTokenBody.pushServer())
    }
}
