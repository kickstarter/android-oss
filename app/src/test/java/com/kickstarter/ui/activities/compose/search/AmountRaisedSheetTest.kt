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

class AmountRaisedSheetTest : KSRobolectricTestCase() {

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    @Test
    fun `All buckets displayed and showing proper text`() {

        composeTestRule.setContent {
            KSTheme {
                AmountRaisedSheet()
            }
        }

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Amount_raised_fpo))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_0)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Amount_raised_bucket_0_fpo))
            .isDisplayed()

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_1)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Amount_raised_bucket_1_fpo))
            .isDisplayed()

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_2)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Amount_raised_bucket_2_fpo))
            .isDisplayed()

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_3)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Amount_raised_bucket_3_fpo))
            .isDisplayed()

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_4)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Amount_raised_bucket_4_fpo))
            .isDisplayed()
    }
}
