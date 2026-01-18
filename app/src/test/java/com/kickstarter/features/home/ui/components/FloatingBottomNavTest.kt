package com.kickstarter.features.home.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.home.data.Tab
import com.kickstarter.features.home.ui.components.FloatingBottomNavTestTags.SLIDING_INDICATOR
import com.kickstarter.features.home.ui.components.FloatingBottomNavTestTags.tabTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class FloatingBottomNavTest : KSRobolectricTestCase() {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `Not logged user tabs`() {
        val tabs = listOf(Tab.Home, Tab.Search, Tab.LogIn)

        composeTestRule.setContent {
            val nav = rememberNavController()
            KSTheme {
                FloatingBottomNav(
                    nav = nav,
                    tabs = tabs,
                )
            }
        }

        tabs.forEach { tab ->
            composeTestRule
                .onNodeWithTag(tabTag(tab))
                .assertIsDisplayed()

            composeTestRule
                .onNodeWithTag(tabTag(Tab.Profile("")))
                .assertDoesNotExist()
        }

        composeTestRule
            .onNodeWithTag(SLIDING_INDICATOR)
            .assertExists()
    }

    @Test
    fun `logged user tabs`() {
        val tabs = listOf(Tab.Home, Tab.Search, Tab.Profile(""))

        composeTestRule.setContent {
            val nav = rememberNavController()
            KSTheme {
                FloatingBottomNav(
                    nav = nav,
                    tabs = tabs,
                )
            }
        }

        tabs.forEach { tab ->
            composeTestRule
                .onNodeWithTag(tabTag(tab))
                .assertIsDisplayed()

            composeTestRule
                .onNodeWithTag(tabTag(Tab.LogIn))
                .assertDoesNotExist()
        }
    }
}
