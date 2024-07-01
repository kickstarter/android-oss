package com.kickstarter.features.pledgedprojectsoverview.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.SnackbarHostState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.ui.compose.designsystem.KSTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class
PledgedProjectsOverviewScreenTest : KSRobolectricTestCase() {

    private val backButton =
        composeTestRule.onNodeWithTag(PledgedProjectsOverviewScreenTestTag.BACK_BUTTON.name)
    @Test
    fun testBackButtonClick() {
        var backClickedCount = 0
        composeTestRule.setContent {
            val ppoCardList1 = (0..10).map {
                PPOCardDataMock()
            }
            val ppoCardList = flowOf(PagingData.from(ppoCardList1)).collectAsLazyPagingItems()

            KSTheme {
                PledgedProjectsOverviewScreen(
                    modifier = Modifier,
                    onBackPressed = { backClickedCount++ },
                    lazyColumnListState = rememberLazyListState(),
                    ppoCards = ppoCardList,
                    errorSnackBarHostState = SnackbarHostState(),
                    onSendMessageClick = {},
                    onAddressConfirmed = {},
                    onCardClick = {},
                    onProjectPledgeSummaryClick = {}
                )
            }
        }

        backButton.performClick()
        assertEquals(1, backClickedCount)
    }
}
