package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.isDarkModeEnabled
import com.kickstarter.mock.factories.UpdateFactory
import com.kickstarter.models.Update
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.toolbars.compose.TopToolBar
import kotlinx.coroutines.flow.flowOf

class PaginationActivity : ComponentActivity() {
    private lateinit var viewModelFactory: Factory
    val viewModel: PaginationViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        this.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env)

            setContent {
                val darModeEnabled = this.isDarkModeEnabled(env = env)

                val updatesPagingSource = viewModel.projectUpdatesState.collectAsLazyPagingItems()
                val total = viewModel.totalItemsState.collectAsStateWithLifecycle()

                KickstarterApp(useDarkTheme = darModeEnabled) {
                    Screen(updatesPagingSource, total.value) { viewModel.loadUpdates() }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewScreen() {
    val list = List(100) {
        UpdateFactory.update().toBuilder().id(it.toLong()).build()
    }
    val updatesList = flowOf(PagingData.from(list)).collectAsLazyPagingItems()
    Screen(updatesPagingSource = updatesList, total = 100)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Screen(
    updatesPagingSource: LazyPagingItems<Update>,
    total: Int = 0,
    pullRefreshCallback: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopToolBar(
                title = "Project Updates",
                titleColor = KSTheme.colors.kds_black,
                leftIconColor = KSTheme.colors.kds_black,
                backgroundColor = KSTheme.colors.kds_white
            )
        },
        scaffoldState = rememberScaffoldState(),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        val isLoading = !updatesPagingSource.loadState.isIdle
        val pullRefreshState = rememberPullRefreshState(isLoading, pullRefreshCallback)
        val lazyListState = rememberLazyListState()

        Text(
            modifier = Modifier.padding(innerPadding),
            text = " Complete collection of items $total"
        )
        ItemsList(
            modifier = Modifier.padding(innerPadding),
            listState = lazyListState,
            pullToRefreshIsLoading = isLoading,
            pullRefreshState = pullRefreshState,
            updates = updatesPagingSource
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ItemsList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    pullToRefreshIsLoading: Boolean,
    pullRefreshState: PullRefreshState,
    updates: LazyPagingItems<Update>
) {
    Box(
        modifier = modifier
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Here we use the standard items API
            items(
                count = updates.itemCount,
            ) { index ->

                // As the standard items call provides only the index, we get the item directly from our lazyPagingItems
                updates[index]?.let {
                    UpdateCard(update = it, index = index)
                }
            }
        }

        PullRefreshIndicator(
            pullToRefreshIsLoading,
            pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun UpdateCard(update: Update, index: Int) {
    Card(
        modifier = Modifier
            .padding(12.dp),
        backgroundColor = KSTheme.colors.kds_black.copy(alpha = 0.1f)
    ) {
        Spacer(modifier = Modifier.height(dimensions.paddingMedium))
        Column(
            modifier = Modifier
                .background(KSTheme.colors.kds_black.copy(alpha = 0.1f))
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "Index: $index | publishedAt: ${update.publishedAt()} |update ID: ${update.id()} | update title: ${update.title()}",
                style = KSTheme.typography.title2,
                textAlign = TextAlign.Start
            )
            Text(
                text = " * ${update.truncatedBody()}",
                style = KSTheme.typography.body,
            )
            Spacer(modifier = Modifier.height(dimensions.paddingMedium))
        }
    }
}
