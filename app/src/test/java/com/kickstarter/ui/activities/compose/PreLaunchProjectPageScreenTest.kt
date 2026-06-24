package com.kickstarter.ui.activities.compose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.features.projectstory.ProjectStoryUiState
import com.kickstarter.features.projectstory.data.StoriedProject
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
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
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.SIMILAR_PROJECTS_CONTAINER
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.viewmodels.projectpage.SimilarProjectsUiState
import org.joda.time.DateTime
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
    private val similarProjectsContainer = composeTestRule.onNodeWithTag(SIMILAR_PROJECTS_CONTAINER.name)

    @Test
    fun verifyInitState() {
        composeTestRule.setContent { // setting our composable as content for test
            KSTheme {
                val projectState = remember { mutableStateOf(null) }
                val similarProjectsState = remember { mutableStateOf(SimilarProjectsUiState()) }
                val projectStoryState = remember { mutableStateOf(ProjectStoryUiState()) }
                PreLaunchProjectPageScreen(projectState, similarProjectsState, projectStoryState)
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
        similarProjectsContainer.assertDoesNotExist()
    }

    @Test
    fun verifyOnSaveButtonClicked() {
        val project = ProjectFactory.project().toBuilder()
            .launchedAt(DateTime(0))
            .displayPrelaunch(true)
            .watchesCount(1)
            .urls(Urls.builder().build()).build()

        composeTestRule.setContent { // setting our composable as content for test
            KSTheme {
                val projectState = remember { mutableStateOf(project) }
                val similarProjectsState = remember { mutableStateOf(SimilarProjectsUiState()) }
                val projectStoryState = remember { mutableStateOf(ProjectStoryUiState()) }

                PreLaunchProjectPageScreen(
                    projectState,
                    similarProjectsState,
                    projectStoryState,
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

    @Test
    fun `test spc is displayed when project story state is either successful or failed but not loading`() {
        val projectStoryState = mutableStateOf(
            ProjectStoryUiState(
                isLoading = false, error = null, storiedProject = null
            )
        )

        composeTestRule.setContent {
            KSTheme {
                val projectState = remember { mutableStateOf(null) }
                val similarProjectsState = remember { mutableStateOf(SimilarProjectsUiState()) }
                val projectStoryState = remember { projectStoryState }
                PreLaunchProjectPageScreen(projectState, similarProjectsState, projectStoryState)
            }
        }

        similarProjectsContainer.assertDoesNotExist()

        projectStoryState.value = ProjectStoryUiState(
            isLoading = true, error = null, storiedProject = null
        )

        similarProjectsContainer.assertDoesNotExist()

        projectStoryState.value = ProjectStoryUiState(
            isLoading = false, error = Throwable(), storiedProject = null
        )

        similarProjectsContainer.assertIsDisplayed()

        projectStoryState.value = ProjectStoryUiState(
            isLoading = false, error = null, storiedProject = StoriedProject(Project.builder().build(), null)
        )

        similarProjectsContainer.assertIsDisplayed()
    }
}
