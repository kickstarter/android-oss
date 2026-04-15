package com.kickstarter.features.projecstory.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.core.app.ApplicationProvider
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.DataSource
import coil.request.ErrorResult
import coil.request.SuccessResult
import coil.test.FakeImageLoaderEngine
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.projectstory.ui.ProjectStoryCaptionedImage
import com.kickstarter.features.projectstory.ui.ProjectStoryCaptionedImageTestTag
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoilApi::class)
class ProjectStoryCaptionedImageTest : KSRobolectricTestCase() {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private fun gradientDrawable(color: Int, width: Int = 100, height: Int = 50) =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            setSize(width, height)
        }

    private fun bitmapDrawable(width: Int = 100, height: Int = 50): Drawable {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        return BitmapDrawable(context.resources, bitmap)
    }

    private fun fakeImageLoaderEngine() =
        FakeImageLoaderEngine.Builder()
            .intercept(
                "https://www.example.com/image.png",
                bitmapDrawable()
            )
            .intercept(
                { it == "https://www.example.com/red.jpg" },
                { ErrorResult(null, it.request, Throwable("Bad request: red")) }
            )
            .intercept(
                { it is String && it.contains("green") },
                gradientDrawable(Color.GREEN)
            )
            .intercept(
                { it == "https://www.example.com/blue.jpg" },
                {
                    /* The `ImageRequest` for `rememberAsyncImagePainter` is executed explicitly on
                     * `Dispatchers.Main.immediate`. But this suspending `intercept` method is run on
                     * the `ImageRequest.interceptorDispatcher`, which we can set to a test dispatcher,
                     * and then introduce a delay to witness AsyncImagePainter's `Loading` state.
                     *
                     * An alternative would be to call `Dispatchers.setMain()` with a test dispatcher.
                     * But on the current versions of the the current set of test libraries, this does
                     * not work as expected, and requires coordinating with Robolectric. */
                    delay(REQUEST_DELAY)
                    SuccessResult(gradientDrawable(Color.BLUE), it.request, DataSource.MEMORY)
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
    fun `test image success with caption`() {
        val caption = "Aye aye, Caption"

        var onSuccessCount = 0

        composeTestRule.setContent {
            ProjectStoryCaptionedImage(
                image = "https://www.example.com/green.jpg",
                caption = caption,
                link = null,
                onSuccess = {
                    onSuccessCount++
                }
            )
        }

        assertEquals(1, onSuccessCount)

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.IMAGE.name))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
            .assertContentDescriptionEquals(caption)
            .assertIsDisplayed()

        composeTestRule.onNode(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
        ).assertDoesNotExist()

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.CAPTION.name))
            .assertTextEquals(caption)
            .assertIsDisplayed()
    }

    @Test
    fun `test image error with caption`() {
        val caption = "Aye aye, Caption"

        composeTestRule.setContent {
            ProjectStoryCaptionedImage(
                image = "https://www.example.com/red.jpg",
                caption = caption,
            )
        }

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.IMAGE.name))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
            .assertContentDescriptionEquals(caption)
            .assertIsNotDisplayed()

        composeTestRule.onNode(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
        ).assertDoesNotExist()

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.CAPTION.name))
            .assertTextEquals(caption)
            .assertIsDisplayed()
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun `test image loading with caption (mainClock)`() {
        /* This test exists for educational purposes.
         * It verifies the same behavior as the two tests below.
         * If this test breaks, delete it. */
        val caption = "Aye aye, Caption"

        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.setContent {
            ProjectStoryCaptionedImage(
                image = "https://www.example.com/green.jpg",
                caption = caption,
            )
        }

        composeTestRule.onNode(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
        ).assertIsDisplayed()

        composeTestRule.onNode(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
        ).assertIsDisplayed()

        composeTestRule.mainClock.autoAdvance = true

        composeTestRule.waitUntilDoesNotExist(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
        )

        composeTestRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image)
                and hasContentDescription(caption)
        ).assertIsDisplayed()

        composeTestRule.onNode(
            hasTestTag(ProjectStoryCaptionedImageTestTag.CAPTION.name)
                and hasText(caption)
        ).assertIsDisplayed()
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `test image loading with caption (runTest)`() = runTest {
        /* This test exists for educational purposes.
         * It verifies the same behavior as the test above and test below.
         * If this test breaks, delete it. */
        val standardDispatcher = StandardTestDispatcher(testScheduler)

        setUpImageLoader(
            ImageLoader.Builder(context)
                .components { add(fakeImageLoaderEngine()) }
                .dispatcher(standardDispatcher)
                .interceptorDispatcher(standardDispatcher)
                .build()
        )

        val caption = "Aye aye, Caption"

        composeTestRule.setContent {
            ProjectStoryCaptionedImage(
                image = "https://www.example.com/blue.jpg",
                caption = caption,
            )
        }

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.LOADING_INDICATOR.name))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertIsDisplayed()

        /* Using `advanceTimeBy()` + `runCurrent()` just to make the explicit connection to `requestDelay`.
         * These can be replaced with a single call to `advanceUntilIdle()`. */
        advanceTimeBy(REQUEST_DELAY)
        runCurrent()

        composeTestRule.onNode(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
        ).assertDoesNotExist()

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.IMAGE.name))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
            .assertContentDescriptionEquals(caption)
            .assertIsDisplayed()

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.CAPTION.name))
            .assertTextEquals(caption)
            .assertIsDisplayed()
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun `test image loading with caption (runComposeUiTest)`() = runComposeUiTest {
        val standardDispatcher = StandardTestDispatcher(mainClock.scheduler)

        setUpImageLoader(
            ImageLoader.Builder(context)
                .components { add(fakeImageLoaderEngine()) }
                .dispatcher(standardDispatcher)
                .interceptorDispatcher(standardDispatcher)
                .build()
        )

        val caption = "Aye aye, Caption"

        setContent {
            ProjectStoryCaptionedImage(
                image = "https://www.example.com/blue.jpg",
                caption = caption,
            )
        }

        onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.LOADING_INDICATOR.name))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertIsDisplayed()

        mainClock.advanceTimeBy(REQUEST_DELAY)

        onNode(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
        ).assertDoesNotExist()

        onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.IMAGE.name))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
            .assertContentDescriptionEquals(caption)
            .assertIsDisplayed()

        onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.CAPTION.name))
            .assertTextEquals(caption)
            .assertIsDisplayed()
    }

    @Test
    fun `test empty caption still displayed`() {
        val caption = ""

        composeTestRule.setContent {
            ProjectStoryCaptionedImage(
                image = null,
                caption = caption,
            )
        }

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.CAPTION.name))
            .assertTextEquals(caption)
            .assertIsDisplayed()
    }

    @Test
    fun `test null caption not displayed`() {
        composeTestRule.setContent {
            ProjectStoryCaptionedImage(
                image = null,
                caption = null,
            )
        }

        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.CAPTION.name))
            .assertDoesNotExist()
    }

    companion object {
        private const val REQUEST_DELAY = 1500L
    }
}
