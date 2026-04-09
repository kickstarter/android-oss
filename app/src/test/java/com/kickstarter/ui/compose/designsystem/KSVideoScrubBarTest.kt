package com.kickstarter.ui.compose.designsystem

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class KSVideoScrubBarTest : KSRobolectricTestCase() {

    @Test
    fun `scrub bar and thumb are displayed`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoScrubBar(
                    progress = 0.5f,
                    onSeek = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoScrubBarTestTag.SCRUB_BAR_CONTAINER.name)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag(KSVideoScrubBarTestTag.SCRUB_BAR_THUMB.name)
            .assertIsDisplayed()
    }

    @Test
    fun `scrub bar exposes correct progress semantics`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoScrubBar(
                    progress = 0.75f,
                    onSeek = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoScrubBarTestTag.SCRUB_BAR_CONTAINER.name)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo(0.75f, 0f..1f)
                )
            )
    }

    @Test
    fun `tapping scrub bar invokes onSeek`() {
        val seekValues = mutableListOf<Float>()

        composeTestRule.setContent {
            KSTheme {
                KSVideoScrubBar(
                    progress = 0f,
                    onSeek = { seekValues.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoScrubBarTestTag.SCRUB_BAR_CONTAINER.name)
            .performTouchInput {
                click(position = Offset(x = width * 0.5f, y = height / 2f))
            }

        assert(seekValues.isNotEmpty()) { "onSeek should have been called" }
        // Tapping at 50% should produce a value around 0.5
        assert(seekValues.last() in 0.4f..0.6f) {
            "Expected progress near 0.5 but got ${seekValues.last()}"
        }
    }

    @Test
    fun `dragging scrub bar invokes onSeek multiple times`() {
        val seekValues = mutableListOf<Float>()

        composeTestRule.setContent {
            KSTheme {
                KSVideoScrubBar(
                    progress = 0f,
                    onSeek = { seekValues.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoScrubBarTestTag.SCRUB_BAR_CONTAINER.name)
            .performTouchInput {
                down(Offset(x = width * 0.2f, y = height / 2f))
                moveBy(Offset(x = width * 0.3f, y = 0f))
                moveBy(Offset(x = width * 0.2f, y = 0f))
                up()
            }

        // onSeek should be called on touch down + each drag move
        assert(seekValues.size >= 2) {
            "Expected at least 2 onSeek calls but got ${seekValues.size}"
        }

        // Final value should be greater than the initial touch position
        assert(seekValues.last() > seekValues.first()) {
            "Progress should increase during drag: first=${seekValues.first()}, last=${seekValues.last()}"
        }
    }

    @Test
    fun `scrub bar displays at zero progress`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoScrubBar(
                    progress = 0f,
                    onSeek = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoScrubBarTestTag.SCRUB_BAR_CONTAINER.name)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo(0f, 0f..1f)
                )
            )

        composeTestRule.onNodeWithTag(KSVideoScrubBarTestTag.SCRUB_BAR_THUMB.name)
            .assertIsDisplayed()
    }

    @Test
    fun `scrub bar displays at full progress`() {
        composeTestRule.setContent {
            KSTheme {
                KSVideoScrubBar(
                    progress = 1f,
                    onSeek = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(KSVideoScrubBarTestTag.SCRUB_BAR_CONTAINER.name)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo(1f, 0f..1f)
                )
            )
    }

    @Test
    fun `tapping at different positions produces correct seek values`() {
        val seekValues = mutableListOf<Float>()

        composeTestRule.setContent {
            KSTheme {
                KSVideoScrubBar(
                    progress = 0f,
                    onSeek = { seekValues.add(it) }
                )
            }
        }

        // Tap at 25%
        composeTestRule.onNodeWithTag(KSVideoScrubBarTestTag.SCRUB_BAR_CONTAINER.name)
            .performTouchInput {
                click(position = Offset(x = width * 0.25f, y = height / 2f))
            }

        assert(seekValues.last() in 0.15f..0.35f) {
            "Expected progress near 0.25 but got ${seekValues.last()}"
        }

        seekValues.clear()

        // Tap at 75%
        composeTestRule.onNodeWithTag(KSVideoScrubBarTestTag.SCRUB_BAR_CONTAINER.name)
            .performTouchInput {
                click(position = Offset(x = width * 0.75f, y = height / 2f))
            }

        assert(seekValues.last() in 0.65f..0.85f) {
            "Expected progress near 0.75 but got ${seekValues.last()}"
        }
    }
}
