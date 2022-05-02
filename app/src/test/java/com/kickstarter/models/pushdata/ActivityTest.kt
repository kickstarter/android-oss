package com.kickstarter.models.pushdata

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class ActivityTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val activity = Activity.Builder()
            .commentId(12L)
            .projectId(123L)
            .projectPhoto("https://www.kickstarter.com/projects/123")
            .updateId(1L)
            .userPhoto(
                "https://www.kickstarter.com/avatars/12345678"
            ).build()

        assertTrue(activity.id() == 0L)
        assertEquals(activity.category(), "")
        assertEquals(activity.projectId(), 123L)
        assertEquals(activity.commentId(), 12L)
        assertEquals(activity.projectPhoto(), "https://www.kickstarter.com/projects/123")
        assertEquals(activity.updateId(), 1L)
        assertEquals(activity.userPhoto(), "https://www.kickstarter.com/avatars/12345678")
    }

    @Test
    fun testDefaultToBuilder() {
        val activity1 =
            Activity.Builder()
                .commentId(12L)
                .projectId(123L)
                .projectPhoto("https://www.kickstarter.com/projects/123")
                .updateId(1L)
                .userPhoto(
                    "https://www.kickstarter.com/avatars/12345678"
                ).build()

        val activity2 = activity1.toBuilder().id(1234L).build()
        assertTrue(activity2.id() == 1234L)
    }

    @Test
    fun testActivity_equalFalse() {
        val activity =
            Activity.Builder()
                .commentId(12L)
                .projectId(123L)
                .projectPhoto("https://www.kickstarter.com/projects/123")
                .updateId(1L)
                .userPhoto(
                    "https://www.kickstarter.com/avatars/12345678"
                ).build()

        val activity2 = Activity.Builder().id(1234L).build()

        val activity3 =
            Activity.Builder()
                .commentId(12L)
                .userPhoto(
                    "https://www.kickstarter.com/avatars/12345678"
                ).build()
        val activity4 =
            Activity.Builder()
                .commentId(12L)
                .projectId(123L)
                .projectPhoto("https://www.kickstarter.com/projects/123")
                .userPhoto(
                    "https://www.kickstarter.com/avatars/12345678"
                ).build()

        assertFalse(activity == activity2)
        assertFalse(activity == activity3)
        assertFalse(activity == activity4)

        assertFalse(activity3 == activity2)
        assertFalse(activity3 == activity4)
    }

    @Test
    fun testActivity_equalsTrue() {
        val activity1 =
            Activity.Builder().build()

        val activity2 = Activity.Builder().build()

        assertTrue(activity1 == activity2)
    }
}
