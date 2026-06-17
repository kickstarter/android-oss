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

        // Verify bookmark button and count (unsaved → accessible name "Save")
        val bookmarkDesc = context().getString(R.string.Save)
        composeTestRule.onNodeWithContentDescription(bookmarkDesc).assertIsDisplayed()
        composeTestRule.onNodeWithText("1.2k").assertIsDisplayed()
        composeTestRule.onNodeWithTag(KSVideoActionsColumnTestTag.BOOKMARK_BUTTON.name, useUnmergedTree = true)
            .assertExists()

        // Verify share button and count. The count is exposed via stateDescription; the visible
        // number is accessibility-decorative, so it isn't part of the semantics text tree.
        val shareDesc = context().getString(R.string.Share)
        composeTestRule.onNodeWithContentDescription(shareDesc)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "45"))
        composeTestRule.onNodeWithTag(KSVideoActionsColumnTestTag.SHARE_BUTTON.name, useUnmergedTree = true)
            .assertExists()

        // More options button exists in the tree (reserves space) but is invisible
        val moreDesc = context().getString(R.string.More_options)
        composeTestRule.onNodeWithContentDescription(moreDesc).assertExists()
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
    fun `KSVideoActionsColumn bookmark stateDescription is Saved when bookmarked`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoActionsColumn(
                    bookmarkCount = "1.2k",
                    isBookmarked = true,
                    shareCount = "45"
                )
            }
        }

        // Stable accessible name ("Save") + saved state via stateDescription ("Saved"),
        // so TalkBack reads "Save, 1.2k, Saved". The count comes from the visible label only.
        val saveDesc = context().getString(R.string.Save)
        val savedState = context().getString(R.string.Saved)
        composeTestRule.onNodeWithContentDescription(saveDesc)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, savedState))
    }

    @Test
    fun `KSVideoActionsColumn bookmark stateDescription is empty when not bookmarked`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoActionsColumn(
                    bookmarkCount = "1.2k",
                    isBookmarked = false,
                    shareCount = "45"
                )
            }
        }

        // Unsaved has no extra state to announce; the accessible name ("Save") already conveys the
        // action, so stateDescription is empty and TalkBack reads "Save, 1.2k" (not "Save, 1.2k, Save").
        val saveDesc = context().getString(R.string.Save)
        composeTestRule.onNodeWithContentDescription(saveDesc)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, ""))
    }

    @Test
    fun `KSVideoActionsColumn share stateDescription is the count`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoActionsColumn(
                    bookmarkCount = "1.2k",
                    shareCount = "45"
                )
            }
        }

        // Share has no on/off state, so the count is its stateDescription. The visible number is
        // decorative, so TalkBack reads "Share, 45" once instead of "Share, 45, 45".
        val shareDesc = context().getString(R.string.Share)
        composeTestRule.onNodeWithContentDescription(shareDesc)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "45"))
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

        // Not bookmarked by default, so the bookmark button's accessible name is "Save".
        composeTestRule.onNodeWithContentDescription(context().getString(R.string.Save)).performClick()
        verify(onBookmarkClick).invoke()

        composeTestRule.onNodeWithContentDescription(context().getString(R.string.Share)).performClick()
        verify(onShareClick).invoke()
    }

    @Test
    fun `more options button is not interactable in phase 1 of videoFeed but occupies space`() {
        val onMoreClick: () -> Unit = mock()

        composeTestRule.setContent {
            KSTheme {
                KSVideoActionsColumn(
                    onMoreOptionsClick = onMoreClick
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoActionsColumnTestTag.MORE_OPTIONS_BUTTON.name, useUnmergedTree = true)
            .assertExists()
            .performClick()

        verify(onMoreClick, org.mockito.Mockito.never()).invoke()
    }

    @Test
    fun `bookmark button accessible name is Save when not bookmarked`() {
        val saveDesc = context().getString(R.string.Save)

        composeTestRule.setContent {
            KSTheme {
                KSVideoActionsColumn(isBookmarked = false)
            }
        }

        composeTestRule.onNodeWithContentDescription(saveDesc).assertIsDisplayed()
    }

    @Test
    fun `bookmark button accessible name stays Save when bookmarked`() {
        // The name is stable across states; the saved/unsaved distinction lives in the
        // stateDescription, not the name.
        val saveDesc = context().getString(R.string.Save)

        composeTestRule.setContent {
            KSTheme {
                KSVideoActionsColumn(isBookmarked = true)
            }
        }

        composeTestRule.onNodeWithContentDescription(saveDesc).assertIsDisplayed()
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
