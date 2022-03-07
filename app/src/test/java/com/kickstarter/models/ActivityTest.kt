package com.kickstarter.models

import com.kickstarter.mock.factories.ActivityFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UpdateFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Activity.Companion.CATEGORY_COMMENT_POST
import com.kickstarter.models.Activity.Companion.CATEGORY_SUCCESS
import junit.framework.TestCase
import org.joda.time.DateTime
import org.junit.Test

class ActivityTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val dateTime: DateTime = DateTime.now().plusMillis(300)
        val activity = Activity.builder().createdAt(dateTime).build()

        assertTrue(activity.category() == "")
        assertTrue(activity.createdAt() == dateTime)
        assertTrue(activity.id() == 0L)
        assertNull(activity.project())
        assertNull(activity.update())
        assertNull(activity.updatedAt())
        assertNull(activity.user())
    }

    @Test
    fun testActivity_equalsTrue() {
        val createdAt: DateTime = DateTime.now().plusMillis(300)
        val updatedAt: DateTime = DateTime.now().plusMillis(300)

        val activity1 =
            Activity.builder()
                .category(CATEGORY_SUCCESS)
                .createdAt(createdAt)
                .id(1234L)
                .project(ProjectFactory.almostCompletedProject())
                .update(UpdateFactory.update())
                .updatedAt(updatedAt)
                .user(UserFactory.user())
                .build()

        val activity2 = activity1.toBuilder().id(1234L).build()

        assertTrue(activity1 == activity2)
    }

    @Test
    fun testActivity_whenCategoryDifferent_equalsFalse() {
        val activity1 = ActivityFactory.activity()
        val activity2 = ActivityFactory.activity().toBuilder().category(CATEGORY_COMMENT_POST).build()

        assertFalse(activity1 == activity2)
    }

    @Test
    fun testActivity_whenCreatedAtDifferent_equalsFalse() {
        val activity1 = ActivityFactory.activity()
        val activity2 = ActivityFactory.activity().toBuilder().createdAt(DateTime.now().plusMillis(300)).build()

        assertFalse(activity1 == activity2)
    }

    @Test
    fun testActivity_whenIdDifferent_equalsFalse() {
        val activity1 = ActivityFactory.activity()
        val activity2 = ActivityFactory.activity().toBuilder().id(456L).build()

        assertFalse(activity1 == activity2)
    }

    @Test
    fun testActivity_whenProjectDifferent_equalsFalse() {
        val activity1 = ActivityFactory.activity()
        val activity2 = ActivityFactory.activity().toBuilder().project(ProjectFactory.britishProject()).build()

        assertFalse(activity1 == activity2)
    }

    @Test
    fun testActivity_whenUpdateDifferent_equalsFalse() {
        val activity1 = ActivityFactory.activity()
        val activity2 = ActivityFactory.activity().toBuilder().update(UpdateFactory.backersOnlyUpdate()).build()

        assertFalse(activity1 == activity2)
    }

    @Test
    fun testActivity_whenUpdatedAtDifferent_equalsFalse() {
        val activity1 = ActivityFactory.activity()
        val activity2 = ActivityFactory.activity().toBuilder().updatedAt(DateTime.now().plusMillis(300)).build()

        assertFalse(activity1 == activity2)
    }

    @Test
    fun testActivity_whenUserDifferent_equalsFalse() {
        val activity1 = ActivityFactory.activity()
        val activity2 = ActivityFactory.activity().toBuilder().user(UserFactory.germanUser()).build()

        assertFalse(activity1 == activity2)
    }
}