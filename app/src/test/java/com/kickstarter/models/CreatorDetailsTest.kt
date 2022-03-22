package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.CreatorDetailsFactory
import org.junit.Test

class CreatorDetailsTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val creatorDetails = CreatorDetails.builder()
            .backingsCount(3)
            .launchedProjectsCount(2)
            .build()

        assertEquals(creatorDetails.backingsCount(), 3)
        assertEquals(creatorDetails.launchedProjectsCount(), 2)
    }

    @Test
    fun testToBuilder() {
        val creatorDetails = CreatorDetailsFactory.creatorDetails().toBuilder().backingsCount(5)
            .launchedProjectsCount(6).build()

        assertEquals(creatorDetails.backingsCount(), 5)
        assertEquals(creatorDetails.launchedProjectsCount(), 6)
    }

    @Test
    fun testCreatorDetails_equalFalse() {
        val creatorDetails = CreatorDetails.builder().build()
        val creatorDetails2 = CreatorDetails.builder().backingsCount(3).build()
        val creatorDetails3 = CreatorDetails.builder().launchedProjectsCount(2).build()
        val creatorDetails4 = CreatorDetailsFactory.creatorDetails()

        assertFalse(creatorDetails == creatorDetails2)
        assertFalse(creatorDetails == creatorDetails3)
        assertFalse(creatorDetails == creatorDetails4)

        assertFalse(creatorDetails3 == creatorDetails2)
        assertFalse(creatorDetails3 == creatorDetails4)
    }

    @Test
    fun testCreatorDetails_equalTrue() {
        val creatorDetails1 = CreatorDetails.builder().build()
        val creatorDetails2 = CreatorDetails.builder().build()

        assertEquals(creatorDetails1, creatorDetails2)
    }
}
