package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.RelativeDateTimeOptions
import org.joda.time.DateTime
import org.junit.Test

class RelativeDateTimeOptionsTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val dateTime = DateTime.parse("2015-12-17T18:35:05Z")
        val threshold = 864000 // Ten days

        val relativeDateTimeOptions = RelativeDateTimeOptions.builder()
            .relativeToDateTime(dateTime)
            .threshold(threshold)
            .abbreviated(true)
            .absolute(true)
            .build()

        assertEquals(relativeDateTimeOptions.relativeToDateTime(), dateTime)
        assertEquals(relativeDateTimeOptions.threshold(), threshold)
        assertEquals(relativeDateTimeOptions.abbreviated(), true)
        assertEquals(relativeDateTimeOptions.absolute(), true)
    }

    @Test
    fun testDefaultToBuilder() {
        val dateTime = DateTime.parse("2015-12-17T18:35:05Z")
        val relativeDateTimeOptions = RelativeDateTimeOptions.builder().build().toBuilder().relativeToDateTime(dateTime).build()

        assertEquals(relativeDateTimeOptions.relativeToDateTime(), dateTime)
    }

    @Test
    fun testRelativeDateTimeOptions_equalFalse() {
        val dateTime = DateTime.parse("2015-12-17T18:35:05Z")

        val relativeDateTimeOptions = RelativeDateTimeOptions.builder().build()

        val relativeDateTimeOptions2 = RelativeDateTimeOptions.builder()
            .relativeToDateTime(dateTime).build()

        val relativeDateTimeOptions3 = RelativeDateTimeOptions.builder()
            .abbreviated(true).build()

        val relativeDateTimeOptions4 = RelativeDateTimeOptions.builder()
            .absolute(true).build()

        assertFalse(relativeDateTimeOptions == relativeDateTimeOptions2)
        assertFalse(relativeDateTimeOptions == relativeDateTimeOptions3)
        assertFalse(relativeDateTimeOptions == relativeDateTimeOptions4)

        assertFalse(relativeDateTimeOptions3 == relativeDateTimeOptions2)
        assertFalse(relativeDateTimeOptions3 == relativeDateTimeOptions4)
    }

    @Test
    fun testRelativeDateTimeOptions_equalTrue() {
        val relativeDateTimeOptions1 = RelativeDateTimeOptions.builder().build()
        val relativeDateTimeOptions2 = RelativeDateTimeOptions.builder().build()

        assertEquals(relativeDateTimeOptions1, relativeDateTimeOptions2)
    }
}
