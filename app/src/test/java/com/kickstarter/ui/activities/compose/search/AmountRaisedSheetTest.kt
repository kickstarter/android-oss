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
            .onNodeWithText(context.resources.getString(R.string.Bucket_0_fpo))
            .isDisplayed()

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_1)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Bucket_1_fpo))
            .isDisplayed()

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_2)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Bucket_2_fpo))
            .isDisplayed()

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_3)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Bucket_3_fpo))
            .isDisplayed()

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_4)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Bucket_4_fpo))
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
                AmountRaisedSheet()
            }
        }

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_4)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_4)
            )
            .performClick()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .assertIsEnabled()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .assertIsEnabled()
    }

    @Test
    fun `Buttons actions for left button (reset)`() {

        var bucket: DiscoveryParams.AmountBuckets? = null
        composeTestRule.setContent {
            KSTheme {
                AmountRaisedSheet(
                    onApply = { amountBucket, b ->
                        bucket = amountBucket
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_4)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_4)
            )
            .performClick()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .assertIsEnabled()

        assertEquals(bucket, DiscoveryParams.AmountBuckets.BUCKET_4)

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .performClick()

        assertEquals(bucket, null)
    }

    @Test
    fun `Buttons actions for right button (see results)`() {

        var bucket: DiscoveryParams.AmountBuckets? = null
        var shouldPropagateUpwards: Boolean? = null
        composeTestRule.setContent {
            KSTheme {
                AmountRaisedSheet(
                    onApply = { amountBucket, b ->
                        bucket = amountBucket
                        shouldPropagateUpwards = b
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_4)
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(
                AmountRaisedTestTags.bucketTag(DiscoveryParams.AmountBuckets.BUCKET_4)
            )
            .performClick()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .assertIsEnabled()

        assertEquals(bucket, DiscoveryParams.AmountBuckets.BUCKET_4)
        assertEquals(shouldPropagateUpwards, null)

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .performClick()

        assertEquals(shouldPropagateUpwards, true)
    }
}
