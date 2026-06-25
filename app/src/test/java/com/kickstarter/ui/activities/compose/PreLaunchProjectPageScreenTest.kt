package com.kickstarter.ui.activities.compose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.features.projectstory.ProjectStoryUiState
import com.kickstarter.features.projectstory.data.RichTextComponent
import com.kickstarter.features.projectstory.data.RichTextItem
import com.kickstarter.features.projectstory.data.StoriedProject
import com.kickstarter.features.projectstory.ui.ProjectStoryCaptionedImageTestTag
import com.kickstarter.features.projectstory.ui.ProjectStoryComponentTestTag
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Urls
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.COMING_SOON_BADGE
import com.kickstarter.ui.activities.compose.PreLaunchProjectPageScreenTestTag.CONTENT_LIST
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
import io.mockk.every
import io.mockk.mockk
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

    private val contentList = composeTestRule.onNodeWithTag(CONTENT_LIST.name)

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
        val projectStoryUiState = mutableStateOf(
            ProjectStoryUiState(
                isLoading = false, error = null, storiedProject = null
            )
        )

        composeTestRule.setContent {
            KSTheme {
                val projectState = remember { mutableStateOf(null) }
                val similarProjectsState = remember { mutableStateOf(SimilarProjectsUiState()) }
                val projectStoryState = remember { projectStoryUiState }
                PreLaunchProjectPageScreen(projectState, similarProjectsState, projectStoryState)
            }
        }

        similarProjectsContainer.assertDoesNotExist()

        projectStoryUiState.value = ProjectStoryUiState(
            isLoading = true, error = null, storiedProject = null
        )

        similarProjectsContainer.assertDoesNotExist()

        projectStoryUiState.value = ProjectStoryUiState(
            isLoading = false, error = Throwable(), storiedProject = null
        )

        similarProjectsContainer.assertIsDisplayed()

        projectStoryUiState.value = ProjectStoryUiState(
            isLoading = false, error = null, storiedProject = StoriedProject(Project.builder().build(), null)
        )

        similarProjectsContainer.assertIsDisplayed()
    }

    @Test
    fun `test no rich text components are displayed when items list is empty`() {
        val richTextComponent = RichTextComponent(
            items = listOf()
        )

        composeTestRule.setContent {
            KSTheme {
                val projectState = remember { mutableStateOf(null) }
                val similarProjectsState = remember { mutableStateOf(SimilarProjectsUiState()) }
                val projectStoryState = remember {
                    mutableStateOf(
                        ProjectStoryUiState(
                            isLoading = false,
                            error = null,
                            storiedProject = StoriedProject(Project.builder().build(), richTextComponent)
                        )
                    )
                }
                PreLaunchProjectPageScreen(projectState, similarProjectsState, projectStoryState)
            }
        }

        composeTestRule.onAllNodesWithTag(PreLaunchProjectPageScreenTestTag.RICH_TEXT_COMPONENT.name).assertCountEquals(0)
    }

    @Test
    fun `test webview component only renders when url is not empty`() {
        val projectStoryUiState = mutableStateOf(
            ProjectStoryUiState(
                isLoading = false, error = null,
                StoriedProject(
                    Project.builder().build(),
                    RichTextComponent(
                        items = listOf(
                            mockk<RichTextItem.Oembed>(relaxed = true).apply {
                                every { iframeUrl } returns "https://www.youtube.com/embed/ExB50D08nE8?feature=oembed"
                            }
                        )
                    )
                )
            )
        )

        composeTestRule.setContent {
            KSTheme {
                val projectState = remember { mutableStateOf(null) }
                val similarProjectsState = remember { mutableStateOf(SimilarProjectsUiState()) }
                val projectStoryState = remember { projectStoryUiState }
                PreLaunchProjectPageScreen(projectState, similarProjectsState, projectStoryState)
            }
        }

        composeTestRule.onAllNodesWithTag(PreLaunchProjectPageScreenTestTag.RICH_TEXT_COMPONENT.name).assertCountEquals(1)
        composeTestRule.onNodeWithTag(ProjectStoryComponentTestTag.WEBVIEW.name).assertExists()

        projectStoryUiState.value = ProjectStoryUiState(
            isLoading = false, error = null,
            StoriedProject(
                Project.builder().build(),
                RichTextComponent(
                    items = listOf(
                        mockk<RichTextItem.Oembed>(relaxed = true).apply {
                            every { iframeUrl } returns ""
                        }
                    )
                )
            )
        )

        composeTestRule.onAllNodesWithTag(PreLaunchProjectPageScreenTestTag.RICH_TEXT_COMPONENT.name).assertCountEquals(1)
        composeTestRule.onNodeWithTag(ProjectStoryComponentTestTag.WEBVIEW.name).assertDoesNotExist()
    }

    @Test
    fun `test rich text paragraph with photo child renders photo component`() {
        val projectStoryUiState = mutableStateOf(
            ProjectStoryUiState(
                isLoading = false, error = null,
                StoriedProject(
                    Project.builder().build(),
                    RichTextComponent(
                        items = listOf(
                            mockk<RichTextItem.Text.Paragraph>(relaxed = true).apply {
                                every { children } returns listOf(
                                    mockk<RichTextItem.Text.ChildParagraph>(relaxed = true)
                                )
                            },
                            mockk<RichTextItem.Text.Paragraph>(relaxed = true).apply {
                                every { children } returns listOf(
                                    mockk<RichTextItem.Photo>(relaxed = true).apply {
                                        every { url } returns "https://example.com/photo.jpg"
                                        every { altText } returns "Example Photo"
                                        every { caption } returns "This is an example photo."
                                    }
                                )
                            }
                        )
                    )
                )
            )
        )

        composeTestRule.setContent {
            KSTheme {
                val projectState = remember { mutableStateOf(null) }
                val similarProjectsState = remember { mutableStateOf(SimilarProjectsUiState()) }
                val projectStoryState = remember { projectStoryUiState }
                PreLaunchProjectPageScreen(projectState, similarProjectsState, projectStoryState)
            }
        }

        composeTestRule.onAllNodesWithTag(PreLaunchProjectPageScreenTestTag.RICH_TEXT_COMPONENT.name).assertCountEquals(2)
        composeTestRule.onAllNodesWithTag(ProjectStoryComponentTestTag.TEXT.name).assertCountEquals(1)
        composeTestRule.onAllNodesWithTag(ProjectStoryComponentTestTag.PHOTO.name).assertCountEquals(1)
    }

    @Test
    fun `test rich items render expected components and in order`() {
        val projectStoryUiState = mutableStateOf(
            ProjectStoryUiState(
                isLoading = false, error = null,
                StoriedProject(
                    Project.builder().build(),
                    RichTextComponent(
                        items = listOf(
                            mockk<RichTextItem.Text.Paragraph>(relaxed = true).apply {
                                every { text } returns "Paragraph Text"
                            },
                            mockk<RichTextItem.Text.Paragraph>(relaxed = true).apply {
                                every { children } returns listOf(
                                    mockk<RichTextItem.Text.ChildParagraph>(relaxed = true).apply {
                                        every { text } returns "Child Paragraph Text"
                                    }
                                )
                            },
                            mockk<RichTextItem.Text.Header>(relaxed = true).apply {
                                every { text } returns "Header Text"
                            },
                            mockk<RichTextItem.ListOpen>(relaxed = true), // Doesn't render a particular component. Will follow up on a test after the data model is cleaned up.
                            mockk<RichTextItem.Text.ListItem>(relaxed = true).apply {
                                every { text } returns "List Item Text"
                            },
                            mockk<RichTextItem.ListClose>(relaxed = true), // Doesn't render a particular component. Will follow up on a test after the data model is cleaned up.
                            mockk<RichTextItem.Photo>(relaxed = true),
                            mockk<RichTextItem.Oembed>(relaxed = true).apply {
                                every { iframeUrl } returns "https://www.youtube.com/embed/ExB50D08nE8?feature=oembed"
                            }
                        )
                    )
                )
            )
        )

        composeTestRule.setContent {
            KSTheme {
                val projectState = remember { mutableStateOf(null) }
                val similarProjectsState = remember { mutableStateOf(SimilarProjectsUiState()) }
                val projectStoryState = remember { projectStoryUiState }
                PreLaunchProjectPageScreen(projectState, similarProjectsState, projectStoryState)
            }
        }

        val initialIndex = 1 // First two items are the Pre-launch Project Header and a Spacer

        contentList.performScrollToIndex(initialIndex + 1)
        composeTestRule.onNode(hasTestTag(ProjectStoryComponentTestTag.TEXT.name) and hasText("Paragraph Text")).assertExists()

        contentList.performScrollToIndex(initialIndex + 2)
        composeTestRule.onNode(hasTestTag(ProjectStoryComponentTestTag.TEXT.name) and hasText("Child Paragraph Text")).assertExists()

        contentList.performScrollToIndex(initialIndex + 3)
        composeTestRule.onNode(hasTestTag(ProjectStoryComponentTestTag.TEXT.name) and hasText("Header Text")).assertExists()

        contentList.performScrollToIndex(initialIndex + 5)
        composeTestRule.onNode(hasTestTag(ProjectStoryComponentTestTag.TEXT.name) and hasText("List Item Text")).assertExists()

        contentList.performScrollToIndex(initialIndex + 5)
        composeTestRule.onNode(hasTestTag(ProjectStoryComponentTestTag.TEXT.name) and hasText("List Item Text")).assertExists()

        contentList.performScrollToIndex(initialIndex + 7)
        composeTestRule.onNode(hasTestTag(ProjectStoryComponentTestTag.PHOTO.name)).assertExists()
        composeTestRule.onNode(hasTestTag(ProjectStoryCaptionedImageTestTag.IMAGE.name)).assertExists()

        contentList.performScrollToIndex(initialIndex + 8)
        composeTestRule.onNode(hasTestTag(ProjectStoryComponentTestTag.WEBVIEW.name)).assertExists()
    }
}
