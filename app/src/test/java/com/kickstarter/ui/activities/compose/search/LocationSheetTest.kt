package com.kickstarter.ui.activities.compose.search

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Location
import com.kickstarter.ui.activities.compose.search.LocationTestTags.INPUT_BUTTON
import com.kickstarter.ui.activities.compose.search.LocationTestTags.INPUT_SEARCH
import com.kickstarter.ui.activities.compose.search.LocationTestTags.SUGGESTED_LOCATIONS_LIST
import com.kickstarter.ui.compose.designsystem.KSTheme
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThat
import org.junit.Test

class LocationSheetTest : KSRobolectricTestCase() {

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `Initial setUp with Empty InputText, Anywhere + Mock Location as default`() {

        val env = Environment.builder().apolloClientV2(
            object : MockApolloClientV2() {
                override suspend fun getLocations(
                    useDefault: Boolean,
                    term: String?,
                    lat: Float?,
                    long: Float?,
                    radius: Float?,
                    filterByCoordinates: Boolean?
                ): Result<List<Location>> {
                    return Result.success(listOf(LocationFactory.vancouver()))
                }
            }
        ).build()
        val fakeViewModel = FilterMenuViewModel(env, isInPreview = true)

        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    LocationSheet()
                }
            }
        }

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Location))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Location_Anywhere))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(LocationTestTags.locationTag(LocationFactory.vancouver()))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(INPUT_SEARCH)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(INPUT_SEARCH)
            .assertTextContains(context.resources.getString(R.string.Location_searchbox_placeholder))

        composeTestRule
            .onNodeWithTag(INPUT_BUTTON)
            .assertDoesNotExist()
    }

    @Test
    fun `InputText focs shows cancel button`() {

        val env = Environment.builder().apolloClientV2(
            object : MockApolloClientV2() {
                override suspend fun getLocations(
                    useDefault: Boolean,
                    term: String?,
                    lat: Float?,
                    long: Float?,
                    radius: Float?,
                    filterByCoordinates: Boolean?
                ): Result<List<Location>> {
                    return Result.success(listOf(LocationFactory.vancouver()))
                }
            }
        ).build()
        val fakeViewModel = FilterMenuViewModel(env, isInPreview = true)

        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    LocationSheet()
                }
            }
        }

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Location))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(INPUT_SEARCH)
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag(INPUT_BUTTON)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Location_Anywhere))
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag(LocationTestTags.locationTag(LocationFactory.vancouver()))
            .assertDoesNotExist()
    }

    @Test
    fun `Typing text into input search updates suggestions`() {
        runTest {
            val env = Environment.builder().apolloClientV2(
                object : MockApolloClientV2() {
                    override suspend fun getLocations(
                        useDefault: Boolean,
                        term: String?,
                        lat: Float?,
                        long: Float?,
                        radius: Float?,
                        filterByCoordinates: Boolean?
                    ): Result<List<Location>> {
                        return if (term?.contains("mexico", ignoreCase = true) == true) {
                            Result.success(listOf(LocationFactory.mexico()))
                        } else {
                            Result.success(emptyList())
                        }
                    }
                }
            ).build()

            val fakeViewModel = FilterMenuViewModel(env, isInPreview = true, testDispatcher = UnconfinedTestDispatcher(testScheduler))

            composeTestRule.setContent {
                KSTheme {
                    CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                        LocationSheet()
                    }
                }
            }

            composeTestRule
                .onNodeWithTag(INPUT_SEARCH)
                .performTextInput("Mexico")

            advanceUntilIdle() // Account for debounce when tipping on search input

            composeTestRule
                .onNodeWithTag(SUGGESTED_LOCATIONS_LIST)
                .assertIsDisplayed()

            composeTestRule
                .onNodeWithText(context.resources.getString(R.string.Location_Anywhere))
                .assertDoesNotExist()

            composeTestRule
                .onNodeWithTag(LocationTestTags.locationTag(LocationFactory.vancouver()))
                .assertDoesNotExist()

            composeTestRule
                .onNodeWithTag(LocationTestTags.locationTag(LocationFactory.mexico()))
                .assertIsDisplayed()
        }
    }

    @Test
    fun `Input text contains text, cancel button will clean up and go back to default locations`() {

        val env = Environment.builder().apolloClientV2(
            object : MockApolloClientV2() {
                override suspend fun getLocations(
                    useDefault: Boolean,
                    term: String?,
                    lat: Float?,
                    long: Float?,
                    radius: Float?,
                    filterByCoordinates: Boolean?
                ): Result<List<Location>> {
                    return Result.success(listOf(LocationFactory.vancouver()))
                }
            }
        ).build()
        val fakeViewModel = FilterMenuViewModel(env, isInPreview = true)

        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    LocationSheet()
                }
            }
        }

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Location))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.resources.getString(R.string.Location_Anywhere))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(LocationTestTags.locationTag(LocationFactory.vancouver()))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(LocationTestTags.locationTag(LocationFactory.vancouver()))
            .performClick()

        composeTestRule
            .onNodeWithTag(INPUT_SEARCH)
            .assertTextContains(LocationFactory.vancouver().displayableName())

        composeTestRule
            .onNodeWithTag(LocationTestTags.locationTag(LocationFactory.vancouver()))
            .isNotDisplayed() // Default locations no visible if text on input

        composeTestRule
            .onNodeWithTag(INPUT_BUTTON)
            .assertExists()

        composeTestRule
            .onNodeWithTag(INPUT_BUTTON)
            .performClick()

        composeTestRule
            .onNodeWithTag(LocationTestTags.locationTag(LocationFactory.vancouver()))
            .isDisplayed() // Default locations visible after input cancel button
    }

    @Test
    fun `Selecting a suggested location updates input`() = runTest {
        val env = Environment.builder().apolloClientV2(
            object : MockApolloClientV2() {
                override suspend fun getLocations(
                    useDefault: Boolean,
                    term: String?,
                    lat: Float?,
                    long: Float?,
                    radius: Float?,
                    filterByCoordinates: Boolean?
                ): Result<List<Location>> {
                    return if (term?.contains("mexico", ignoreCase = true) == true) {
                        Result.success(listOf(LocationFactory.mexico()))
                    } else {
                        Result.success(emptyList())
                    }
                }
            }
        ).build()
        val fakeViewModel = FilterMenuViewModel(env, isInPreview = true, testDispatcher = UnconfinedTestDispatcher(testScheduler))

        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    LocationSheet()
                }
            }
        }

        composeTestRule.onNodeWithTag(INPUT_SEARCH).performTextInput("Mexico")
        advanceUntilIdle()

        val mexicoTag = LocationTestTags.locationTag(LocationFactory.mexico())

        composeTestRule.onNodeWithTag(mexicoTag).performClick()

        composeTestRule.onNodeWithTag(SUGGESTED_LOCATIONS_LIST)
            .assertExists()

        composeTestRule.onNodeWithTag(INPUT_SEARCH)
            .assertTextContains(LocationFactory.mexico().displayableName())
    }

    @Test
    fun `Debounce reduce number of API calls`() = runTest {
        var callCount = 0

        val env = Environment.builder().apolloClientV2(
            object : MockApolloClientV2() {
                override suspend fun getLocations(
                    useDefault: Boolean,
                    term: String?,
                    lat: Float?,
                    long: Float?,
                    radius: Float?,
                    filterByCoordinates: Boolean?
                ): Result<List<Location>> {
                    callCount++
                    return Result.success(emptyList())
                }
            }
        ).build()
        val fakeViewModel = FilterMenuViewModel(env, isInPreview = true, testDispatcher = UnconfinedTestDispatcher(testScheduler))

        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    LocationSheet()
                }
            }
        }

        composeTestRule.onNodeWithTag(INPUT_SEARCH).performTextInput("M")
        composeTestRule.onNodeWithTag(INPUT_SEARCH).performTextInput("Me")
        composeTestRule.onNodeWithTag(INPUT_SEARCH).performTextInput("Mex")

        advanceUntilIdle() // wait for debounce

        assertNotSame(callCount,3)
    }

}
