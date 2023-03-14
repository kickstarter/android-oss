package com.kickstarter.ui.activities.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.junit.Test

class PreLaunchProjectPageScreenTest : KSRobolectricTestCase() {

    private val projectImage = composeTestRule.onNodeWithTag("Project Image")
    private val comingSoonBadge = composeTestRule.onNodeWithTag("Coming soon badge")
    private val projectName = composeTestRule.onNodeWithTag("Project name")
    private val projectCreatorLayout = composeTestRule.onNodeWithTag("Creator Layout")
    private val projectDescription = composeTestRule.onNodeWithTag("Project description")
    private val projectCategory = composeTestRule.onNodeWithTag("Project category name")
    private val projectLocation = composeTestRule.onNodeWithTag("Project location name")
    private val projectSaveButton = composeTestRule.onNodeWithTag("Project save Button")
    private val projectFollowers = composeTestRule.onNodeWithTag("Project followers")

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
        projectSaveButton.assertTextEquals("Notify me on launch")
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
        comingSoonBadge.assertTextEquals("Coming soon")

        projectName.assertTextEquals("Some Name")
        projectCreatorLayout.assertTextEquals("Created by", "Some Name")
        projectDescription.assertTextEquals("Some blurb")
        projectCategory.assertExists()
        projectLocation.assertExists()
        projectSaveButton.assertExists()
        projectSaveButton.assertTextEquals("Notify me on launch")
        projectFollowers.assertTextEquals("1 followers")

        // Save project clicked
        projectSaveButton.performClick()

        projectSaveButton.assertTextEquals("Saved")
        projectFollowers.assertTextEquals("2 followers")
    }
}
