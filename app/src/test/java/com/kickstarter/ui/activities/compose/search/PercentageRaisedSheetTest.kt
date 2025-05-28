package com.kickstarter.ui.activities.compose.search

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class PercentageRaisedSheetTest : KSRobolectricTestCase() {

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `All buckets displayed and showing proper text`() {

        composeTestRule.setContent {
            KSTheme {
                PercentageRaisedSheet()
            }
        }

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Percentage_raised_fpo))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(
                PercentageRaisedTestTags.bucketTag(DiscoveryParams.RaisedBuckets.BUCKET_0)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Percentage_raised_bucket_0))
            .isDisplayed()

        composeTestRule
            .onNodeWithTag(
                PercentageRaisedTestTags.bucketTag(DiscoveryParams.RaisedBuckets.BUCKET_1)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Percentage_raised_bucket_1))
            .isDisplayed()

        composeTestRule
            .onNodeWithTag(
                PercentageRaisedTestTags.bucketTag(DiscoveryParams.RaisedBuckets.BUCKET_2)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Percentage_raised_bucket_2))
            .isDisplayed()
    }
}
