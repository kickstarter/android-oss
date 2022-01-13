package com.kickstarter.models

import com.kickstarter.services.apirequests.BackingBody
import junit.framework.TestCase

class BackingBodyTest : TestCase() {

    fun testEquals_whenBacker() {
        val bodA = BackingBody.builder().build()
        val bodB = BackingBody.builder()
            .backer(9L)
            .build()
        assertFalse(bodA == bodB)

        val bodC = bodB
            .toBuilder()
            .backer(bodA.backer())
            .build()
        assertTrue(bodA == bodC)
    }

    fun testEquals_whenId() {
        val bodA = BackingBody.builder().build()
        val bodB = BackingBody.builder()
            .id(9L)
            .build()
        assertFalse(bodA == bodB)

        val bodC = bodB
            .toBuilder()
            .id(bodA.id())
            .build()
        assertTrue(bodA == bodC)
    }

    fun testEquals_whenBackerNote() {
        val bodA = BackingBody.builder().build()
        val bodB = BackingBody.builder()
            .backerNote("Hola")
            .build()
        assertFalse(bodA == bodB)

        val bodC = bodB
            .toBuilder()
            .backerNote(bodA.backerNote())
            .build()
        assertTrue(bodA == bodC)
    }

    fun testEquals_whenBackerCompletedAt() {
        val bodA = BackingBody.builder().build()
        val bodB = BackingBody.builder()
            .backerCompletedAt(7)
            .build()
        assertFalse(bodA == bodB)

        val bodC = bodB
            .toBuilder()
            .backerCompletedAt(bodA.backerCompletedAt())
            .build()
        assertTrue(bodA == bodC)
    }

    fun testEquals_All() {
        val bodA = BackingBody.builder()
            .backer(4)
            .backerNote("cosa")
            .id(1)
            .backerCompletedAt(7)
            .build()
        val bodB = bodA
        assertTrue(bodA == bodB)
    }
}
