package com.kickstarter.features.pledgedprojectsoverview.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kickstarter.R
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCard
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCardFactory
import com.kickstarter.ui.compose.designsystem.KSAlertDialog
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
                PPOCardFactory.confirmAddressCard()
            }
            val ppoCardPagingList = flowOf(PagingData.from(ppoCardList1)).collectAsLazyPagingItems()
            PledgedProjectsOverviewScreen(
                modifier = Modifier.padding(padding),
                lazyColumnListState = rememberLazyListState(),
                ppoCards = ppoCardPagingList,
                totalAlerts = 10,
                onBackPressed = {},
                onAddressConfirmed = {},
                onCardClick = {},
                onProjectPledgeSummaryClick = {},
                onSendMessageClick = {},
                errorSnackBarHostState = SnackbarHostState()
            )
        }
    }
}

@Composable
fun PledgedProjectsOverviewScreen(
    modifier: Modifier,
    onBackPressed: () -> Unit,
    onAddressConfirmed: () -> Unit,
    lazyColumnListState: LazyListState,
    errorSnackBarHostState: SnackbarHostState,
    ppoCards: LazyPagingItems<PPOCard>,
    totalAlerts: Int = 0,
    onCardClick: () -> Unit,
    onProjectPledgeSummaryClick: (backingDetailsUrl: String) -> Unit,
    onSendMessageClick: (projectName: String) -> Unit
) {
    val openConfirmAddressAlertDialog = remember { mutableStateOf(false) }
    var confirmedAddress by remember { mutableStateOf("") } // TODO: This is either the original shipping address or the user-edited address

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
                            viewType = it.viewType() ?: PPOCardViewType.UNKNOWN,
                            onCardClick = { },
                            onProjectPledgeSummaryClick = { onProjectPledgeSummaryClick(it.backingDetailsUrl() ?: "") },
                            projectName = it.projectName(),
                            pledgeAmount = it.amount(),
                            imageUrl = it.imageUrl(),
                            imageContentDescription = it.imageContentDescription(),
                            creatorName = it.creatorName(),
                            sendAMessageClickAction = { onSendMessageClick(it.projectSlug() ?: "") },
                            shippingAddress = it.address() ?: "", // TODO replace with formatted address from PPO response
                            showBadge = it.showBadge(),
                            onActionButtonClicked = { },
                            onSecondaryActionButtonClicked = {
                                when (it.viewType()) {
                                    PPOCardViewType.CONFIRM_ADDRESS -> {
                                        confirmedAddress = it.address() ?: ""
                                        openConfirmAddressAlertDialog.value = true
                                    }
                                    else -> {}
                                }
                            },
                            timeNumberForAction = it.timeNumberForAction()
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(dimensions.paddingDoubleLarge))
                }
            }
        }
    }

    when {
        openConfirmAddressAlertDialog.value -> {
            KSAlertDialog(
                setShowDialog = { openConfirmAddressAlertDialog.value = it },
                headlineText = "Confirm your address",
                bodyText = confirmedAddress,
                leftButtonText = stringResource(id = R.string.Cancel),
                leftButtonAction = { openConfirmAddressAlertDialog.value = false },
                rightButtonText = stringResource(id = R.string.Confirm),
                rightButtonAction = {
                    openConfirmAddressAlertDialog.value = false

                    // Call confirm address API
                    // TODO: MBL-1556 Add network call to confirm address

                    // Show snackbar and refresh list
                    onAddressConfirmed()
                }
            )
        }
    }
}

enum class PledgedProjectsOverviewScreenTestTag {
    BACK_BUTTON,
}
