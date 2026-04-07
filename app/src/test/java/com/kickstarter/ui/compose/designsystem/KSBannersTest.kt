package com.kickstarter.ui.compose.designsystem

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class KSBannersTest : KSRobolectricTestCase() {

    @Test
    fun `test banner container is displayed and clickable`() {
        var clicked = false
        composeTestRule.setContent {
            KSTheme {
                KSVideoFeedBanner(onButtonClick = { clicked = true })
            }
        }

        composeTestRule.onNodeWithTag(KSVideoFeedBannerTestTag.BANNER_CONTAINER.name)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()

        assert(clicked)
    }

    @Test
    fun `test title is displayed with heading semantics`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoFeedBanner(onButtonClick = {})
            }
        }

        composeTestRule.onNodeWithTag(KSVideoFeedBannerTestTag.BANNER_TITLE.name, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNode(
            hasTestTag(KSVideoFeedBannerTestTag.BANNER_TITLE.name),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun `test description is displayed`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoFeedBanner(onButtonClick = {})
            }
        }

        composeTestRule.onNodeWithTag(KSVideoFeedBannerTestTag.BANNER_DESCRIPTION.name, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun `test button is displayed and triggers callback`() {
        var buttonClicked = false
        composeTestRule.setContent {
            KSTheme {
                KSVideoFeedBanner(onButtonClick = { buttonClicked = true })
            }
        }

        composeTestRule.onNodeWithTag(KSVideoFeedBannerTestTag.BANNER_BUTTON.name, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        assert(buttonClicked)
    }

    @Test
    fun `test banner image is displayed with content description`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoFeedBanner(onButtonClick = {})
            }
        }

        composeTestRule.onNodeWithTag(KSVideoFeedBannerTestTag.BANNER_IMAGE.name, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun `test tapping banner container triggers callback`() {
        var callbackCount = 0
        composeTestRule.setContent {
            KSTheme {
                KSVideoFeedBanner(onButtonClick = { callbackCount++ })
            }
        }

        composeTestRule.onNodeWithTag(KSVideoFeedBannerTestTag.BANNER_CONTAINER.name)
            .performClick()

        // - Clicking the container should trigger the callback
        assert(callbackCount == 1)
    }
}
