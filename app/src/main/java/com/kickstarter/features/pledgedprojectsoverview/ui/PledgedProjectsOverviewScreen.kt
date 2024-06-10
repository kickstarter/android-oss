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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.activities.compose.login.CreatePasswordScreenTestTag
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.toolbars.compose.TopToolBar

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PledgedProjectsOverviewScreenPreview() {
    KSTheme {
        Scaffold(
            backgroundColor = colors.backgroundAccentGraySubtle
        ) { padding ->
            PledgedProjectsOverviewScreen(
                modifier = Modifier.padding(padding),
                lazyColumnListState = rememberLazyListState(),
                ppoCards = (0..10).map {
                    PPOCardDataMock()
                },
                alertsAmount = 10,
                onBackPressed = {}
            )
        }
    }
}

@Composable
fun PledgedProjectsOverviewScreen(
    modifier: Modifier,
    alertsAmount : Int = 0,
    onBackPressed: () -> Unit,
    lazyColumnListState: LazyListState,
    ppoCards : List<PPOCardDataMock> = listOf(),
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopToolBar(
                    title = stringResource(id = R.string.project_alerts_fpo),
                    titleColor = colors.kds_support_700,
                    leftOnClickAction = onBackPressed,
                    leftIconColor = colors.kds_support_700,
                    leftIconModifier = Modifier.testTag(PledgedProjectsOverviewScreenTestTag.BACK_BUTTON.name),
                    backgroundColor = colors.kds_white,
                )
            },
            backgroundColor = colors.kds_white
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
                        text = stringResource(id = R.string.alerts_fpo, alertsAmount),
                        style = typography.title3Bold,
                        color = colors.textPrimary
                    )
                }

                items(
                    items = ppoCards
                ) { ppoCard ->
                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                    PPOCardView(
                        viewType = ppoCard.viewType,
                        onCardClick = { },
                        projectName = ppoCard.projectName,
                        pledgeAmount = ppoCard.pledgeAmount,
                        imageUrl = ppoCard.imageUrl,
                        imageContentDescription = ppoCard.imageContentDescription,
                        creatorName = ppoCard.creatorName,
                        sendAMessageClickAction = { },
                        shippingAddress = ppoCard.shippingAddress,
                        showBadge = ppoCard.showBadge,
                        onActionButtonClicked = { },
                        onSecondaryActionButtonClicked = { },
                        timeNumberForAction = ppoCard.timeNumberForAction
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(dimensions.paddingDoubleLarge))
                }
            }
        }
    }
}

enum class PledgedProjectsOverviewScreenTestTag {
    BACK_BUTTON,
}

// For preview purposes only, will remove once we have the PPO Card payload model from graph
data class PPOCardDataMock(
    val viewType: PPOCardViewType = PPOCardViewType.FIX_PAYMENT,
    val onCardClick: () -> Unit = { },
    val projectName : String = "This is a project name",
    val pledgeAmount : String = "$14.00",
    val imageUrl : String = "",
    val imageContentDescription : String = "",
    val creatorName : String = "Creator Name",
    val sendAMessageClickAction: () -> Unit = { },
    val shippingAddress : String = "",
    val showBadge : Boolean = true,
    val onActionButtonClicked: () -> Unit = {},
    val onSecondaryActionButtonClicked: () -> Unit = {},
    val timeNumberForAction : Int = 25
)