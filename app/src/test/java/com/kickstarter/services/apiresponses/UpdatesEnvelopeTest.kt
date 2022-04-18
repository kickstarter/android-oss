package com.kickstarter.services.apiresponses

import com.kickstarter.mock.factories.UpdateFactory
import junit.framework.TestCase
import org.junit.Test

class UpdatesEnvelopeTest : TestCase() {

    @Test
    fun testUpdatesEnvelopDefaultInit() {
        val updates = listOf(UpdateFactory.update(), UpdateFactory.backersOnlyUpdate())
        val urls = UpdatesEnvelope.UrlsEnvelope.builder().build()
        val updatesEnvelope = UpdatesEnvelope.builder().updates(updates).urls(urls).build()

        assertEquals(updatesEnvelope.urls(), urls)
        assertEquals(updatesEnvelope.updates(), updates)
    }

    @Test
    fun testUrlsEnvelopeDefaultInit() {
        val api = UpdatesEnvelope.UrlsEnvelope.ApiEnvelope
            .builder()
            .moreUpdates("more_updates")
            .build()
        val urlsEnvelope = UpdatesEnvelope.UrlsEnvelope.builder()
            .api(api)
            .build()

        assertEquals(urlsEnvelope.api(), api)
    }

    @Test
    fun testApiEnvelopeDefaultInit() {
        val apiEnvelope = UpdatesEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreUpdates("more_updates").build()

        assertEquals(apiEnvelope.moreUpdates(), "more_updates")
    }

    @Test
    fun testUpdatesEnvelopEquals_whenFieldsDontMatch_returnsFalse() {
        val updatesEnvelope1 = UpdatesEnvelope.builder()
            .updates(listOf(UpdateFactory.update(), UpdateFactory.backersOnlyUpdate()))
            .urls(UpdatesEnvelope.UrlsEnvelope.builder().api(UpdatesEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreUpdates("first_update").build()).build())
            .build()

        val updatesEnvelope2 = updatesEnvelope1.toBuilder()
            .updates(listOf(UpdateFactory.update(), UpdateFactory.update(), UpdateFactory.backersOnlyUpdate()))
            .build()

        val updatesEnvelope3 = updatesEnvelope1.toBuilder()
            .urls(UpdatesEnvelope.UrlsEnvelope.builder().api(UpdatesEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreUpdates("second_update").build()).build())
            .build()

        assertFalse(updatesEnvelope1 == updatesEnvelope2)
        assertFalse(updatesEnvelope1 == updatesEnvelope3)
        assertFalse(updatesEnvelope2 == updatesEnvelope3)
    }

    @Test
    fun testUrlsEnvelopeEquals_whenFieldsDontMatch_returnsFalse() {
        val urlsEnvelope1 = UpdatesEnvelope.UrlsEnvelope.builder().api(UpdatesEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreUpdates("first_update").build()).build()

        val urlsEnvelope2 = urlsEnvelope1.toBuilder()
            .api(UpdatesEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreUpdates("second_update").build()).build()

        assertFalse(urlsEnvelope1 == urlsEnvelope2)
    }

    @Test
    fun testApiEnvelopeEquals_whenFieldsDontMatch_returnsFalse() {
        val apiEnvelope1 = UpdatesEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreUpdates("first_update").build()

        val apiEnvelope2 = apiEnvelope1.toBuilder()
            .moreUpdates("second_update").build()

        assertFalse(apiEnvelope1 == apiEnvelope2)
    }

    @Test
    fun testUpdatesEnvelopEquals_whenFieldsMatch_returnsTrue() {
        val updatesEnvelope1 = UpdatesEnvelope.builder()
            .updates(listOf(UpdateFactory.update(), UpdateFactory.backersOnlyUpdate()))
            .urls(UpdatesEnvelope.UrlsEnvelope.builder().api(UpdatesEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreUpdates("first_update").build()).build())
            .build()

        val updatesEnvelope2 = updatesEnvelope1

        assertTrue(updatesEnvelope1 == updatesEnvelope2)
    }

    @Test
    fun testUrlsEnvelopeEquals_whenFieldsMatch_returnsTrue() {
        val urlsEnvelope1 = UpdatesEnvelope.UrlsEnvelope.builder().api(UpdatesEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreUpdates("first_update").build()).build()

        val urlsEnvelope2 = urlsEnvelope1

        assertTrue(urlsEnvelope1 == urlsEnvelope2)
    }

    @Test
    fun testApiEnvelopeEquals_whenFieldsMatch_returnsTrue() {
        val apiEnvelope1 = UpdatesEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreUpdates("first_update").build()

        val apiEnvelope2 = apiEnvelope1

        assertTrue(apiEnvelope1 == apiEnvelope2)
    }

    @Test
    fun testUpdatesEnvelopToBuilder() {
        val updates1 = listOf(UpdateFactory.update(), UpdateFactory.backersOnlyUpdate())
        val updates2 = listOf(UpdateFactory.update(), UpdateFactory.update(), UpdateFactory.backersOnlyUpdate())

        val updatesEnvelope = UpdatesEnvelope.builder().updates(updates1).build().toBuilder().updates(updates2).build()

        assertEquals(updatesEnvelope.updates(), updates2)
    }

    @Test
    fun testUrlsEnvelopeToBuilder() {
        val apiEnvelope1 = UpdatesEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreUpdates("first_update").build()
        val apiEnvelope2 = UpdatesEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreUpdates("second_update").build()
        val urlsEnvelope =
            UpdatesEnvelope.UrlsEnvelope.builder()
                .api(apiEnvelope1)
                .build()
                .toBuilder()
                .api(apiEnvelope2)
                .build()

        assertEquals(urlsEnvelope.api(), apiEnvelope2)
    }

    @Test
    fun testApiEnvelopeToBuilder() {
        val apiEnvelope =
            UpdatesEnvelope.UrlsEnvelope.ApiEnvelope.builder()
                .moreUpdates("first_update")
                .build().toBuilder()
                .moreUpdates("second_update")
                .build()

        assertEquals(apiEnvelope.moreUpdates(), "second_update")
    }
}
