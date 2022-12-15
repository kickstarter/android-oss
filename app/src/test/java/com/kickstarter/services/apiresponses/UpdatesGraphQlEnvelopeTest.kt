package com.kickstarter.services.apiresponses

import com.kickstarter.mock.factories.UpdateFactory
import com.kickstarter.services.apiresponses.updatesresponse.UpdatesGraphQlEnvelope
import junit.framework.TestCase
import org.junit.Test

class UpdatesGraphQlEnvelopeTest : TestCase() {

    @Test
    fun testUpdatesEnvelopDefaultInit() {
        val updates = listOf(UpdateFactory.update(), UpdateFactory.backersOnlyUpdate())
        val updatesGraphQlEnvelope = UpdatesGraphQlEnvelope.builder()
            .updates(updates)
            .totalCount(updates.size).build()

        assertEquals(updatesGraphQlEnvelope.totalCount, updates.size)
        assertEquals(updatesGraphQlEnvelope.updates, updates)
    }

    @Test
    fun testUpdatesEnvelopToBuilder() {
        val updates1 = listOf(UpdateFactory.update(), UpdateFactory.backersOnlyUpdate())
        val updates2 = listOf(UpdateFactory.update(), UpdateFactory.update(), UpdateFactory.backersOnlyUpdate())

        val updatesGraphQlEnvelope = UpdatesGraphQlEnvelope.builder().updates(updates1).build().toBuilder().updates(updates2).build()

        assertEquals(updatesGraphQlEnvelope.updates, updates2)
    }

    @Test
    fun testEnvelopeToBuilder() {
        val newList = listOf(UpdateFactory.backersOnlyUpdate())
        val updatesGraphQlEnvelope =
            UpdatesGraphQlEnvelope.builder()
                .updates(listOf(UpdateFactory.update()))
                .build().toBuilder()
                .updates(newList)
                .build()

        assertEquals(updatesGraphQlEnvelope.updates, newList)
    }
}
