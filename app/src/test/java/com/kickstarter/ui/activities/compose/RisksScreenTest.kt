package com.kickstarter.ui.activities.compose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.fragments.projectpage.ui.RisksScreen
import com.kickstarter.ui.fragments.projectpage.ui.RisksScreenTestTag
import org.junit.Test

class RisksScreenTest : KSRobolectricTestCase() {

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val pageTitle = composeTestRule.onNodeWithTag(RisksScreenTestTag.PAGE_TITLE.name)
    private val riskDescriptionTextView =
        composeTestRule.onNodeWithTag(RisksScreenTestTag.RISK_DESCRIPTION.name)
    private val clickableTextView =
        composeTestRule.onNodeWithTag(RisksScreenTestTag.CLICKABLE_TEXT.name)

    @Test
    fun `test screen init`() {
        val pageTitleText = context.getString(R.string.Risks_and_challenges)
        val riskDescriptionText = context.getString(R.string.risk_description)
        val clickableText = context.getString(R.string.Learn_about_accountability_on_Kickstarter)
        val riskDescription = mutableStateOf(riskDescriptionText)

        composeTestRule.setContent {
            KSTheme {
                RisksScreen(
                    riskDescState = riskDescription,
                    callback = { }
                )
            }
        }

        pageTitle.assertIsDisplayed()
        pageTitle.assertTextEquals(pageTitleText)
        riskDescriptionTextView.assertIsDisplayed()
        riskDescriptionTextView.assertTextEquals(riskDescriptionText)
        clickableTextView.assertIsDisplayed()
        clickableTextView.assertIsEnabled()
        clickableTextView.assertTextEquals(clickableText)
    }

    @Test
    fun `test clickable text action`() {
        var clickableTextClicks = 0
        val riskDescriptionText = context.getString(R.string.risk_description)
        val riskDescription = mutableStateOf(riskDescriptionText)

        composeTestRule.setContent {
            KSTheme {
                RisksScreen(
                    riskDescState = riskDescription,
                    callback = { clickableTextClicks++ }
                )
            }
        }

        clickableTextView.assertIsDisplayed()
        clickableTextView.assertIsEnabled()
        clickableTextView.performClick()
        assertEquals(1, clickableTextClicks)
    }
}
