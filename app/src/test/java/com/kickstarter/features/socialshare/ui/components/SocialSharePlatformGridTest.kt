package com.kickstarter.features.socialshare.ui.components

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.socialshare.data.SocialSharePlatform
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class SocialSharePlatformGridTest : KSRobolectricTestCase() {

    @Test
    fun `SocialSharePlatformGrid grid container is displayed`() {
        composeTestRule.setContent {
            KSTheme {
                SocialSharePlatformGrid(
                    platforms = SocialSharePlatform.entries,
                    onPlatformSelected = {},
                    onCopyLinkSelected = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SocialSharePlatformGridTestTag.GRID.name)
            .assertIsDisplayed()
    }

    @Test
    fun `SocialSharePlatformGrid shows all platforms when full list is provided`() {
        composeTestRule.setContent {
            KSTheme {
                SocialSharePlatformGrid(
                    platforms = SocialSharePlatform.entries,
                    onPlatformSelected = {},
                    onCopyLinkSelected = {}
                )
            }
        }

        // Verify every platform button node exists, carries Role.Button so TalkBack
        // announces it as an actionable element, and exposes an unambiguous content
        // description (the accessibilityLabel, not the short visual label).
        // Labels are not used here because INSTAGRAM_FEED/FACEBOOK_FEED both use "Feed"
        // and INSTAGRAM_STORIES/FACEBOOK_STORIES both use "Stories".
        SocialSharePlatform.entries.forEach { platform ->
            composeTestRule
                .onNodeWithTag(platform.name, useUnmergedTree = true)
                .assertExists()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(platform.accessibilityLabel())))
        }
    }

    @Test
    fun `SocialSharePlatformGrid shows only the provided subset of platforms`() {
        val subset = listOf(
            SocialSharePlatform.COPY_LINK,
            SocialSharePlatform.EMAIL,
            SocialSharePlatform.MORE
        )
        val excluded = SocialSharePlatform.entries - subset.toSet()

        composeTestRule.setContent {
            KSTheme {
                SocialSharePlatformGrid(
                    platforms = subset,
                    onPlatformSelected = {},
                    onCopyLinkSelected = {}
                )
            }
        }

        subset.forEach { platform ->
            composeTestRule
                .onNodeWithTag(platform.name, useUnmergedTree = true)
                .assertExists()
        }

        excluded.forEach { platform ->
            composeTestRule
                .onNodeWithTag(platform.name, useUnmergedTree = true)
                .assertDoesNotExist()
        }
    }

    @Test
    fun `SocialSharePlatformGrid shows unique platform labels`() {
        composeTestRule.setContent {
            KSTheme {
                SocialSharePlatformGrid(
                    platforms = SocialSharePlatform.entries,
                    onPlatformSelected = {},
                    onCopyLinkSelected = {}
                )
            }
        }

        // TODO: Evaluate if Copy Link, More and Email need to be translated

        // Visual labels — short text rendered below each icon
        composeTestRule.onNodeWithText("Copy link").assertIsDisplayed()
        composeTestRule.onNodeWithText("X").assertIsDisplayed()
        composeTestRule.onNodeWithText("Whatsapp").assertIsDisplayed()
        composeTestRule.onNodeWithText("Messages").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("More").assertIsDisplayed()

        // Accessibility labels — unambiguous descriptions announced by TalkBack.
        // Platforms that share a visual label ("Feed", "Stories") are verified
        // separately here to confirm each has a unique accessible name.
        composeTestRule.onNodeWithContentDescription("Copy project link").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share to Instagram Feed").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share to Instagram Stories").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share to X").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share to Facebook Feed").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share to Facebook Stories").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share via WhatsApp").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share via Messages").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share via Email").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("More sharing options").assertIsDisplayed()
    }

    @Test
    fun `SocialSharePlatformGrid empty platform list renders empty grid`() {
        composeTestRule.setContent {
            KSTheme {
                SocialSharePlatformGrid(
                    platforms = emptyList(),
                    onPlatformSelected = {},
                    onCopyLinkSelected = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SocialSharePlatformGridTestTag.GRID.name)
            .assertIsDisplayed()

        SocialSharePlatform.entries.forEach { platform ->
            composeTestRule
                .onNodeWithTag(platform.name, useUnmergedTree = true)
                .assertDoesNotExist()
        }
    }

    @Test
    fun `SocialSharePlatformGrid calls onCopyLinkSelected when COPY_LINK is clicked`() {
        val onCopyLinkSelected: () -> Unit = mock()
        val onPlatformSelected: (SocialSharePlatform) -> Unit = mock()

        composeTestRule.setContent {
            KSTheme {
                SocialSharePlatformGrid(
                    platforms = SocialSharePlatform.entries,
                    onPlatformSelected = onPlatformSelected,
                    onCopyLinkSelected = onCopyLinkSelected
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SocialSharePlatform.COPY_LINK.name, useUnmergedTree = true)
            .performClick()

        verify(onCopyLinkSelected).invoke()
        verify(onPlatformSelected, never()).invoke(SocialSharePlatform.COPY_LINK)
    }

    @Test
    fun `SocialSharePlatformGrid does not call onCopyLinkSelected when a regular platform is clicked`() {
        val onCopyLinkSelected: () -> Unit = mock()

        composeTestRule.setContent {
            KSTheme {
                SocialSharePlatformGrid(
                    platforms = SocialSharePlatform.entries,
                    onPlatformSelected = {},
                    onCopyLinkSelected = onCopyLinkSelected
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SocialSharePlatform.WHATSAPP.name, useUnmergedTree = true)
            .performClick()

        verify(onCopyLinkSelected, never()).invoke()
    }

    @Test
    fun `SocialSharePlatformGrid fires correct callback for each non-copy-link platform`() {
        val platforms = listOf(
            SocialSharePlatform.INSTAGRAM_FEED,
            SocialSharePlatform.X,
            SocialSharePlatform.WHATSAPP,
            SocialSharePlatform.MESSAGES,
            SocialSharePlatform.EMAIL,
            SocialSharePlatform.MORE
        )
        val onPlatformSelected: (SocialSharePlatform) -> Unit = mock()

        composeTestRule.setContent {
            KSTheme {
                SocialSharePlatformGrid(
                    platforms = platforms,
                    onPlatformSelected = onPlatformSelected,
                    onCopyLinkSelected = {}
                )
            }
        }

        platforms.forEach { platform ->
            composeTestRule
                .onNodeWithTag(platform.name, useUnmergedTree = true)
                .performClick()

            verify(onPlatformSelected).invoke(platform)
        }
    }
}
