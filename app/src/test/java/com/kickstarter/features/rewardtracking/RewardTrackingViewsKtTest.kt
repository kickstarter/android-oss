package com.kickstarter.features.rewardtracking

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ActivityFactory
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class RewardTrackingViewsKtTest: KSRobolectricTestCase() {

    private val trackingButton =
        composeTestRule.onNodeWithTag(RewardTrackingTestTag.TRACK_SHIPMENT_BUTTON.name)

    private val projectCardModal =
        composeTestRule.onNodeWithTag(RewardTrackingTestTag.PROJECT_CARD_MODAL.name)

    @Test
    fun `test tracking activity card clicks`() {
        var trackShipmentClicks = 0
        var projectClicks = 0
        composeTestRule.setContent {
            val activity = ActivityFactory.rewardShippedActivity()

            KSTheme {
                RewardTrackingActivityFeed(
                        trackingNumber = activity.trackingNumber() ?: "",
                        projectName = activity.project()?.name() ?: "",
                        photo = activity.project()?.photo(),
                        projectClicked = { projectClicks++ },
                        trackingButtonEnabled = !activity.trackingUrl().isNullOrEmpty(),
                        trackShipmentClicked = { trackShipmentClicks++ }
                    )
            }
        }

        trackingButton.performClick()
        projectCardModal.performClick()
        assertEquals(1, trackShipmentClicks)
        assertEquals(1, projectClicks)
    }

    @Test
    fun `test tracking button disabled when false`() {
        var trackShipmentClicks = 0
        var projectClicks = 0
        composeTestRule.setContent {
            val activity = ActivityFactory.rewardShippedActivity().toBuilder().trackingUrl(null).build()

            KSTheme {
                RewardTrackingActivityFeed(
                    trackingNumber = activity.trackingNumber() ?: "",
                    projectName = activity.project()?.name() ?: "",
                    photo = activity.project()?.photo(),
                    projectClicked = { projectClicks++ },
                    trackingButtonEnabled = !activity.trackingUrl().isNullOrEmpty(),
                    trackShipmentClicked = { trackShipmentClicks++ }
                )
            }
        }

        trackingButton.assertIsNotEnabled()
    }
}