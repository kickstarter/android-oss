package com.kickstarter.libs.utils.extensions

import android.content.Intent
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.ui.IntentKey
import org.junit.Test

class IntentExtTest : KSRobolectricTestCase() {

    @Test
    fun testGetProjectIntent_whenFeatureFlagTrue_shouldReturnProjectPageActivity() {
        assertEquals(Intent().getProjectIntent(context(), true).component?.className, "com.kickstarter.ui.activities.ProjectPageActivity")
    }

    @Test
    fun testGetProjectIntent_whenFeatureFlagFalse_shouldReturnProjectActivity() {
        assertEquals(Intent().getProjectIntent(context(), false).component?.className, "com.kickstarter.ui.activities.ProjectActivity")
    }

    @Test
    fun testGetRootCommentsActivityIntent() {
        val projectData = ProjectDataFactory.project(ProjectFactory.project())
        val intent = Intent().getRootCommentsActivityIntent(context(), Pair(projectData.project(), projectData))
        assertEquals(intent.extras?.get(IntentKey.PROJECT), projectData.project())
        assertEquals(intent.extras?.get(IntentKey.PROJECT_DATA), projectData)
        assertNull(intent.extras?.get(IntentKey.COMMENT))
    }

    @Test
    fun testGetRootCommentsActivityIntent_forDeeplinkThread() {
        val projectData = ProjectDataFactory.project(ProjectFactory.project())
        val comment = "SOME ID COMMENT HERE"
        val intent = Intent().getRootCommentsActivityIntent(context(), Pair(projectData.project(), projectData), comment)
        assertEquals(intent.extras?.get(IntentKey.PROJECT), projectData.project())
        assertEquals(intent.extras?.get(IntentKey.PROJECT_DATA), projectData)
        assertEquals(intent.extras?.get(IntentKey.COMMENT), comment)
    }
}
