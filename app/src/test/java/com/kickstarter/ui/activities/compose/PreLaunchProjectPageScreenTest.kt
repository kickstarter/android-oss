package com.kickstarter.ui.activities.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.apollographql.apollo.api.CustomTypeValue
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.graphql.DateTimeAdapter
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Urls
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.COMING_SOON_BADGE
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.CREATOR_LAYOUT
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_CATEGORY_NAME
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_DESCRIPTION
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_FOLLOWERS
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_IMAGE
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_LOCATION_NAME
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_NAME
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.PROJECT_SAVE_BUTTON
import org.junit.Test

class PreLaunchProjectPageScreenTest : KSRobolectricTestCase() {
    private val projectImage = composeTestRule.onNodeWithTag(PROJECT_IMAGE.name)
    private val comingSoonBadge = composeTestRule.onNodeWithTag(COMING_SOON_BADGE.name)
    private val projectName = composeTestRule.onNodeWithTag(PROJECT_NAME.name)
    private val projectCreatorLayout = composeTestRule.onNodeWithTag(CREATOR_LAYOUT.name)
    private val projectDescription = composeTestRule.onNodeWithTag(PROJECT_DESCRIPTION.name)
    private val projectCategory = composeTestRule.onNodeWithTag(PROJECT_CATEGORY_NAME.name)
    private val projectLocation = composeTestRule.onNodeWithTag(PROJECT_LOCATION_NAME.name)
    private val projectSaveButton = composeTestRule.onNodeWithTag(PROJECT_SAVE_BUTTON.name)
    private val projectFollowers = composeTestRule.onNodeWithTag(PROJECT_FOLLOWERS.name)

    @Test
    fun verifyInitState() {
        composeTestRule.setContent { // setting our composable as content for test
            MaterialTheme {
                val projectState = remember { mutableStateOf(null) }
                PreLaunchProjectPageScreen(projectState)
            }
        }
        projectImage.assertExists()
        comingSoonBadge.assertExists()
        projectName.assertTextEquals("")
        projectName.assertIsNotDisplayed()
        projectCreatorLayout.assertExists()
        projectDescription.assertDoesNotExist()
        projectCategory.assertDoesNotExist()
        projectLocation.assertDoesNotExist()
        projectSaveButton.assertExists()
        projectSaveButton.assertTextEquals(context().getString(R.string.Notify_me_on_launch))
        projectFollowers.assertDoesNotExist()
    }

    @Test
    fun verifyOnSaveButtonClicked() {
        val project = ProjectFactory.project().toBuilder()
            .launchedAt(
                DateTimeAdapter().decode(CustomTypeValue.fromRawValue(0))
            )
            .displayPrelaunch(true)
            .watchesCount(1)
            .urls(Urls.builder().build()).build()

        composeTestRule.setContent { // setting our composable as content for test
            MaterialTheme {
                val projectState = remember { mutableStateOf(project) }

                PreLaunchProjectPageScreen(
                    projectState,
                    onButtonClicked = {
                        projectState.value = project.toBuilder().isStarred(true).watchesCount(2).build()
                    },
                    numberOfFollowers = environment().ksString()?.format(
                        context().getString(R.string.activity_followers),
                        "number_of_followers",
                        projectState.value.watchesCount().toString()
                    )
                )
            }
        }

        projectImage.assertExists()

        comingSoonBadge.assertExists()
        comingSoonBadge.assertTextEquals(context().getString(R.string.Coming_soon))

        projectName.assertTextEquals("Some Name")
        projectCreatorLayout.assertTextEquals(context().getString(R.string.project_menu_created_by), "Some Name")
        projectDescription.assertTextEquals("Some blurb")
        projectCategory.assertExists()
        projectLocation.assertExists()
        projectSaveButton.assertExists()
        projectSaveButton.assertTextEquals(context().getString(R.string.Notify_me_on_launch))
        projectFollowers.assertTextEquals("1 followers")

        // Save project clicked
        projectSaveButton.performClick()

        projectSaveButton.assertTextEquals(context().getString(R.string.Saved))
        projectFollowers.assertTextEquals("2 followers")
    }
}
