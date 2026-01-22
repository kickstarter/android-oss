package com.kickstarter.features.home.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.home.data.Tab
import com.kickstarter.features.home.ui.components.FloatingBottomNavTestTags.SLIDING_INDICATOR
import com.kickstarter.features.home.ui.components.FloatingBottomNavTestTags.tabTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test

class FloatingBottomNavTest : KSRobolectricTestCase() {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `Not logged user tabs`() {
        val tabs = listOf(Tab.Home(), Tab.Search(), Tab.LogIn())

        composeTestRule.setContent {
            KSTheme {
                FloatingBottomNav(
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
        val tabs = listOf(Tab.Home(), Tab.Search(), Tab.Profile(""))

        composeTestRule.setContent {
            KSTheme {
                FloatingBottomNav(
                    tabs = tabs,
                )
            }
        }

        tabs.forEach { tab ->
            composeTestRule
                .onNodeWithTag(tabTag(tab))
                .assertIsDisplayed()

            composeTestRule
                .onNodeWithTag(tabTag(Tab.LogIn()))
                .assertDoesNotExist()
        }
    }

    @Test
    fun `Active indicated by default is Tab Home`() {

        val tabs = listOf(Tab.Home(), Tab.Search(), Tab.Profile(""))

        composeTestRule.setContent {
            val nav = rememberNavController()
            KSTheme {
                FloatingBottomNav(
                    tabs = tabs,
                )
            }
        }

        // Default active is home
        val homeTabCoord = composeTestRule
            .onNodeWithTag(tabTag(Tab.Home()))
            .getUnclippedBoundsInRoot()
            .left

        val slidingContainerCoord = composeTestRule
            .onNodeWithTag(SLIDING_INDICATOR)
            .getUnclippedBoundsInRoot()
            .left

        assertEquals(
            "The indicator pill is not aligned with the active tab on initial load",
            homeTabCoord.value,
            slidingContainerCoord.value,
            0.0f
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Sliding container when user clicks Tab Search moves from Tab Home to Tab Search`() {

        val tabs = listOf(Tab.Home(), Tab.Search(), Tab.Profile(""))

        composeTestRule.setContent {
            KSTheme {
                FloatingBottomNav(
                    tabs = tabs,
                )
            }
        }

        val searchTabCoord = composeTestRule
            .onNodeWithTag(tabTag(tabs[1]))
            .getUnclippedBoundsInRoot()
            .left

        composeTestRule
            .onNodeWithTag(tabTag(tabs[1]))
            .performClick()

        composeTestRule.waitForIdle()

        // - Capture coordinate once animation is finished
        val finalSlidingContainerX = composeTestRule
            .onNodeWithTag(SLIDING_INDICATOR)
            .getUnclippedBoundsInRoot()
            .left

        assertEquals(
            "The indicator pill is not aligned with the active tab",
            searchTabCoord.value,
            finalSlidingContainerX.value,
            0.0f
        )
    }
}
