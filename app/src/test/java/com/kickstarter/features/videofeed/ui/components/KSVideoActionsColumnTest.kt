package com.kickstarter.features.videofeed.ui.components

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class KSVideoActionsColumnTest : KSRobolectricTestCase() {

    @Test
    fun `KSVideoActionsColumn shows all buttons and labels when provided`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoActionsColumn(
                    profileImageUrl = "some_url",
                    bookmarkCount = "1.2k",
                    shareCount = "45"
                )
            }
        }

        // Verify profile button
        val profileDesc = context().getString(R.string.fpo_Profile)
        composeTestRule.onNodeWithContentDescription(profileDesc).assertIsDisplayed()

        // Verify bookmark button and count
        val bookmarkDesc = context().getString(R.string.fpo_Bookmark)
        composeTestRule.onNodeWithContentDescription(bookmarkDesc).assertIsDisplayed()
        composeTestRule.onNodeWithText("1.2k").assertIsDisplayed()
        composeTestRule.onNodeWithTag(KSVideoActionsColumnTestTag.BOOKMARK_BUTTON.name, useUnmergedTree = true)
            .assertExists()

        // Verify share button and count
        val shareDesc = context().getString(R.string.fpo_Share)
        composeTestRule.onNodeWithContentDescription(shareDesc).assertIsDisplayed()
        composeTestRule.onNodeWithText("45").assertIsDisplayed()
        composeTestRule.onNodeWithTag(KSVideoActionsColumnTestTag.SHARE_BUTTON.name, useUnmergedTree = true)
            .assertExists()

        // Verify more options button
        val moreDesc = context().getString(R.string.fpo_More_options)
        composeTestRule.onNodeWithContentDescription(moreDesc).assertIsDisplayed()
    }

    @Test
    fun `KSVideoActionsColumn hides profile and labels when not provided`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoActionsColumn()
            }
        }

        // Profile should be gone
        val profileDesc = context().getString(R.string.fpo_Profile)
        composeTestRule.onNodeWithContentDescription(profileDesc).assertDoesNotExist()

        // Bookmark and Share should exist (icons), but counts should not
        composeTestRule.onNodeWithText("1.2k").assertDoesNotExist()
        composeTestRule.onNodeWithText("45").assertDoesNotExist()
    }

    @Test
    fun `KSVideoActionsColumn accessibility stateDescription matches counts`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoActionsColumn(
                    bookmarkCount = "1.2k",
                    shareCount = "45"
                )
            }
        }

        val bookmarkDesc = context().getString(R.string.fpo_Bookmark)
        composeTestRule.onNodeWithContentDescription(bookmarkDesc)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "1.2k"))

        val shareDesc = context().getString(R.string.fpo_Share)
        composeTestRule.onNodeWithContentDescription(shareDesc)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "45"))
    }

    @Test
    fun `KSVideoActionsColumn accessibility stateDescription is empty when counts are null`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoActionsColumn(
                    bookmarkCount = null,
                    shareCount = null
                )
            }
        }

        val bookmarkDesc = context().getString(R.string.fpo_Bookmark)
        composeTestRule.onNodeWithContentDescription(bookmarkDesc)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, ""))

        val shareDesc = context().getString(R.string.fpo_Share)
        composeTestRule.onNodeWithContentDescription(shareDesc)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, ""))
    }

    @Test
    fun `KSVideoActionsColumn callbacks are executed on click`() {
        val onProfileClick: () -> Unit = mock()
        val onBookmarkClick: () -> Unit = mock()
        val onShareClick: () -> Unit = mock()
        val onMoreClick: () -> Unit = mock()

        composeTestRule.setContent {
            KSTheme {
                KSVideoActionsColumn(
                    profileImageUrl = "some_url",
                    bookmarkCount = "10",
                    shareCount = "5",
                    onProfileClick = onProfileClick,
                    onBookmarkClick = onBookmarkClick,
                    onShareClick = onShareClick,
                    onMoreOptionsClick = onMoreClick
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(context().getString(R.string.fpo_Profile)).performClick()
        verify(onProfileClick).invoke()

        composeTestRule.onNodeWithContentDescription(context().getString(R.string.fpo_Bookmark)).performClick()
        verify(onBookmarkClick).invoke()

        composeTestRule.onNodeWithContentDescription(context().getString(R.string.fpo_Share)).performClick()
        verify(onShareClick).invoke()

        composeTestRule.onNodeWithContentDescription(context().getString(R.string.fpo_More_options)).performClick()
        verify(onMoreClick).invoke()
    }

    @Test
    fun `bookmark button fires callback when isBookmarked is false`() {
        val onBookmarkClick: () -> Unit = mock()

        composeTestRule.setContent {
            KSTheme {
                KSVideoActionsColumn(
                    isBookmarked = false,
                    onBookmarkClick = onBookmarkClick
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoActionsColumnTestTag.BOOKMARK_BUTTON.name, useUnmergedTree = true)
            .performClick()

        verify(onBookmarkClick).invoke()
    }

    @Test
    fun `bookmark button fires callback when isBookmarked is true`() {
        val onBookmarkClick: () -> Unit = mock()

        composeTestRule.setContent {
            KSTheme {
                KSVideoActionsColumn(
                    isBookmarked = true,
                    onBookmarkClick = onBookmarkClick
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoActionsColumnTestTag.BOOKMARK_BUTTON.name, useUnmergedTree = true)
            .performClick()

        verify(onBookmarkClick).invoke()
    }
}
