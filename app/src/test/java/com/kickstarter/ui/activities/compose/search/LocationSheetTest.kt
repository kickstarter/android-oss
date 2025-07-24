package com.kickstarter.ui.activities.compose.search

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
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
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Location
import com.kickstarter.ui.activities.compose.search.LocationTestTags.INPUT_BUTTON
import com.kickstarter.ui.activities.compose.search.LocationTestTags.INPUT_SEARCH
import com.kickstarter.ui.activities.compose.search.LocationTestTags.SUGGESTED_LOCATIONS_LIST
import com.kickstarter.ui.compose.designsystem.BottomSheetFooterTestTags
import com.kickstarter.ui.compose.designsystem.KSTheme
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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
        )
            .currentUserV2(MockCurrentUserV2())
            .build()
        val fakeViewModel = FilterMenuViewModel(env)

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
            .onNodeWithText(context.resources.getString(R.string.Location_anywhere))
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
        )
            .currentUserV2(MockCurrentUserV2())
            .build()
        val fakeViewModel = FilterMenuViewModel(env)

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
            .onNodeWithText(context.resources.getString(R.string.Location_anywhere))
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
            )
                .currentUserV2(MockCurrentUserV2())
                .build()

            val fakeViewModel = FilterMenuViewModel(env, testDispatcher = UnconfinedTestDispatcher(testScheduler))

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
                .onNodeWithText(context.resources.getString(R.string.Location_anywhere))
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
    fun `default state when opening Location Sheet, when pressing default location buttons states updates`() {

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
        )
            .currentUserV2(MockCurrentUserV2())
            .build()
        val fakeViewModel = FilterMenuViewModel(env)

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
            .onNodeWithText(context.resources.getString(R.string.Location_anywhere))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(LocationTestTags.locationTag(LocationFactory.vancouver()))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .assertIsEnabled()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag(LocationTestTags.locationTag(LocationFactory.vancouver()))
            .performClick()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .assertIsEnabled()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .assertIsEnabled()
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
        )
            .currentUserV2(MockCurrentUserV2())
            .build()
        val fakeViewModel = FilterMenuViewModel(env, testDispatcher = UnconfinedTestDispatcher(testScheduler))

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
        )
            .currentUserV2(MockCurrentUserV2())
            .build()
        val fakeViewModel = FilterMenuViewModel(env, testDispatcher = UnconfinedTestDispatcher(testScheduler))

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

        assertNotSame(callCount, 3)
    }

    @Test
    fun `Clicking "See Results" button sends selected location`() {
        var selected: Location? = null
        var applied: Boolean? = null

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
        )
            .currentUserV2(MockCurrentUserV2())
            .build()
        val fakeViewModel = FilterMenuViewModel(env)

        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    LocationSheet(
                        onApply = { loc, confirmed ->
                            selected = loc
                            applied = confirmed
                        }
                    )
                }
            }
        }

        val location = LocationFactory.vancouver()

        composeTestRule.onNodeWithTag(LocationTestTags.locationTag(location)).performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.See_results))
            .performClick()

        assertEquals(selected, location)
        assertEquals(applied, true)
    }
}
