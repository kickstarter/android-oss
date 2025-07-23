package com.kickstarter.features.pledgedprojectsoverview.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue.Hidden
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kickstarter.R
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCard
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCardFactory
import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.format
import com.kickstarter.libs.utils.extensions.isNullOrZero
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.ui.compose.designsystem.KSAlertDialog
import com.kickstarter.ui.compose.designsystem.KSErrorSnackbar
import com.kickstarter.ui.compose.designsystem.KSHeadsupSnackbar
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSSnackbarTypes
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.toolbars.compose.TopToolBar
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PledgedProjectsOverviewScreenPreview() {
    KSTheme {
        Scaffold(
            backgroundColor = colors.backgroundSurfacePrimary
        ) { padding ->
            val ppoCardList1 = (0..10).map {
                PPOCardFactory.fixPaymentCard()
            }
            val ppoCardPagingList = flowOf(PagingData.from(ppoCardList1)).collectAsLazyPagingItems()
            PledgedProjectsOverviewScreen(
                modifier = Modifier.padding(padding),
                lazyColumnListState = rememberLazyListState(),
                ppoCards = ppoCardPagingList,
                totalAlerts = 10,
                onBackPressed = {},
                onPrimaryActionButtonClicked = {},
                onSecondaryActionButtonClicked = {},
                onAddressConfirmed = { _, _ -> },
                onRewardReceivedChanged = { _, _ -> },
                onProjectPledgeSummaryClick = { _, _ -> },
                onSendMessageClick = { _, _, _, _, _ -> },
                onSeeAllBackedProjectsClick = {},
                errorSnackBarHostState = SnackbarHostState(),
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PledgedProjectsOverviewScreenErrorPreview() {
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
                onPrimaryActionButtonClicked = {},
                onSecondaryActionButtonClicked = {},
                onAddressConfirmed = { _, _ -> },
                onRewardReceivedChanged = { _, _ -> },
                onProjectPledgeSummaryClick = { _, _ -> },
                onSendMessageClick = { _, _, _, _, _ -> },
                onSeeAllBackedProjectsClick = {},
                isErrored = true,
                errorSnackBarHostState = SnackbarHostState(),
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PledgedProjectsOverviewScreenEmptyPreview() {
    KSTheme {
        Scaffold(
            backgroundColor = colors.backgroundSurfacePrimary
        ) { padding ->
            val ppoCardPagingList = flowOf(PagingData.from(listOf<PPOCard>())).collectAsLazyPagingItems()
            PledgedProjectsOverviewScreen(
                modifier = Modifier.padding(padding),
                lazyColumnListState = rememberLazyListState(),
                ppoCards = ppoCardPagingList,
                totalAlerts = 0,
                onBackPressed = {},
                onPrimaryActionButtonClicked = {},
                onSecondaryActionButtonClicked = {},
                onAddressConfirmed = { _, _ -> },
                onRewardReceivedChanged = { _, _ -> },
                onProjectPledgeSummaryClick = { _, _ -> },
                onSendMessageClick = { _, _, _, _, _ -> },
                errorSnackBarHostState = SnackbarHostState(),
                onSeeAllBackedProjectsClick = {}
            )
        }
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PledgedProjectsOverviewScreenV2Preview() {
    KSTheme {
        Scaffold(
            backgroundColor = colors.backgroundSurfacePrimary
        ) { padding ->
            val ppoCardList1 = (0..10).map {
                PPOCardFactory.fixPaymentCard()
            }
            val ppoCardPagingList = flowOf(PagingData.from(ppoCardList1)).collectAsLazyPagingItems()
            PledgedProjectsOverviewScreen(
                modifier = Modifier.padding(padding),
                lazyColumnListState = rememberLazyListState(),
                ppoCards = ppoCardPagingList,
                totalAlerts = 10,
                onBackPressed = {},
                onPrimaryActionButtonClicked = {},
                onSecondaryActionButtonClicked = {},
                onAddressConfirmed = { _, _ -> },
                onRewardReceivedChanged = { _, _ -> },
                onProjectPledgeSummaryClick = { _, _ -> },
                onSendMessageClick = { _, _, _, _, _ -> },
                onSeeAllBackedProjectsClick = {},
                errorSnackBarHostState = SnackbarHostState(),
                v2Enabled = true,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PledgedProjectsOverviewScreen(
    modifier: Modifier,
    onBackPressed: () -> Unit,
    onAddressConfirmed: (addressID: String, backingID: String) -> Unit,
    lazyColumnListState: LazyListState,
    errorSnackBarHostState: SnackbarHostState,
    ppoCards: LazyPagingItems<PPOCard>,
    totalAlerts: Int = 0,
    onProjectPledgeSummaryClick: (url: String, isPledgeManagement: Boolean) -> Unit,
    onSendMessageClick: (projectName: String, projectID: String, ppoCards: List<PPOCard?>, totalAlerts: Int, creatorID: String) -> Unit,
    onSeeAllBackedProjectsClick: () -> Unit,
    onPrimaryActionButtonClicked: (PPOCard) -> Unit,
    onSecondaryActionButtonClicked: (PPOCard) -> Unit,
    onRewardReceivedChanged: ((String, Boolean) -> Unit),
    isLoading: Boolean = false,
    isErrored: Boolean = false,
    showEmptyState: Boolean = false,
    pullRefreshCallback: () -> Unit = {},
    analyticEvents: AnalyticEvents? = null,
    v2Enabled: Boolean = false,
) {
    val openConfirmAddressAlertDialog = remember { mutableStateOf(false) }
    var confirmedAddress by remember { mutableStateOf("") } // TODO: This is either the original shipping address or the user-edited address
    var addressID by remember { mutableStateOf("") }
    var backingID by remember { mutableStateOf("") }
    var projectID by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(
        initialValue = Hidden,
        skipHalfExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullRefreshState(
        isLoading,
        pullRefreshCallback,
    )
    ModalBottomSheetLayout(
        modifier = Modifier.testTag(PledgedProjectsOverviewScreenTestTag.BOTTOM_SHEET.name),
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(
            topStart = dimensions.paddingLarge,
            topEnd = dimensions.paddingLarge
        ),
        sheetContent = {
            BetaMessagingBottomSheet(
                onSeeAllBackedProjectsClick = onSeeAllBackedProjectsClick,
                dismiss = { coroutineScope.launch { sheetState.hide() } }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.systemBarsPadding(),
            snackbarHost = {
                SnackbarHost(
                    modifier = Modifier.padding(dimensions.paddingSmall),
                    hostState = errorSnackBarHostState,
                    snackbar = { data ->
                        // Action label is typically for the action on a snackbar, but we can
                        // leverage it and show different visuals depending on what we pass in
                        if (data.actionLabel == KSSnackbarTypes.KS_ERROR.name) {
                            KSErrorSnackbar(text = data.message)
                        } else {
                            KSHeadsupSnackbar(text = data.message)
                        }
                    }
                )
            },
            topBar = {
                TopToolBar(
                    title = if (v2Enabled) stringResource(id = R.string.Backings) else stringResource(id = R.string.Project_alerts),
                    titleColor = colors.textPrimary,
                    leftOnClickAction = onBackPressed,
                    leftIconColor = colors.icon,
                    leftIconModifier = Modifier.testTag(PledgedProjectsOverviewScreenTestTag.BACK_BUTTON.name),
                    backgroundColor = colors.backgroundSurfacePrimary,
                    showBetaPill = true,
                    right = {
                        if (v2Enabled) {
                            IconButton(
                                modifier = Modifier.testTag(PledgedProjectsOverviewScreenTestTag.INFO_BUTTON.name),
                                onClick = { coroutineScope.launch { sheetState.show() } },
                                enabled = true
                            ) {
                                Box {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_info_new),
                                        contentDescription = stringResource(
                                            id = R.string.general_navigation_accessibility_button_help_menu_label
                                        ),
                                        tint = colors.kds_black
                                    )
                                }
                            }
                        }
                    },
                )
            },
            backgroundColor = colors.backgroundSurfacePrimary
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
                contentAlignment = Alignment.TopCenter
            ) {
                if (isErrored) {
                    PPOScreenErrorState()
                } else if (showEmptyState) {
                    PPOScreenEmptyState(onSeeAllBackedProjectsClick, v2Enabled)
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
                            if (!totalAlerts.isNullOrZero()) {
                                Text(
                                    text = stringResource(id = R.string.Alerts_count).format("count", totalAlerts.toString()),
                                    style = typographyV2.headingXL,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        items(
                            count = ppoCards.itemCount
                        ) { index ->
                            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                            ppoCards[index]?.let { ppoData ->
                                PPOCardView(
                                    viewType = ppoData.viewType() ?: PPOCardViewType.UNKNOWN,
                                    onCardClick = { },
                                    onProjectPledgeSummaryClick = {
                                        val isPledgeManagement = ppoData.viewType == PPOCardViewType.PLEDGE_MANAGEMENT
                                        onProjectPledgeSummaryClick((if (isPledgeManagement) ppoData.webviewUrl else ppoData.backingDetailsUrl) ?: "", isPledgeManagement)
                                    },
                                    projectName = ppoData.projectName(),
                                    pledgeAmount = ppoData.amount?.toDoubleOrNull()?.let { amount ->
                                        RewardViewUtils.formatCurrency(amount, ppoData.currencyCode?.rawValue, ppoData.currencySymbol)
                                    },
                                    imageUrl = ppoData.imageUrl(),
                                    flags = ppoData.flags,
                                    imageContentDescription = ppoData.imageContentDescription(),
                                    creatorName = ppoData.creatorName(),
                                    sendAMessageClickAction = { onSendMessageClick(ppoData.projectSlug() ?: "", ppoData.projectId ?: "", ppoCards.itemSnapshotList.toList(), totalAlerts, ppoData.creatorID() ?: "") },
                                    shippingAddress = ppoData.deliveryAddress()?.getFormattedAddress() ?: "",
                                    onActionButtonClicked = {
                                        when (ppoData.viewType()) {
                                            PPOCardViewType.CONFIRM_ADDRESS -> {
                                                analyticEvents?.trackPPOConfirmAddressInitiateCTAClicked(projectID = ppoData.projectId ?: "", ppoCards.itemSnapshotList.items, totalAlerts)
                                                confirmedAddress = ppoData.deliveryAddress()?.getFormattedAddress() ?: ""
                                                addressID = ppoData.deliveryAddress()?.addressId() ?: ""
                                                backingID = ppoData.backingId ?: ""
                                                projectID = ppoData.projectId ?: ""
                                                openConfirmAddressAlertDialog.value = true
                                            }
                                            else -> {
                                                onPrimaryActionButtonClicked(ppoData)
                                            }
                                        }
                                    },
                                    onSecondaryActionButtonClicked = {
                                        onSecondaryActionButtonClicked(ppoData)
                                    },
                                    rewardReceived = ppoData.backerCompleted().isTrue(),
                                    onRewardReceivedChanged = {
                                        backingID = ppoData.backingId ?: ""
                                        onRewardReceivedChanged.invoke(backingID, it)
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(dimensions.paddingDoubleLarge))
                        }
                    }
                }

                PullRefreshIndicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    refreshing = isLoading,
                    state = pullRefreshState,
                    backgroundColor = colors.backgroundAccentGraySubtle,
                    contentColor = colors.backgroundAccentGreenBold
                )
            }
        }

        when {
            openConfirmAddressAlertDialog.value -> {
                KSAlertDialog(
                    setShowDialog = { openConfirmAddressAlertDialog.value = it },
                    headlineText = stringResource(id = R.string.Confirm_your_address),
                    bodyText = confirmedAddress,
                    leftButtonText = stringResource(id = R.string.Cancel),
                    leftButtonAction = { openConfirmAddressAlertDialog.value = false },
                    rightButtonText = stringResource(id = R.string.Confirm),
                    rightButtonAction = {
                        openConfirmAddressAlertDialog.value = false
                        analyticEvents?.trackPPOConfirmAddressSubmitCTAClicked(ppoCards = ppoCards.itemSnapshotList.items, projectID = projectID, totalCount = totalAlerts)
                        onAddressConfirmed(addressID, backingID)
                    }
                )
            }
        }
    }
}

@Composable
fun PPOScreenEmptyState(
    onSeeAllBackedProjectsClick: () -> Unit,
    v2Enabled: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(
                start = dimensions.paddingMedium,
                end = dimensions.paddingMedium,
                top = dimensions.paddingMedium
            )
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            color = colors.textPrimary,
            text = if (v2Enabled) stringResource(id = R.string.No_funded_backings) else stringResource(id = R.string.Youre_all_caught_up),
            style = typographyV2.headingXL,
        )

        Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

        Text(
            color = colors.textPrimary,
            text = if (v2Enabled) stringResource(id = R.string.When_projects_youve_backed_have_successfully_funded_youll_see_them_here) else stringResource(id = R.string.When_projects_youve_backed_need_your_attention_youll_see_them_here),
            style = typographyV2.body,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

        KSPrimaryGreenButton(
            modifier = Modifier,
            onClickAction = { onSeeAllBackedProjectsClick.invoke() },
            text = stringResource(id = R.string.See_all_backed__projects),
            isEnabled = true
        )
    }
}

@Composable
fun PPOScreenErrorState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(
                start = dimensions.paddingMedium,
                end = dimensions.paddingMedium,
                top = dimensions.paddingMedium
            )
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_alert),
            modifier = Modifier.size(dimensions.iconSizeLarge),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colors.icon)
        )
        Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

        Text(
            color = colors.textPrimary,
            text = (stringResource(id = R.string.Something_went_wrong_pull_to_refresh_no_period)),
            style = typographyV2.body,
            textAlign = TextAlign.Center
        )
    }
}

enum class PledgedProjectsOverviewScreenTestTag {
    BACK_BUTTON,
    INFO_BUTTON,
    BOTTOM_SHEET,
}
