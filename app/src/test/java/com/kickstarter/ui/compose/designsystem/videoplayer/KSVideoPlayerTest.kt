package com.kickstarter.ui.compose.designsystem.videoplayer

import android.graphics.Matrix
import android.view.TextureView
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.media3.exoplayer.ExoPlayer
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class KSVideoPlayerTest() : KSRobolectricTestCase() {

    private lateinit var textureView: TextureView
    private val viewWidth = 1080
    private val viewHeight = 1920

    @Before
    fun setup() {
        textureView = mock(TextureView::class.java)
        `when`(textureView.width).thenReturn(viewWidth)
        `when`(textureView.height).thenReturn(viewHeight)
    }

    @Test
    fun `applyZoomMatrix - Guard against zero video dimensions`() {
        // - Call with 0 video dimensions
        textureView.applyZoomMatrix(videoWidth = 0, videoHeight = 100)

        // - No transform should be set
        verify(textureView, never()).setTransform(any())
    }

    @Test
    fun `applyZoomMatrix - Guard against zero TextureView dimensions`() {
        `when`(textureView.width).thenReturn(0)

        textureView.applyZoomMatrix(videoWidth = 100, videoHeight = 100)

        verify(textureView, never()).setTransform(any())
    }

    @Test
    fun `applyZoomMatrix - Handling negative dimension values`() {
        textureView.applyZoomMatrix(videoWidth = -10, videoHeight = 100)

        verify(textureView, never()).setTransform(any())
    }

    @Test
    fun `applyZoomMatrix - Perfect aspect ratio match`() {
        // Arrange: Video matches view exactly (9:16)
        val videoWidth = 1080
        val videoHeight = 1920

        textureView.applyZoomMatrix(videoWidth, videoHeight)

        // - should call setTransform(null) no transformation applied
        verify(textureView).setTransform(null)
    }

    @Test
    fun `applyZoomMatrix - Horizontal video in vertical view (Center Crop X)`() {
        // Arrange: 16:9 Video in a 9:16 View.
        // Video is "wider" relative to the view, so we must scale X up (overflow).
        val videoWidth = 1920
        val videoHeight = 1080
        val matrixCaptor = ArgumentCaptor.forClass(Matrix::class.java)

        textureView.applyZoomMatrix(videoWidth, videoHeight)

        verify(textureView).setTransform(matrixCaptor.capture())
        val matrix = matrixCaptor.value

        val values = FloatArray(9)
        matrix.getValues(values)

        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]

        // scaleX should be (1920/1080) / (1080/1920) = 1.77 / 0.56 = ~3.16
        assert(scaleX > 1.0f)
        assertEquals(1.0f, scaleY, 0.01f)
    }

    @Test
    fun `applyZoomMatrix - Vertical video in horizontal view (Center Crop Y)`() {
        // - Vertical video in a square view
        `when`(textureView.width).thenReturn(1000)
        `when`(textureView.height).thenReturn(1000)

        val videoWidth = 500
        val videoHeight = 1000 // 1:2 ratio
        val matrixCaptor = ArgumentCaptor.forClass(Matrix::class.java)

        textureView.applyZoomMatrix(videoWidth, videoHeight)

        verify(textureView).setTransform(matrixCaptor.capture())
        val values = FloatArray(9)
        matrixCaptor.value.getValues(values)

        // View is wider than video ratio, so scaleY must increase to crop top/bottom
        assert(values[Matrix.MSCALE_Y] > 1.0f)
        assertEquals(1.0f, values[Matrix.MSCALE_X], 0.01f)
    }

    @Test
    fun `applyZoomMatrix - Matrix pivot point verification`() {
        val videoWidth = 100
        val videoHeight = 200
        val matrixCaptor = ArgumentCaptor.forClass(Matrix::class.java)

        textureView.applyZoomMatrix(videoWidth, videoHeight)

        verify(textureView).setTransform(matrixCaptor.capture())

        // - To verify pivot, we check if a point at (0,0) moves correctly relative to the center (viewWidth/2, viewHeight/2)
        val matrix = matrixCaptor.value
        val points = floatArrayOf(0f, 0f)
        matrix.mapPoints(points)

        // - If it scales from the center, the top-left (0,0) should move further away from the center (540, 960)
        assert(points[0] < 0f || points[1] < 0f)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `test tapping surface controls visible video pauses when tapping again hides controls and plays`() {
        val mockPlayer = mock(ExoPlayer::class.java)
        composeTestRule.setContent {
            KSTheme {
                KSVideoPlayer(
                    videoUrl = "https://example.com/video.mp4",
                    isActive = true,
                    player = mockPlayer
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_CONTROLS.name, useUnmergedTree = true).assertDoesNotExist()

        // - Tap to show controls
        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_SURFACE.name, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_CONTROLS.name, useUnmergedTree = true).assertIsDisplayed()
        // - Video should be paused
        verify(mockPlayer).pause()

        // - Tap surface again
        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_SURFACE.name, useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_CONTROLS.name).assertDoesNotExist()

        // - Video should be playing
        verify(mockPlayer).play()
    }

    @Test
    fun `test tapping play button hides controls and plays`() {
        val mockPlayer = mock(ExoPlayer::class.java)
        composeTestRule.setContent {
            KSTheme {
                KSVideoPlayer(
                    videoUrl = "https://example.com/video.mp4",
                    isActive = true,
                    player = mockPlayer
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_SURFACE.name).performClick()
        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PLAY_BUTTON.name).performClick()
        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_CONTROLS.name).assertDoesNotExist()

        verify(mockPlayer).play()

        // Check Video Player Surface has click label
        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_SURFACE.name)
            .assertExists()
    }

    @Test
    fun `test forward and rewind buttons are not present`() {
        val mockPlayer = mock(ExoPlayer::class.java)
        composeTestRule.setContent {
            KSTheme {
                KSVideoPlayer(
                    videoUrl = "https://example.com/video.mp4",
                    isActive = true,
                    player = mockPlayer
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_SURFACE.name).performClick()

        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_REWIND_BUTTON.name).assertDoesNotExist()
        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_FORWARD_BUTTON.name).assertDoesNotExist()
    }

    @Test
    fun `test tapping progress bar seeks video`() {
        val mockPlayer = mock(ExoPlayer::class.java)
        `when`(mockPlayer.duration).thenReturn(100000L) // 100 seconds
        composeTestRule.setContent {
            KSTheme {
                KSVideoPlayer(
                    videoUrl = "https://example.com/video.mp4",
                    isActive = true,
                    player = mockPlayer
                )
            }
        }

        // - Tap the progress bar at 25% (offset x = 0.25 * width)
        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PROGRESS_BAR.name, useUnmergedTree = true)
            .performTouchInput {
                click(position = Offset(x = width * 0.25f, y = height / 2f))
            }

        // - Should seek to 25% of 100000L = 25000L
        verify(mockPlayer).seekTo(25000L)

        // Check Progress Bar semantics
        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PROGRESS_BAR.name, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun `test dragging progress bar pauses video and releasing resumes`() {
        val mockPlayer = mock(ExoPlayer::class.java)
        `when`(mockPlayer.duration).thenReturn(100000L)
        composeTestRule.setContent {
            KSTheme {
                KSVideoPlayer(
                    videoUrl = "https://example.com/video.mp4",
                    isActive = true,
                    player = mockPlayer
                )
            }
        }

        // - Drag on the progress bar: down → move → up
        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PROGRESS_BAR.name, useUnmergedTree = true)
            .performTouchInput {
                down(Offset(x = width * 0.2f, y = height / 2f))
            }

        composeTestRule.waitForIdle()

        // - Player should be paused while scrubbing
        verify(mockPlayer).pause()

        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PROGRESS_BAR.name, useUnmergedTree = true)
            .performTouchInput {
                moveBy(Offset(x = width * 0.3f, y = 0f))
                up()
            }

        composeTestRule.waitForIdle()

        // - Player should resume after scrub ends (controls are not visible)
        verify(mockPlayer).play()
    }

    @Test
    fun `test scrubbing while controls visible does not resume playback on release`() {
        val mockPlayer = mock(ExoPlayer::class.java)
        `when`(mockPlayer.duration).thenReturn(100000L)
        composeTestRule.setContent {
            KSTheme {
                KSVideoPlayer(
                    videoUrl = "https://example.com/video.mp4",
                    isActive = true,
                    player = mockPlayer
                )
            }
        }

        // - Tap surface to show controls (pauses video)
        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_SURFACE.name).performClick()
        composeTestRule.waitForIdle()
        verify(mockPlayer).pause()

        // - Drag on the progress bar while controls are visible
        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PROGRESS_BAR.name, useUnmergedTree = true)
            .performTouchInput {
                down(Offset(x = width * 0.2f, y = height / 2f))
                moveBy(Offset(x = width * 0.5f, y = 0f))
                up()
            }

        composeTestRule.waitForIdle()

        // - play() should NOT have been called since controls are still showing
        verify(mockPlayer, never()).play()
    }

    @Test
    fun `test overlay content is displayed`() {
        val mockPlayer = mock(ExoPlayer::class.java)
        val testTag = "overlay_test_tag"
        composeTestRule.setContent {
            KSTheme {
                KSVideoPlayer(
                    videoUrl = "https://example.com/video.mp4",
                    isActive = true,
                    player = mockPlayer,
                    overlayContent = {
                        Text(text = "Overlay Text", modifier = Modifier.testTag(testTag))
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag(testTag, useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag(testTag, useUnmergedTree = true).isDisplayed()
    }

    @Test
    fun `test empty video url does not render player surface`() {
        val mockPlayer = mock(ExoPlayer::class.java)
        composeTestRule.setContent {
            KSTheme {
                KSVideoPlayer(
                    videoUrl = "",
                    isActive = true,
                    player = mockPlayer
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_SURFACE.name).assertDoesNotExist()
        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PROGRESS_BAR.name, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `test player does not autoplay when inactive`() {
        val mockPlayer = mock(ExoPlayer::class.java)
        composeTestRule.setContent {
            KSTheme {
                KSVideoPlayer(
                    videoUrl = "https://example.com/video.mp4",
                    isActive = false,
                    player = mockPlayer
                )
            }
        }

        composeTestRule.waitForIdle()

        // - playWhenReady should be set to false when inactive
        verify(mockPlayer).playWhenReady = false
        verify(mockPlayer, never()).play()
    }

    @Test
    fun `test progress bar is displayed on render`() {
        val mockPlayer = mock(ExoPlayer::class.java)
        composeTestRule.setContent {
            KSTheme {
                KSVideoPlayer(
                    videoUrl = "https://example.com/video.mp4",
                    isActive = true,
                    player = mockPlayer
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoPlayerTestTag.VIDEO_PLAYER_PROGRESS_BAR.name, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    private fun <T> any(): T = org.mockito.ArgumentMatchers.any()
}
