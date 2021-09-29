package com.kickstarter.libs.utils.extensions

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class IntentExtTest : KSRobolectricTestCase() {

    @Test
    fun testGetProjectIntent_whenFeatureFlagTrue_shouldReturnProjectPageActivity() {
        assertEquals(Intent().getProjectIntent(context(), true).component?.className , "com.kickstarter.ui.activities.ProjectPageActivity" )
    }

    @Test
    fun testGetProjectIntent_whenFeatureFlagFalse_shouldReturnProjectActivity() {
        assertEquals(Intent().getProjectIntent(context(), false).component?.className , "com.kickstarter.ui.activities.ProjectActivity" )
    }
}