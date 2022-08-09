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
        assertEquals(Intent().getProjectIntent(context()).component?.className, "com.kickstarter.ui.activities.ProjectPageActivity")
    }

    @Test
    fun testGetRootCommentsActivityIntent() {
        val projectData = ProjectDataFactory.project(ProjectFactory.project())
        val intent = Intent().getRootCommentsActivityIntent(context(), Pair(projectData.project(), projectData))
        assertEquals(intent.component?.className, "com.kickstarter.ui.activities.CommentsActivity")
        assertEquals(intent.extras?.get(IntentKey.PROJECT), projectData.project())
        assertEquals(intent.extras?.get(IntentKey.PROJECT_DATA), projectData)
        assertNull(intent.extras?.get(IntentKey.COMMENT))
    }

    @Test
    fun testGetRootCommentsActivityIntent_forDeeplinkThread() {
        val projectData = ProjectDataFactory.project(ProjectFactory.project())
        val comment = "SOME ID COMMENT HERE"
        val intent = Intent().getRootCommentsActivityIntent(context(), Pair(projectData.project(), projectData), comment)
        assertEquals(intent.component?.className, "com.kickstarter.ui.activities.CommentsActivity")
        assertEquals(intent.extras?.get(IntentKey.PROJECT), projectData.project())
        assertEquals(intent.extras?.get(IntentKey.PROJECT_DATA), projectData)
        assertEquals(intent.extras?.get(IntentKey.COMMENT), comment)
    }

    @Test
    fun testGetUpdatesActivityIntent() {
        val project = ProjectFactory.project()
        val intent = Intent().getUpdatesActivityIntent(context(), project)
        assertEquals(intent.component?.className, "com.kickstarter.ui.activities.UpdateActivity")
        assertEquals(intent.extras?.get(IntentKey.PROJECT), project)
        assertNull(intent.extras?.get(IntentKey.UPDATE_POST_ID))
        assertNull(intent.extras?.get(IntentKey.IS_UPDATE_COMMENT))
    }

    @Test
    fun testGetCreatorDashboardIntent() {
        val project = ProjectFactory.project()
        val intent = Intent().getCreatorDashboardActivityIntent(context(), project)
        assertEquals(intent.component?.className, "com.kickstarter.ui.activities.CreatorDashboardActivity")
        assertEquals(intent.extras?.get(IntentKey.PROJECT), project)
    }

    @Test
    fun testGetCreatorBioIntent() {
        val project = ProjectFactory.project()
        val intent = Intent().getCreatorBioWebViewActivityIntent(context(), project)
        assertEquals(intent.component?.className, "com.kickstarter.ui.activities.CreatorBioActivity")
        assertEquals(intent.extras?.get(IntentKey.PROJECT), project)
        assertEquals(intent.extras?.get(IntentKey.URL), project.creatorBioUrl())
    }

    @Test
    fun testGetProjectUpdatesIntent() {
        val projectData = ProjectDataFactory.project(ProjectFactory.project())
        val intent = Intent().getProjectUpdatesActivityIntent(context(), Pair(projectData.project(), projectData))
        assertEquals(intent.component?.className, "com.kickstarter.ui.activities.ProjectUpdatesActivity")
        assertEquals(intent.extras?.get(IntentKey.PROJECT), projectData.project())
        assertEquals(intent.extras?.get(IntentKey.PROJECT_DATA), projectData)
    }

    @Test
    fun testGetUpdatesActivityIntent_forDeepLinkPostIdUpdate() {
        val project = ProjectFactory.project()
        val postId = "Some post id"
        val intent = Intent().getUpdatesActivityIntent(context(), project, postId)
        assertEquals(intent.component?.className, "com.kickstarter.ui.activities.UpdateActivity")
        assertEquals(intent.extras?.get(IntentKey.PROJECT), project)
        assertEquals(intent.extras?.get(IntentKey.UPDATE_POST_ID), postId)
        assertNull(intent.extras?.get(IntentKey.IS_UPDATE_COMMENT))
    }

    @Test
    fun testGetUpdatesActivityIntent_forDeepLinkPostIdUpdateComment() {
        val project = ProjectFactory.project()
        val postId = "Some post id"
        val comment = "SomeCommentId"
        val isCommentForUpdate = true
        val intent = Intent().getUpdatesActivityIntent(context(), project, postId, isCommentForUpdate, comment)
        assertEquals(intent.component?.className, "com.kickstarter.ui.activities.UpdateActivity")
        assertEquals(intent.extras?.get(IntentKey.PROJECT), project)
        assertEquals(intent.extras?.get(IntentKey.UPDATE_POST_ID), postId)
        assertEquals(intent.extras?.get(IntentKey.IS_UPDATE_COMMENT), isCommentForUpdate)
        assertEquals(intent.extras?.get(IntentKey.COMMENT), comment)
    }

    @Test
    fun testResetPasswordIntent() {
        val intent = Intent().getResetPasswordIntent(context())
        assertEquals(intent.component?.className, "com.kickstarter.ui.activities.ResetPasswordActivity")
        assertEquals(intent.extras?.get(IntentKey.EMAIL), null)

        val intent2 = Intent().getResetPasswordIntent(context(), email = "test@kickstarter.com")
        assertEquals(intent2.component?.className, "com.kickstarter.ui.activities.ResetPasswordActivity")
        assertEquals(intent2.extras?.get(IntentKey.EMAIL), "test@kickstarter.com")
    }
}
