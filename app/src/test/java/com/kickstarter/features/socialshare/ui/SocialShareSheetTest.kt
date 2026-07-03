package com.kickstarter.features.socialshare.ui

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.socialshare.SocialShareService
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.data.SocialSharePlatform
import com.kickstarter.features.socialshare.viewmodel.SocialShareViewModel
import com.kickstarter.libs.utils.EventContextValues.ContextPageName
import com.kickstarter.ui.compose.designsystem.KSTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SocialShareSheetTest : KSRobolectricTestCase() {

    private val shareData = SocialShareData(
        projectName = "Ringo Move - The Ultimate Workout Bottle",
        projectUrl = "https://www.kickstarter.com/projects/ringo/ringo-move",
        imageUrl = "https://example.com/image.jpg",
        creatorName = "Ringo"
    )

    private val fakeImageUri: Uri =
        Uri.parse("content://com.kickstarter.fileprovider/share_images/kickstarter_share.png")

    @Test
    fun `sheet content is displayed when isVisible is true`() {
        val viewModel = SocialShareViewModel(environment(), FakeSocialShareService(), shareData, ContextPageName.VIDEO_FEED, UnconfinedTestDispatcher())

        composeTestRule.setContent {
            KSTheme {
                Surface {
                    CompositionLocalProvider(LocalSocialShareViewModel provides viewModel) {
                        SocialShareSheet(
                            shareData = shareData,
                            isVisible = true,
                            onDismiss = {},
                            onIntentReady = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Share project").assertIsDisplayed()
    }

    @Test
    fun `sheet content is not shown when isVisible is false`() {
        val viewModel = SocialShareViewModel(environment(), FakeSocialShareService(), shareData, ContextPageName.VIDEO_FEED, UnconfinedTestDispatcher())

        composeTestRule.setContent {
            KSTheme {
                Surface {
                    CompositionLocalProvider(LocalSocialShareViewModel provides viewModel) {
                        SocialShareSheet(
                            shareData = shareData,
                            isVisible = false,
                            onDismiss = {},
                            onIntentReady = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Share project").assertDoesNotExist()
    }

    @Test
    fun `sheet shows only platforms returned by the service`() {
        val platforms = listOf(SocialSharePlatform.X, SocialSharePlatform.EMAIL, SocialSharePlatform.MORE)
        val viewModel = SocialShareViewModel(
            environment(),
            object : FakeSocialShareService() {
                override fun getInstalledPlatforms() = platforms
            },
            shareData,
            ContextPageName.VIDEO_FEED,
            UnconfinedTestDispatcher()
        )

        composeTestRule.setContent {
            KSTheme {
                Surface {
                    CompositionLocalProvider(LocalSocialShareViewModel provides viewModel) {
                        SocialShareSheet(
                            shareData = shareData,
                            isVisible = true,
                            onDismiss = {},
                            onIntentReady = {}
                        )
                    }
                }
            }
        }

        platforms.forEach { platform ->
            composeTestRule.onNodeWithTag(platform.name, useUnmergedTree = true).assertExists()
        }

        val excluded = SocialSharePlatform.entries - platforms.toSet()
        excluded.forEach { platform ->
            composeTestRule.onNodeWithTag(platform.name, useUnmergedTree = true).assertDoesNotExist()
        }
    }

    @Test
    fun `clicking a platform fires onIntentReady with the built intent`() {
        val fakeIntent = Intent(Intent.ACTION_SEND)
        var capturedIntent: Intent? = null

        val viewModel = SocialShareViewModel(
            environment(),
            object : FakeSocialShareService() {
                override fun getInstalledPlatforms() = listOf(SocialSharePlatform.X)
                override fun buildIntent(platform: SocialSharePlatform, shareData: SocialShareData, imageUri: Uri?) = fakeIntent
            },
            shareData,
            ContextPageName.VIDEO_FEED,
            UnconfinedTestDispatcher()
        )

        composeTestRule.setContent {
            KSTheme {
                Surface {
                    CompositionLocalProvider(LocalSocialShareViewModel provides viewModel) {
                        SocialShareSheet(
                            shareData = shareData,
                            isVisible = true,
                            onDismiss = {},
                            onIntentReady = { capturedIntent = it }
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(SocialSharePlatform.X.name, useUnmergedTree = true).performClick()

        assertEquals(fakeIntent, capturedIntent)
    }

    @Test
    fun `clicking a platform when buildIntent returns null shows error snackbar`() {
        val snackbarHostState = SnackbarHostState()
        val viewModel = SocialShareViewModel(
            environment(),
            object : FakeSocialShareService() {
                override fun getInstalledPlatforms() = listOf(SocialSharePlatform.X)
                override fun buildIntent(platform: SocialSharePlatform, shareData: SocialShareData, imageUri: Uri?): Intent? = null
            },
            shareData,
            ContextPageName.VIDEO_FEED,
            UnconfinedTestDispatcher()
        )

        composeTestRule.setContent {
            KSTheme {
                Surface {
                    CompositionLocalProvider(LocalSocialShareViewModel provides viewModel) {
                        SocialShareSheet(
                            shareData = shareData,
                            isVisible = true,
                            onDismiss = {},
                            onIntentReady = {},
                            snackbarHostState = snackbarHostState
                        )
                    }
                    SnackbarHost(hostState = snackbarHostState)
                }
            }
        }

        composeTestRule.onNodeWithTag(SocialSharePlatform.X.name, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Could not open X").assertIsDisplayed()
    }

    @Test
    fun `clicking Copy Link shows Link copied snackbar`() {
        val snackbarHostState = SnackbarHostState()
        val viewModel = SocialShareViewModel(
            environment(),
            object : FakeSocialShareService() {
                override fun getInstalledPlatforms() = listOf(SocialSharePlatform.COPY_LINK)
            },
            shareData,
            ContextPageName.VIDEO_FEED,
            UnconfinedTestDispatcher()
        )

        composeTestRule.setContent {
            KSTheme {
                Surface {
                    CompositionLocalProvider(LocalSocialShareViewModel provides viewModel) {
                        SocialShareSheet(
                            shareData = shareData,
                            isVisible = true,
                            onDismiss = {},
                            onIntentReady = {},
                            snackbarHostState = snackbarHostState
                        )
                    }
                    SnackbarHost(hostState = snackbarHostState)
                }
            }
        }

        composeTestRule.onNodeWithTag(SocialSharePlatform.COPY_LINK.name, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Link copied!").assertIsDisplayed()
    }

    @Test
    fun `Share project header has heading semantics`() {
        val viewModel = SocialShareViewModel(environment(), FakeSocialShareService(), shareData, ContextPageName.VIDEO_FEED, UnconfinedTestDispatcher())

        composeTestRule.setContent {
            KSTheme {
                Surface {
                    CompositionLocalProvider(LocalSocialShareViewModel provides viewModel) {
                        SocialShareSheet(
                            shareData = shareData,
                            isVisible = true,
                            onDismiss = {},
                            onIntentReady = {}
                        )
                    }
                }
            }
        }

        composeTestRule
            .onNodeWithText("Share project")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }

    @Test
    fun `project card container is accessible via sheet-level test tag`() {
        val viewModel = SocialShareViewModel(environment(), FakeSocialShareService(), shareData, ContextPageName.VIDEO_FEED, UnconfinedTestDispatcher())

        composeTestRule.setContent {
            KSTheme {
                Surface {
                    CompositionLocalProvider(LocalSocialShareViewModel provides viewModel) {
                        SocialShareSheet(
                            shareData = shareData,
                            isVisible = true,
                            onDismiss = {},
                            onIntentReady = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(SocialShareSheetTestTag.PROJECT_CARD.name).assertIsDisplayed()
    }

    @Test
    fun `platform grid container is accessible via sheet-level test tag`() {
        val viewModel = SocialShareViewModel(environment(), FakeSocialShareService(), shareData, ContextPageName.VIDEO_FEED, UnconfinedTestDispatcher())

        composeTestRule.setContent {
            KSTheme {
                Surface {
                    CompositionLocalProvider(LocalSocialShareViewModel provides viewModel) {
                        SocialShareSheet(
                            shareData = shareData,
                            isVisible = true,
                            onDismiss = {},
                            onIntentReady = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(SocialShareSheetTestTag.PLATFORM_GRID.name).assertIsDisplayed()
    }

    /**
     * Default no-op fake; override individual methods in each test.
     */
    private open inner class FakeSocialShareService : SocialShareService {
        override fun getInstalledPlatforms(): List<SocialSharePlatform> = listOf(
            SocialSharePlatform.X,
            SocialSharePlatform.WHATSAPP,
            SocialSharePlatform.EMAIL,
            SocialSharePlatform.MORE
        )
        override fun copyToClipboard(label: String, url: String) {}
        override suspend fun loadShareImage(imageUrl: String): Bitmap? {
            kotlinx.coroutines.awaitCancellation()
        }
        override suspend fun cacheShareImage(bitmap: Bitmap): Uri = fakeImageUri
        override fun buildIntent(
            platform: SocialSharePlatform,
            shareData: SocialShareData,
            imageUri: Uri?
        ): Intent? = Intent(Intent.ACTION_SEND)
    }
}
