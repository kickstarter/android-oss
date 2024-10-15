package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.extensions.deadlineCountdownDetail
import com.kickstarter.libs.utils.extensions.deadlineCountdownValue
import com.kickstarter.models.Project
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.viewholders.compose.search.FeaturedSearchViewHolder
import com.kickstarter.ui.viewholders.compose.search.ProjectSearchViewHolder
import com.kickstarter.ui.views.compose.search.SearchEmptyView
import com.kickstarter.ui.views.compose.search.SearchTopBar

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchScreenPreviewNonEmpty() {
    KSTheme {
        SearchScreen(
            onBackClicked = { },
            scaffoldState = rememberScaffoldState(),
            isLoading = false,
            isPopularList = true,
            itemsList = List(100) {
                Project.builder()
                    .name("This is a test $it")
                    .pledged((it * 2).toDouble())
                    .goal(100.0)
                    .state(if (it in 10..20) Project.STATE_SUBMITTED else Project.STATE_LIVE)
                    .build()
            },
            lazyColumnListState = rememberLazyListState(),
            showEmptyView = false,
            onSearchTermChanged = {},
            onItemClicked = { project -> }
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchScreenPreviewEmpty() {
    KSTheme {
        SearchScreen(
            onBackClicked = { },
            scaffoldState = rememberScaffoldState(),
            isLoading = true,
            itemsList = listOf(),
            lazyColumnListState = rememberLazyListState(),
            showEmptyView = true,
            onSearchTermChanged = {},
            onItemClicked = { project -> }
        )
    }
}

enum class SearchScreenTestTag {
    BACK_BUTTON,
    SEARCH_TEXT_INPUT,
    EMPTY_VIEW,
    LOADING_VIEW,
    IN_LIST_LOADING_VIEW,
    LIST_VIEW,
    POPULAR_PROJECTS_TITLE,
    FEATURED_PROJECT_VIEW,
    NORMAL_PROJECT_VIEW
}

@Composable
fun SearchScreen(
    environment: Environment? = null,
    onBackClicked: () -> Unit,
    scaffoldState: ScaffoldState,
    isPopularList: Boolean = true,
    isLoading: Boolean,
    itemsList: List<Project> = listOf(),
    lazyColumnListState: LazyListState,
    showEmptyView: Boolean,
    onSearchTermChanged: (String) -> Unit,
    onItemClicked: (Project) -> Unit
) {
    val context = LocalContext.current
    var currentSearchTerm by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        scaffoldState = scaffoldState,
        topBar = {
            Surface(elevation = 3.dp) {
                SearchTopBar(
                    onBackPressed = onBackClicked,
                    onValueChanged = {
                        onSearchTermChanged.invoke(it)
                        currentSearchTerm = it
                    },
                )
            }
        },
        backgroundColor = colors.kds_white
    ) { padding ->
        if (showEmptyView) {
            SearchEmptyView(
                modifier = Modifier.testTag(SearchScreenTestTag.EMPTY_VIEW.name),
                environment = environment,
                currentSearchTerm = currentSearchTerm
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .testTag(SearchScreenTestTag.LIST_VIEW.name)
                    .padding(padding)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = dimensions.paddingSmall,
                    end = dimensions.paddingSmall
                ),
                state = lazyColumnListState,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(itemsList) { index, project ->
                    if (index == 0 && isPopularList) {
                        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                        Text(
                            modifier = Modifier
                                .testTag(SearchScreenTestTag.POPULAR_PROJECTS_TITLE.name)
                                .fillMaxWidth(),
                            text = stringResource(id = R.string.Popular_Projects),
                            style = typography.title2,
                            color = colors.kds_support_700,
                            textAlign = TextAlign.Start
                        )
                    }

                    if (index == 0) {
                        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                        FeaturedSearchViewHolder(
                            modifier = Modifier
                                .testTag(SearchScreenTestTag.FEATURED_PROJECT_VIEW.name),
                            imageUrl = project.photo()?.full(),
                            title = project.name(),
                            isLaunched = project.isLive,
                            fundedAmount = project.percentageFunded().toInt(),
                            timeRemainingString = environment?.ksString()?.let {
                                NumberUtils.format(
                                    project.deadlineCountdownValue(),
                                ) + " " + project.deadlineCountdownDetail(context, it)
                            } ?: ""
                        ) {
                            onItemClicked(project)
                        }

                        if (itemsList.size > 1) {
                            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                            KSDividerLineGrey()

                            Spacer(modifier = Modifier.height(dimensions.paddingXSmall))
                        }
                    } else {
                        ProjectSearchViewHolder(
                            modifier = Modifier
                                .testTag(SearchScreenTestTag.NORMAL_PROJECT_VIEW.name + index),
                            imageUrl = project.photo()?.med(),
                            title = project.name(),
                            isLaunched = project.isLive,
                            fundedAmount = project.percentageFunded().toInt(),
                            timeRemainingString = environment?.ksString()?.let {
                                NumberUtils.format(
                                    project.deadlineCountdownValue(),
                                ) + " " + project.deadlineCountdownDetail(context, it)
                            } ?: ""
                        ) {
                            onItemClicked(project)
                        }

                        if (index < itemsList.size - 1) {
                            Spacer(modifier = Modifier.height(dimensions.paddingXSmall))

                            KSDividerLineGrey()

                            Spacer(modifier = Modifier.height(dimensions.paddingXSmall))
                        } else {
                            Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))
                        }
                    }
                }

                item(isLoading) {
                    if (isLoading && itemsList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                        KSCircularProgressIndicator(
                            modifier = Modifier
                                .testTag(SearchScreenTestTag.IN_LIST_LOADING_VIEW.name)
                                .size(size = dimensions.imageSizeLarge)
                        )

                        Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                    }
                }
            }
        }

        if (isLoading && itemsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .testTag(SearchScreenTestTag.LOADING_VIEW.name)
                    .fillMaxSize()
                    .background(color = colors.kds_black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                KSCircularProgressIndicator()
            }
        }
    }
}
