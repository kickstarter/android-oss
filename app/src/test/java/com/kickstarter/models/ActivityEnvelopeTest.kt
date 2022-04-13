package com.kickstarter.models

import com.kickstarter.mock.factories.ActivityEnvelopeFactory
import com.kickstarter.mock.factories.ActivityFactory
import com.kickstarter.services.apiresponses.ActivityEnvelope
import junit.framework.TestCase
import org.junit.Test

class ActivityEnvelopeTest : TestCase() {
    @Test
    fun testDefaultInit() {

        val activities = listOf(ActivityFactory.activity())

        val apiEnvelope = ActivityEnvelope.UrlsEnvelope.ApiEnvelope.builder()
            .moreActivities("http://kck.str/activities/more")
            .build()

        val urlsEnvelope = ActivityEnvelope.UrlsEnvelope.builder()
            .api(apiEnvelope)
            .build()

        val activityEnvelope = ActivityEnvelope.builder()
            .activities(activities)
            .urls(urlsEnvelope)
            .build()

        assertEquals(activityEnvelope.activities(), activities)
        assertEquals(activityEnvelope.urls(), urlsEnvelope)
        assertEquals(urlsEnvelope.api(), apiEnvelope)
        assertEquals(apiEnvelope.moreActivities(), "http://kck.str/activities/more")
    }

    @Test
    fun testActivityEnvelope_equalFalse() {
        val activities = listOf(ActivityFactory.activity())

        val messageThreadsEnvelope = ActivityEnvelopeFactory.activityEnvelope(activities)
        val messageThreadsEnvelope2 = ActivityEnvelope.builder().build()
        val messageThreadsEnvelope3 = ActivityEnvelope.builder().urls(
            ActivityEnvelope.UrlsEnvelope.builder()
                .api(
                    ActivityEnvelope.UrlsEnvelope.ApiEnvelope.builder()
                        .moreActivities("http://kck.str/activities/more")
                        .newerActivities("http://kck.str/activities/more")
                        .build()
                )
                .build()
        ).build()

        assertFalse(messageThreadsEnvelope == messageThreadsEnvelope2)
        assertFalse(messageThreadsEnvelope == messageThreadsEnvelope3)

        assertFalse(messageThreadsEnvelope3 == messageThreadsEnvelope2)
    }

    @Test
    fun testActivityEnvelope_equalTrue() {
        val messageThreadsEnvelope1 = ActivityEnvelope.builder().build()
        val messageThreadsEnvelope2 = ActivityEnvelope.builder().build()

        assertEquals(messageThreadsEnvelope1, messageThreadsEnvelope2)
    }

    @Test
    fun testActivityEnvelopeToBuilder() {
        val apiEnvelope = ActivityEnvelope.UrlsEnvelope.ApiEnvelope.builder().build()
            .toBuilder()
            .moreActivities("http://kck.str/activities/more")
            .build()

        val urlsEnvelope = ActivityEnvelope.UrlsEnvelope.builder().build()
            .toBuilder()
            .api(apiEnvelope)
            .build()

        val messageThreadsEnvelope = ActivityEnvelope.builder().build()
            .toBuilder()
            .urls(urlsEnvelope)
            .build()

        assertEquals(messageThreadsEnvelope.urls(), urlsEnvelope)
        assertEquals(urlsEnvelope.api(), apiEnvelope)
        assertEquals(apiEnvelope.moreActivities(), "http://kck.str/activities/more")
    }
}
