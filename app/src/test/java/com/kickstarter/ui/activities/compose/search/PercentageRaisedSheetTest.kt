package com.kickstarter.ui.activities.compose.search

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.BottomSheetFooterTestTags
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
            .onNodeWithText(context.resources.getString(R.string.Percentage_raised))
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

    @Test
    fun `Initial buttons state`() {
        composeTestRule.setContent {
            KSTheme {
                AmountRaisedSheet()
            }
        }

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .assertIsEnabled()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .assertIsNotEnabled()
    }

    @Test
    fun `Buttons state when one bucket is selected`() {
        composeTestRule.setContent {
            KSTheme {
                PercentageRaisedSheet()
            }
        }

        composeTestRule
            .onNodeWithTag(
                PercentageRaisedTestTags.bucketTag(DiscoveryParams.RaisedBuckets.BUCKET_2)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(
                PercentageRaisedTestTags.bucketTag(DiscoveryParams.RaisedBuckets.BUCKET_2)
            )
            .performClick()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .assertIsEnabled()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .assertIsEnabled()
    }
}
