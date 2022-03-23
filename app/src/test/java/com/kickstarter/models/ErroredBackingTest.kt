package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import org.joda.time.DateTime
import org.junit.Test

class ErroredBackingTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val dateTime = DateTime.parse("2020-04-02T18:08:32Z")

        val name = "Some Project Name"

        val slug = "slug"

        val project = ErroredBacking.Project.builder()
            .finalCollectionDate(dateTime)
            .name(name)
            .slug(slug)
            .build()

        val erroredBacking = ErroredBacking.builder()
            .project(project)
            .build()

        assertEquals(erroredBacking.project(), project)
        assertEquals(erroredBacking.project().finalCollectionDate(), dateTime)
        assertEquals(erroredBacking.project().name(), name)
        assertEquals(erroredBacking.project().slug(), slug)
    }

    @Test
    fun testDefaultRoBuilderInit() {

        val name = "Some Project Name"

        val project = ErroredBacking.Project.builder().build().toBuilder().name(name).build()

        val erroredBacking = ErroredBacking.builder()
            .build().toBuilder().project(project).build()

        assertEquals(erroredBacking.project(), project)

        assertEquals(project.name(), name)
    }

    @Test
    fun testErroredBacking_equalFalse() {
        val project = ErroredBacking.Project.builder().name("project").build()
        val project2 = ErroredBacking.Project.builder().name("project2").build()
        val project3 = ErroredBacking.Project.builder().name("project3").build()

        val erroredBacking = ErroredBacking.builder().build()
        val erroredBacking2 = ErroredBacking.builder().project(project).build()
        val erroredBacking3 = ErroredBacking.builder().project(project2).build()
        val erroredBacking4 = ErroredBacking.builder().project(project3).build()

        assertFalse(erroredBacking == erroredBacking2)
        assertFalse(erroredBacking == erroredBacking3)
        assertFalse(erroredBacking == erroredBacking4)

        assertFalse(erroredBacking3 == erroredBacking2)
        assertFalse(erroredBacking3 == erroredBacking4)
    }

    @Test
    fun testErroredBacking_equalTrue() {
        val project = ErroredBacking.Project.builder()
            .finalCollectionDate(DateTime.parse("2020-04-02T18:08:32Z"))
            .name("Some Project Name")
            .slug("slug")
            .build()

        val erroredBacking1 = ErroredBacking.builder().project(project).build()
        val erroredBacking2 = ErroredBacking.builder().project(project).build()

        assertEquals(erroredBacking1, erroredBacking2)
    }
}
