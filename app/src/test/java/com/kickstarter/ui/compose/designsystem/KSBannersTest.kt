package com.kickstarter.ui.compose.designsystem

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
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
    fun `test banner has button role for TalkBack`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoFeedBanner(onButtonClick = {})
            }
        }

        // - TalkBack announces the banner as a "Button"
        composeTestRule.onNodeWithTag(KSVideoFeedBannerTestTag.BANNER_CONTAINER.name)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }

    @Test
    fun `test banner merged semantics contains title and description text`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoFeedBanner(onButtonClick = {})
            }
        }

        // - The merged node (what TalkBack reads) should contain both texts
        composeTestRule.onNodeWithTag(KSVideoFeedBannerTestTag.BANNER_CONTAINER.name)
            .assertTextContains("Try our new discovery mode", substring = true)
            .assertTextContains("Swipe through a video feed", substring = true)
    }

    @Test
    fun `test title is displayed`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoFeedBanner(onButtonClick = {})
            }
        }

        composeTestRule.onNodeWithTag(KSVideoFeedBannerTestTag.BANNER_TITLE.name, useUnmergedTree = true)
            .assertIsDisplayed()
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
    fun `test banner image is decorative with no content description`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoFeedBanner(onButtonClick = {})
            }
        }

        // - Image should exist but have no contentDescription (decorative)
        composeTestRule.onNodeWithTag(KSVideoFeedBannerTestTag.BANNER_IMAGE.name, useUnmergedTree = true)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
    }
}
