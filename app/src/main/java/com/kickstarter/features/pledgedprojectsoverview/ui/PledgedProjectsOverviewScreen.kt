package com.kickstarter.features.pledgedprojectsoverview.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.toolbars.compose.TopToolBar
import kotlinx.coroutines.flow.flowOf

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PledgedProjectsOverviewScreenPreview() {
    KSTheme {
        Scaffold(
            backgroundColor = colors.backgroundSurfacePrimary
        ) { padding ->
            val ppoCardList1 = (0..10).map {
                PPOCardDataMock()
            }
            val ppoCardList = flowOf(PagingData.from(ppoCardList1)).collectAsLazyPagingItems()
            PledgedProjectsOverviewScreen(
                modifier = Modifier.padding(padding),
                lazyColumnListState = rememberLazyListState(),
                ppoCards = ppoCardList,
                totalAlerts = 0,
                onBackPressed = {},
                onSendMessageClick = {},
                onSeeAllBackedProjectsClick = {},
                errorSnackBarHostState = SnackbarHostState()
            )
        }
    }
}

@Composable
fun PledgedProjectsOverviewScreen(
    modifier: Modifier,
    onBackPressed: () -> Unit,
    lazyColumnListState: LazyListState,
    errorSnackBarHostState: SnackbarHostState,
    ppoCards: LazyPagingItems<PPOCardDataMock>,
    totalAlerts: Int = 0,
    onSendMessageClick: (projectName: String) -> Unit,
    onSeeAllBackedProjectsClick : () -> Unit,
    isLoading: Boolean = false,
    isErrored: Boolean = false,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = errorSnackBarHostState
                )
            },
            modifier = modifier,
            topBar = {
                TopToolBar(
                    title = stringResource(id = R.string.project_alerts_fpo),
                    titleColor = colors.textPrimary,
                    leftOnClickAction = onBackPressed,
                    leftIconColor = colors.icon,
                    leftIconModifier = Modifier.testTag(PledgedProjectsOverviewScreenTestTag.BACK_BUTTON.name),
                    backgroundColor = colors.backgroundSurfacePrimary,
                )
            },
            backgroundColor = colors.backgroundSurfacePrimary
        ) { padding ->
            if (isErrored) {
                //show errored screen
            }
            else if (totalAlerts == 0) {
                PPOScreenEmptyState(onSeeAllBackedProjectsClick)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(
                            start = dimensions.paddingMedium,
                            end = dimensions.paddingMedium,
                            top = dimensions.paddingMedium
                        )
                        .padding(paddingValues = padding),
                    state = lazyColumnListState
                ) {
                    item {
                        Text(
                            text = stringResource(id = R.string.alerts_fpo, totalAlerts),
                            style = typography.title3Bold,
                            color = colors.textPrimary
                        )
                    }

                    items(
                        count = ppoCards.itemCount
                    ) { index ->
                        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                        ppoCards[index]?.let {
                            PPOCardView(
                                viewType = it.viewType,
                                onCardClick = { },
                                projectName = it.projectName,
                                pledgeAmount = it.pledgeAmount,
                                imageUrl = it.imageUrl,
                                imageContentDescription = it.imageContentDescription,
                                creatorName = it.creatorName,
                                sendAMessageClickAction = { onSendMessageClick(it.projectSlug) },
                                shippingAddress = it.shippingAddress,
                                showBadge = it.showBadge,
                                onActionButtonClicked = { },
                                onSecondaryActionButtonClicked = { },
                                timeNumberForAction = it.timeNumberForAction
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(dimensions.paddingDoubleLarge))
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KSTheme.colors.backgroundAccentGraySubtle.copy(alpha = 0.5f))
                        .clickable(enabled = false) { },
                    contentAlignment = Alignment.Center
                ) {
                    KSCircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun PPOScreenEmptyState(
    onSeeAllBackedProjectsClick : () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(
                start = dimensions.paddingMedium,
                end = dimensions.paddingMedium,
                top = dimensions.paddingMedium
            ),
        horizontalAlignment = Alignment.CenterHorizontally ,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            color = colors.textPrimary,
            text = stringResource(id = R.string.youre_all_caught_up_fpo),
            style = typography.title3Bold,
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

        Text(
            color = colors.textPrimary,
            text = stringResource(id = R.string.when_projects_youve_backed_need_your_attention_youll_see_them_here_fpo),
            style = typography.body,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

        KSPrimaryGreenButton(
            modifier = Modifier,
            onClickAction = { onSeeAllBackedProjectsClick.invoke() },
            text = stringResource(id = R.string.see_all_backed__projects_fpo),
            isEnabled = true)
    }
}

enum class PledgedProjectsOverviewScreenTestTag {
    BACK_BUTTON,
}

// For preview purposes only, will remove once we have the PPO Card payload model from graph
data class PPOCardDataMock(
    val viewType: PPOCardViewType = PPOCardViewType.FIX_PAYMENT,
    val onCardClick: () -> Unit = { },
    val projectName: String = "This is a project name",
    val projectSlug: String = "",
    val pledgeAmount: String = "$14.00",
    val imageUrl: String = "",
    val imageContentDescription: String = "",
    val creatorName: String = "Creator Name",
    val sendAMessageClickAction: () -> Unit = { },
    val shippingAddress: String = "",
    val addressId: String = "",
    val backingID: String = "",
    val showBadge: Boolean = true,
    val onActionButtonClicked: () -> Unit = {},
    val onSecondaryActionButtonClicked: () -> Unit = {},
    val timeNumberForAction: Int = 25
)
