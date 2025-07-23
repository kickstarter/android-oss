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

class GoalSheetTest : KSRobolectricTestCase() {

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `All goal buckets displayed and showing proper text`() {
        composeTestRule.setContent {
            KSTheme {
                GoalSheet()
            }
        }

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Goal))
            .assertIsDisplayed()

        DiscoveryParams.GoalBuckets.values().forEach { bucket ->
            composeTestRule
                .onNodeWithTag(GoalTestTags.bucketTag(bucket))
                .assertIsDisplayed()

            composeTestRule
                .onNodeWithText(
                    context.resources.getString(
                        context.resources.getIdentifier(
                            "Bucket_${bucket.name.last().digitToInt()}_fpo",
                            "string",
                            context.packageName
                        )
                    )
                )
                .isDisplayed()
        }
    }

    @Test
    fun `Initial buttons state`() {
        composeTestRule.setContent {
            KSTheme {
                GoalSheet()
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
    fun `Buttons state when one goal bucket is selected`() {
        composeTestRule.setContent {
            KSTheme {
                GoalSheet()
            }
        }

        composeTestRule
            .onNodeWithTag(GoalTestTags.bucketTag(DiscoveryParams.GoalBuckets.BUCKET_3))
            .assertIsDisplayed()
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
        var selectedBucket: DiscoveryParams.GoalBuckets? = null

        composeTestRule.setContent {
            KSTheme {
                GoalSheet(
                    onApply = { bucket, _ ->
                        selectedBucket = bucket
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(GoalTestTags.bucketTag(DiscoveryParams.GoalBuckets.BUCKET_2))
            .performClick()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .performClick()

        assertEquals(null, selectedBucket)
    }

    @Test
    fun `Buttons actions for right button (see results)`() {
        var selectedBucket: DiscoveryParams.GoalBuckets? = null
        var shouldApply: Boolean? = null

        composeTestRule.setContent {
            KSTheme {
                GoalSheet(
                    onApply = { bucket, apply ->
                        selectedBucket = bucket
                        shouldApply = apply
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(GoalTestTags.bucketTag(DiscoveryParams.GoalBuckets.BUCKET_4))
            .performClick()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .performClick()

        assertEquals(DiscoveryParams.GoalBuckets.BUCKET_4, selectedBucket)
        assertEquals(true, shouldApply)
    }
}
