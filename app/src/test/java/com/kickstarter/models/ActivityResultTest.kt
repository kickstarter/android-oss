package com.kickstarter.models

import android.app.Activity
import android.content.Intent
import com.kickstarter.ui.data.ActivityResult
import junit.framework.TestCase
import org.junit.Test

class ActivityResultTest : TestCase() {

    @Test
    fun testDefaultInit() {
        val intent = Intent()

        val activityResult = ActivityResult.builder()
            .resultCode(Activity.RESULT_OK)
            .requestCode(2)
            .intent(intent)
            .build()

        assertEquals(activityResult.requestCode(), 2)
        assertEquals(activityResult.resultCode(), Activity.RESULT_OK)
        assertEquals(activityResult.intent(), intent)
    }

    @Test
    fun testActivityResult_equalFalse() {
        val intent = Intent()

        val activityResult = ActivityResult.builder().build()
        val activityResult2 = ActivityResult.builder().resultCode(1)
            .requestCode(2).build()
        val activityResult3 = ActivityResult.create(1, 2, intent)
        assertFalse(activityResult == activityResult2)
        assertFalse(activityResult == activityResult3)

        assertFalse(activityResult3 == activityResult2)
    }

    @Test
    fun testActivityResult_equalTrue() {
        val activityResult1 = ActivityResult.builder().build()
        val activityResult2 = ActivityResult.builder().build()

        assertEquals(activityResult1, activityResult2)
    }

    @Test
    fun testActivityResultToBuilder() {
        val intent = Intent()
        val activityResult = ActivityResult.builder().build().toBuilder()
            .intent(intent).build()

        assertEquals(activityResult.intent(), intent)
    }

    @Test
    fun testActivityResultIsOk() {
        val activityResult = ActivityResult.builder().build().toBuilder()
            .resultCode(Activity.RESULT_OK).build()

        assertFalse(activityResult.isCanceled)
        assertTrue(activityResult.isOk)
    }

    @Test
    fun testActivityResultIsCancelled() {
        val activityResult = ActivityResult.builder().build().toBuilder()
            .resultCode(Activity.RESULT_CANCELED).build()

        assertTrue(activityResult.isCanceled)
        assertFalse(activityResult.isOk)
    }
}
