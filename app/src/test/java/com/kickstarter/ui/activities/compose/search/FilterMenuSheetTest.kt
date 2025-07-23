package com.kickstarter.ui.activities.compose.search

import android.content.Context
import androidx.compose.material.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.compose.designsystem.BottomSheetFooterTestTags
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class FilterMenuSheetTest : KSRobolectricTestCase() {
    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `test FilterMenuSheet renders all pills within ProjectStatusRow`() {

        composeTestRule.setContent {
            val env = environment()
            val fakeViewModel = FilterMenuViewModel(env)
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    Surface {
                        FilterMenuSheet()
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(null)).assertIsDisplayed() // All
        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.State.LIVE)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.State.LATE_PLEDGES)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.State.SUCCESSFUL)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.State.UPCOMING)).assertIsDisplayed()
    }

    @Test
    fun `test FilterMenuSheet renders all options within OthersRow when logged in user`() {

        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    Surface {
                        FilterMenuSheet()
                    }
                }
            }
        }

        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.LIST)
            .performScrollToNode(hasTestTag(FilterMenuTestTags.OTHERS_ROW))
        composeTestRule.onNodeWithText(context.resources.getString(R.string.Show_only_fpo)).assertIsDisplayed()

        // Recommended
        composeTestRule.onNodeWithTag(DiscoveryParams::recommended.name).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.resources.getString(R.string.Recommended_fpo)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.switchTag(DiscoveryParams::recommended.name)).assertIsOff()

        // Projects we love
        composeTestRule.onNodeWithTag(DiscoveryParams::staffPicks.name).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.resources.getString(R.string.Projects_We_Love_fpo)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.switchTag(DiscoveryParams::staffPicks.name)).assertIsOff()

        // Saved
        composeTestRule.onNodeWithTag(DiscoveryParams::starred.name).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.resources.getString(R.string.Saved_projects_fpo)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.switchTag(DiscoveryParams::starred.name)).assertIsOff()

        // Social
        composeTestRule.onNodeWithTag(DiscoveryParams::social.name).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.resources.getString(R.string.Following_fpo)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.switchTag(DiscoveryParams::social.name)).assertIsOff()
    }

    @Test
    fun `test FilterMenuSheet renders all available filter Rows with ffOff`() {
        val shouldShowPhase = false
        val env = environment()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    FilterMenuSheet(
                        availableFilters = if (shouldShowPhase) FilterType.values().asList()
                        else FilterType.values().asList().filter { it != FilterType.OTHERS },
                        onDismiss = {},
                        onApply = { a, b, c, d, e, f -> },
                        onNavigate = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag(FilterMenuTestTags.SHEET).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.CATEGORY_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.PROJECT_STATUS_ROW).assertIsDisplayed()

        // - As working with LazyColumns, not all elements of the list are composed until the elements is visible
        // - perform a scroll on the list, to reach the desired not, once scroll performed THEN the element will be composed and added to the semantic tree
        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.LIST)
            .performScrollToNode(hasTestTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW))

        composeTestRule.onNodeWithTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.LOCATION_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.AMOUNT_RAISED_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.OTHERS_ROW).assertDoesNotExist()
    }

    @Test
    fun `test FilterMenuSheet renders all available filter Rows with ffOn`() {
        val shouldShowPhase = true
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    FilterMenuSheet(
                        selectedProjectStatus = DiscoveryParams.State.LIVE,
                        availableFilters = if (shouldShowPhase) FilterType.values().asList()
                        else FilterType.values().asList().filter { it != FilterType.OTHERS },
                        onDismiss = {},
                        onApply = { a, b, c, d, e, f -> },
                        onNavigate = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag(FilterMenuTestTags.SHEET).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.CATEGORY_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.PROJECT_STATUS_ROW).assertIsDisplayed()

        // - As working with LazyColumns, not all elements of the list are composed until the elements is visible
        // - perform a scroll on the list, to reach the desired not, once scroll performed THEN the element will be composed and added to the semantic tree
        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.LIST)
            .performScrollToNode(hasTestTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW))

        composeTestRule.onNodeWithTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.AMOUNT_RAISED_ROW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilterMenuTestTags.LOCATION_ROW).assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.LIST)
            .performScrollToNode(hasTestTag(FilterMenuTestTags.OTHERS_ROW))
        composeTestRule.onNodeWithTag(FilterMenuTestTags.OTHERS_ROW).assertIsDisplayed()
    }

    @Test
    fun `test selected and unselected status for live pill`() {
        var counter = 0
        val env = environment()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    FilterMenuSheet(
                        onApply = { publicState: DiscoveryParams.State?, recommended: Boolean, projectsLoved: Boolean, saved: Boolean, social: Boolean, from: Boolean? ->
                            counter++
                            if (counter == 1)
                                assertEquals(publicState, DiscoveryParams.State.LIVE)

                            if (counter == 2)
                                assertEquals(publicState, null)

                            assertNull(from)
                        }
                    )
                }
            }
        }

        val livePill = composeTestRule.onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.State.LIVE))
        livePill.performClick() // Select

        livePill.performClick() // Unselect (toggle off)
    }

    @Test
    fun `test FilterMenu _onApplyCallback receives projectState when pressing footer right button`() {
        var selected: DiscoveryParams.State? = null
        var contextFrom: Boolean? = null
        val env = environment()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    FilterMenuSheet(onApply = { publicState: DiscoveryParams.State?, recommended: Boolean, projectsLoved: Boolean, saved: Boolean, social: Boolean, from: Boolean? ->
                        selected = publicState
                        contextFrom = from
                    })
                }
            }
        }

        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.pillTag(DiscoveryParams.State.LIVE))
            .performClick()

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.SEE_RESULTS.name)
            .performClick()

        assertEquals(DiscoveryParams.State.LIVE, selected)
        assertEquals(contextFrom, true)
    }

    @Test
    fun `test FilterMenu  _onApplyCallback clears selection when pressing footer left button`() {
        var selected: DiscoveryParams.State? = DiscoveryParams.State.LIVE
        var contextFrom: Boolean? = null

        val env = environment()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    FilterMenuSheet(
                        selectedProjectStatus = selected,
                        onApply = { publicState: DiscoveryParams.State?, recommended: Boolean, projectsLoved: Boolean, saved: Boolean, social: Boolean, from: Boolean? ->
                            selected = publicState
                            contextFrom = from
                        }
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(BottomSheetFooterTestTags.RESET.name)
            .performClick()

        assertNull(selected)
        assertEquals(contextFrom, false)
    }

    @Test
    fun `category row, selected category subtext is present`() {
        val env = environment()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    FilterMenuSheet(
                        selectedCategory = CategoryFactory.CeramicsCategory(),
                        onApply = { publicState: DiscoveryParams.State?, recommended: Boolean, projectsLoved: Boolean, saved: Boolean, social: Boolean, from: Boolean? ->
                        }
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithText(CategoryFactory.CeramicsCategory().name())
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(CategoryFactory.CeramicsCategory().name())
            .performScrollTo()
    }

    @Test
    fun `percentage raised row, selected percentage bucket, subtext is present`() {
        var textForBucket: String = ""
        val env = environment()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    textForBucket = textForBucket(DiscoveryParams.RaisedBuckets.BUCKET_2)
                    FilterMenuSheet(
                        onApply = { publicState: DiscoveryParams.State?, recommended: Boolean, projectsLoved: Boolean, saved: Boolean, social: Boolean, from: Boolean? ->
                        },
                        selectedPercentage = DiscoveryParams.RaisedBuckets.BUCKET_2
                    )
                }
            }
        }

        // - As working with LazyColumns, not all elements of the list are composed until the elements is visible
        // - perform a scroll on the list, to reach the desired not, once scroll performed THEN the element will be composed and added to the semantic tree
        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.LIST)
            .performScrollToNode(hasTestTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW))

        composeTestRule.onNodeWithTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(textForBucket)
            .assertIsDisplayed()
    }

    @Test
    fun `location row, selected location, subtext is present`() {
        val env = environment()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    FilterMenuSheet(
                        onApply = { publicState: DiscoveryParams.State?, recommended: Boolean, projectsLoved: Boolean, saved: Boolean, social: Boolean, from: Boolean? ->
                        },
                        selectedLocation = LocationFactory.vancouver()
                    )
                }
            }
        }

        // - As working with LazyColumns, not all elements of the list are composed until the elements is visible
        // - perform a scroll on the list, to reach the desired not, once scroll performed THEN the element will be composed and added to the semantic tree
        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.LIST)
            .performScrollToNode(hasTestTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW))

        composeTestRule
            .onNodeWithText(LocationFactory.vancouver().displayableName())
            .assertIsDisplayed()
    }

    @Test
    fun `amount raised row, selected amount bucket, subtext is present`() {
        var textForBucket: String = ""
        val env = environment()
        val fakeViewModel = FilterMenuViewModel(env)
        composeTestRule.setContent {
            KSTheme {
                CompositionLocalProvider(LocalFilterMenuViewModel provides fakeViewModel) {
                    textForBucket = textForBucket(DiscoveryParams.AmountBuckets.BUCKET_4)
                    FilterMenuSheet(
                        onApply = { publicState: DiscoveryParams.State?, recommended: Boolean, projectsLoved: Boolean, saved: Boolean, social: Boolean, from: Boolean? ->
                        },
                        selectedAmount = DiscoveryParams.AmountBuckets.BUCKET_4
                    )
                }
            }
        }

        // - As working with LazyColumns, not all elements of the list are composed until the elements is visible
        // - perform a scroll on the list, to reach the desired not, once scroll performed THEN the element will be composed and added to the semantic tree
        composeTestRule
            .onNodeWithTag(FilterMenuTestTags.LIST)
            .performScrollToNode(hasTestTag(FilterMenuTestTags.PERCENTAGE_RAISED_ROW))

        composeTestRule
            .onNodeWithText(textForBucket)
            .assertIsDisplayed()
    }
}
