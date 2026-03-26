package com.kickstarter.features.projecstory.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.DataSource
import coil.request.ErrorResult
import coil.request.SuccessResult
import coil.test.FakeImageLoaderEngine
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.projectstory.data.RichTextItem
import com.kickstarter.features.projectstory.ui.ProjectStoryCaptionedImageTestTag
import com.kickstarter.features.projectstory.ui.RichTextItemPhotoComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoilApi::class)
class ProjectStoryComponentsTest : KSRobolectricTestCase() {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private fun fakeImageLoaderEngine() =
        FakeImageLoaderEngine.Builder()
            .intercept(
                { it == "https://www.example.com/blue.jpg" },
                {
                    delay(1500L)

                    SuccessResult(
                        GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            setColor(Color.BLUE)
                            setSize(100, 50)
                        },
                        it.request,
                        DataSource.MEMORY
                    )
                }
            )
            .default { ErrorResult(null, it.request, Throwable("Default interceptor error")) }
            .build()

    private fun setUpImageLoader(imageLoader: ImageLoader) {
        Coil.setImageLoader(imageLoader)
    }

    @Before
    fun before() {
        setUpImageLoader(
            ImageLoader.Builder(context)
                .components { add(fakeImageLoaderEngine()) }
                .build()
        )
    }

    @After
    fun after() {
        Coil.reset()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `test image loading + success with caption`() = runTest {
        val standardDispatcher = StandardTestDispatcher(testScheduler)

        setUpImageLoader(
            ImageLoader.Builder(context)
                .components { add(fakeImageLoaderEngine()) }
                .dispatcher(standardDispatcher)
                .interceptorDispatcher(standardDispatcher)
                .build()
        )

        val caption = "Aye aye, Caption"

        val imageUrl = "https://www.example.com/blue.jpg"
        val richTextItemPhoto = RichTextItem.Photo(
            __typename = "",
            url = imageUrl,
            altText = caption,
            caption = caption,
            asset = RichTextItem.Photo.Asset(
                url = imageUrl,
                altText = caption
            ),
        )

        composeTestRule.setContent {
            RichTextItemPhotoComponent(
                richTextItemPhoto
            )
        }

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.LOADING_INDICATOR.name))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.IMAGE.name))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
            .assertContentDescriptionEquals(caption)
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.CAPTION.name))
            .assertTextEquals(caption)
            .assertIsDisplayed()

        advanceUntilIdle()

        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.LOADING_INDICATOR.name))
            .assertDoesNotExist()

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.IMAGE.name))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
            .assertContentDescriptionEquals(caption)
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.CAPTION.name))
            .assertTextEquals(caption)
            .assertIsDisplayed()
    }

    @Test
    fun `test empty caption does not render`() {
        val caption = ""

        val imageUrl = ""
        val richTextItemPhoto = RichTextItem.Photo(
            __typename = "",
            url = imageUrl,
            altText = caption,
            caption = caption,
            asset = RichTextItem.Photo.Asset(
                url = imageUrl,
                altText = caption
            ),
        )

        composeTestRule.setContent {
            RichTextItemPhotoComponent(
                richTextItemPhoto
            )
        }

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.CAPTION.name))
            .assertDoesNotExist()
    }

    @Test
    fun `test link + click`() {
        val caption = "Aye aye, Caption"

        val imageUrl = ""
        val richTextItemPhoto = RichTextItem.Photo(
            __typename = "",
            url = imageUrl,
            altText = caption,
            caption = caption,
            asset = RichTextItem.Photo.Asset(
                url = imageUrl,
                altText = caption
            ),
        )

        val link = "https://www.example.com"
        var clickCount = 0
        val uriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                clickCount++
            }
        }

        composeTestRule.setContent {
            CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                RichTextItemPhotoComponent(
                    richTextItemPhoto,
                    link
                )
            }
        }

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.IMAGE.name), true)
            .performClick()

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.CAPTION.name), true)
            .performClick()

        assertEquals(2, clickCount)
    }
}
