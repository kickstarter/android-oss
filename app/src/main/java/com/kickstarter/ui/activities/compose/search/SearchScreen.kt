package com.kickstarter.ui.activities.compose.search

import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.models.Project
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.toolbars.compose.TopToolBar
import com.kickstarter.ui.viewholders.compose.search.FeaturedSearchViewHolder
import com.kickstarter.ui.viewholders.compose.search.ProjectSearchViewHolder

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchScreenPreview() {
    KSTheme {
        SearchScreen(
            onBackClicked = { },
            scaffoldState = rememberScaffoldState(),
            isPopularList = true,
            itemsList = List(100) {
                Project.builder()
                    .name("This is a test $it")
                    .pledged((it * 2).toDouble())
                    .goal(100.0)
                    .state(if (it in 10..20) Project.STATE_SUBMITTED else Project.STATE_LIVE)
                    .build()
            },
            onItemClicked = { project -> }
        )
    }
}

@Composable
fun SearchScreen(
    onBackClicked: () -> Unit,
    scaffoldState: ScaffoldState,
    isPopularList: Boolean,
    itemsList: List<Project> = listOf(),
    onItemClicked: (Project) -> Unit
) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopToolBar(
                title = stringResource(id = R.string.tabbar_search),
                titleColor = colors.kds_support_700,
                leftOnClickAction = onBackClicked,
                leftIconColor = colors.kds_support_700,
                backgroundColor = colors.kds_white,
            )
        },
        backgroundColor = colors.kds_white
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentPadding = PaddingValues(
                start = dimensions.paddingSmall,
                end = dimensions.paddingSmall
            )
        ) {
            itemsIndexed(itemsList) { index, project ->
                if (index == 0 && isPopularList) {
                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                    Text(
                        text = stringResource(id = R.string.Popular_Projects),
                        style = typography.title2,
                        color = colors.kds_support_700
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                }

                if (index == 0) {
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
        }
    }
}
