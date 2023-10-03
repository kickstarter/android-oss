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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
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

@Composable
fun SearchScreen(
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
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Surface(elevation = 3.dp) {
                SearchTopBar(
                    onBackPressed = onBackClicked,
                    onValueChanged = {
                        onSearchTermChanged.invoke(it)
                    },
                )
            }
        },
        backgroundColor = colors.kds_white
    ) { padding ->
        if (showEmptyView) {
            SearchEmptyView()
        } else {
            LazyColumn(
                modifier = Modifier
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
                            text = stringResource(id = R.string.Popular_Projects),
                            style = typography.title2,
                            color = colors.kds_support_700
                        )
                    }

                    if (index == 0) {
                        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                        FeaturedSearchViewHolder(
                            imageUrl = project.photo()?.full(),
                            title = project.name(),
                            isLaunched = project.isLive,
                            fundedAmount = project.percentageFunded().toInt()
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
                            imageUrl = project.photo()?.med(),
                            title = project.name(),
                            isLaunched = project.isLive,
                            fundedAmount = project.percentageFunded().toInt()
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

                        KSCircularProgressIndicator(Modifier.size(size = dimensions.imageSizeLarge))

                        Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                    }
                }
            }
        }

        if (isLoading && itemsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colors.kds_black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                KSCircularProgressIndicator()
            }
        }
    }
}
