package com.kickstarter.ui.activities.compose.search

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.features.search.ui.LocalFilterMenuViewModel
import com.kickstarter.features.search.viewmodel.FilterMenuViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.mock.factories.LocationFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Location
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.junit.Test

class LocationSheetTest : KSRobolectricTestCase() {

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun `Initial setUp with Empty InputText, Anywhere + default Location`() {

        val env = Environment.builder().apolloClientV2(
            object : MockApolloClientV2() {
                override suspend fun getLocations(
                    useDefault: Boolean,
                    term: String?
                ): Result<List<Location>> {
                    if (useDefault) return Result.success(listOf(LocationFactory.vancouver()))
                    val searched = listOf(
                        LocationFactory.sydney(),
                        LocationFactory.mexico(),
                        LocationFactory.canada(),
                        LocationFactory.germany(),
                        LocationFactory.unitedStates(),
                        LocationFactory.nigeria(),
                        LocationFactory.sydney(),
                        LocationFactory.mexico(),
                        LocationFactory.canada(),
                        LocationFactory.germany(),
                        LocationFactory.unitedStates(),
                        LocationFactory.nigeria(),
                    )
                    if (term.isNotNull()) return Result.success(searched)

                    return Result.success(emptyList())
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
    }
}
