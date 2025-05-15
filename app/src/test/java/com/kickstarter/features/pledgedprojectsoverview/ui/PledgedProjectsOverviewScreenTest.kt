package com.kickstarter.features.pledgedprojectsoverview.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.SnackbarHostState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCardFactory
import com.kickstarter.ui.compose.designsystem.KSTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class
PledgedProjectsOverviewScreenTest : KSRobolectricTestCase() {

    private val backButton =
        composeTestRule.onNodeWithTag(PledgedProjectsOverviewScreenTestTag.BACK_BUTTON.name)
    private val infoButton =
        composeTestRule.onNodeWithTag(PledgedProjectsOverviewScreenTestTag.INFO_BUTTON.name)
    private val bottomSheet =
        composeTestRule.onNodeWithTag(PledgedProjectsOverviewScreenTestTag.BOTTOM_SHEET.name)
    @Test
    fun testBackButtonClick() {
        var backClickedCount = 0
        composeTestRule.setContent {
            val ppoCardList = (0..10).map {
                PPOCardFactory.confirmAddressCard()
            }
            val ppoCardPagingList = flowOf(PagingData.from(ppoCardList)).collectAsLazyPagingItems()

            KSTheme {
                PledgedProjectsOverviewScreen(
                    modifier = Modifier,
                    onBackPressed = { backClickedCount++ },
                    lazyColumnListState = rememberLazyListState(),
                    ppoCards = ppoCardPagingList,
                    errorSnackBarHostState = SnackbarHostState(),
                    onSendMessageClick = { projectName, projectID, ppoCards, totalAlerts, creatorID -> },
                    onAddressConfirmed = { backingID, addressID -> },
                    onRewardReceivedChanged = { backingID, checked -> },
                    onProjectPledgeSummaryClick = {},
                    onSeeAllBackedProjectsClick = {},
                    onPrimaryActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {}
                )
            }
        }

        backButton.performClick()
        assertEquals(1, backClickedCount)
    }

    @Test
    fun `when v2 feature flag enabled, info button should appear and open bottom sheet`() {
        composeTestRule.setContent {
            val ppoCardList = (0..10).map {
                PPOCardFactory.confirmAddressCard()
            }
            val ppoCardPagingList = flowOf(PagingData.from(ppoCardList)).collectAsLazyPagingItems()

            KSTheme {
                PledgedProjectsOverviewScreen(
                    modifier = Modifier,
                    onBackPressed = { },
                    lazyColumnListState = rememberLazyListState(),
                    ppoCards = ppoCardPagingList,
                    errorSnackBarHostState = SnackbarHostState(),
                    onSendMessageClick = { projectName, projectID, ppoCards, totalAlerts, creatorID -> },
                    onAddressConfirmed = { backingID, addressID -> },
                    onRewardReceivedChanged = { backingID, checked -> },
                    onProjectPledgeSummaryClick = {},
                    onSeeAllBackedProjectsClick = {},
                    onPrimaryActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    v2Enabled = true
                )
            }
        }

        infoButton.isDisplayed()
        infoButton.performClick()
        bottomSheet.isDisplayed()
        infoButton.performClick()
        bottomSheet.isNotDisplayed()
    }

    @Test
    fun `when v2 feature flag off, should not show info button`() {
        composeTestRule.setContent {
            val ppoCardList = (0..10).map {
                PPOCardFactory.confirmAddressCard()
            }
            val ppoCardPagingList = flowOf(PagingData.from(ppoCardList)).collectAsLazyPagingItems()

            KSTheme {
                PledgedProjectsOverviewScreen(
                    modifier = Modifier,
                    onBackPressed = { },
                    lazyColumnListState = rememberLazyListState(),
                    ppoCards = ppoCardPagingList,
                    errorSnackBarHostState = SnackbarHostState(),
                    onSendMessageClick = { projectName, projectID, ppoCards, totalAlerts, creatorID -> },
                    onAddressConfirmed = { backingID, addressID -> },
                    onRewardReceivedChanged = { backingID, checked -> },
                    onProjectPledgeSummaryClick = {},
                    onSeeAllBackedProjectsClick = {},
                    onPrimaryActionButtonClicked = {},
                    onSecondaryActionButtonClicked = {},
                    v2Enabled = false
                )
            }
        }

        infoButton.assertDoesNotExist()
    }
}
