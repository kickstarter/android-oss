package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.toolbars.compose.TopToolBar
import kotlinx.coroutines.launch
import java.math.RoundingMode

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ProjectPledgeButtonAndContainerPreview() {
    KSTheme {
        var expanded by remember {
            mutableStateOf(false)
        }
        val pagerState = rememberPagerState(initialPage = 1, pageCount = { 4 })

        val coroutineScope = rememberCoroutineScope()
        ProjectPledgeButtonAndFragmentContainer(
            expanded = expanded,
            onContinueClicked = { expanded = !expanded },
            onBackClicked = {
                if (pagerState.currentPage > 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            page = pagerState.currentPage - 1,
                            animationSpec = tween(
                                durationMillis = 150,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                } else {
                    expanded = !expanded
                }
            },
            pagerState = pagerState,
            onAddOnsContinueClicked = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(
                        page = 2,
                        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                    )
                }
            },
            environment = Environment.builder().build(),
            rewardsList = listOf(),
            addOns = listOf(),
            project = Project.builder().build(),
            onRewardSelected = {},
            onAddOnAddedOrRemoved = {},
            totalAmount = 0.0,
            totalAmountCurrencyConverted = 0.0,
            currentShippingRule = ShippingRule.builder().build(),
            onShippingRuleSelected = {}
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectPledgeButtonAndFragmentContainer(
    expanded: Boolean,
    onContinueClicked: () -> Unit,
    onBackClicked: () -> Unit,
    pagerState: PagerState,
    onAddOnsContinueClicked: () -> Unit,
    shippingRules: List<ShippingRule> = listOf(),
    currentShippingRule: ShippingRule,
    environment: Environment?,
    rewardsList: List<Reward>,
    addOns: List<Reward>,
    project: Project,
    onRewardSelected: (reward: Reward) -> Unit,
    onAddOnAddedOrRemoved: (Map<Reward, Int>) -> Unit,
    totalAmount: Double,
    totalAmountCurrencyConverted: Double,
    selectedReward: Reward? = null,
    onShippingRuleSelected: (ShippingRule) -> Unit
) {
    Column {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = if (expanded) {
                RectangleShape
            } else {
                RoundedCornerShape(
                    topStart = dimensions.radiusLarge,
                    topEnd = dimensions.radiusLarge
                )
            },
            color = colors.backgroundSurfacePrimary,
            elevation = dimensions.elevationLarge,
        ) {
            AnimatedVisibility(
                visible = !expanded,
                enter = expandVertically(
                    animationSpec = tween(
                        durationMillis = 150,
                        easing = FastOutSlowInEasing
                    )
                ),
                exit = shrinkVertically(
                    animationSpec = tween(
                        durationMillis = 150,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.paddingMediumLarge)
                ) {
                    KSPrimaryGreenButton(
                        onClickAction = onContinueClicked,
                        text = stringResource(id = R.string.Back_this_project),
                        isEnabled = true
                    )
                }
            }


            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(
                        durationMillis = 150,
                        easing = FastOutSlowInEasing
                    )
                ),
                exit = shrinkVertically(
                    animationSpec = tween(
                        durationMillis = 150,
                        easing = FastOutSlowInEasing
                    )
                )
            ) {
                Scaffold(
                    topBar = {
                        TopToolBar(
                            title = stringResource(id = R.string.Back_this_project),
                            titleColor = colors.textPrimary,
                            leftOnClickAction = onBackClicked,
                            leftIconColor = colors.textPrimary,
                            backgroundColor = colors.backgroundSurfacePrimary,
                        )
                    }
                ) { padding ->
                    Box(
                        Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        HorizontalPager(
                            userScrollEnabled = false,
                            state = pagerState
                        ) { page ->
                            when (page) {
                                0 -> {
                                    RewardCarouselScreen(
                                        modifier = Modifier,
                                        lazyRowState = rememberLazyListState(),
                                        environment = environment ?: Environment.builder().build(),
                                        rewards = rewardsList,
                                        project = project,
                                        onRewardSelected = onRewardSelected
                                    )
                                }

                                1 -> {
                                    AddOnsScreen(
                                        modifier = Modifier,
                                        environment = environment ?: Environment.builder().build(),
                                        lazyColumnListState = rememberLazyListState(),
                                        countryList = shippingRules,
                                        currentShippingRule = currentShippingRule,
                                        onShippingRuleSelected = onShippingRuleSelected,
                                        rewardItems = addOns,
                                        project = project,
                                        onItemAddedOrRemoved = onAddOnAddedOrRemoved,
                                        onContinueClicked = onAddOnsContinueClicked
                                    )
                                }

                                2 -> {
                                    ConfirmPledgeDetailsScreen(
                                        modifier = Modifier,
                                        ksString = environment?.ksString() ?: Environment.builder()
                                            .build().ksString(),
                                        onContinueClicked = { },
                                        onShippingRuleSelected = onShippingRuleSelected,
                                        totalAmount = environment?.ksCurrency()?.let {
                                            RewardViewUtils.styleCurrency(
                                                totalAmount,
                                                project,
                                                it
                                            ).toString()
                                        } ?: "",
                                        shippingAmount = "",
                                        currentShippingRule = currentShippingRule,
                                        countryList = shippingRules,
                                        totalAmountCurrencyConverted = environment?.ksCurrency()
                                            ?.let {
                                                it.format(
                                                    totalAmountCurrencyConverted,
                                                    project,
                                                    true,
                                                    RoundingMode.HALF_UP,
                                                    true
                                                )
                                            } ?: "",
                                        initialBonusSupport = "$0",
                                        totalBonusSupport = "$0",
                                        deliveryDateString = if (selectedReward?.estimatedDeliveryOn()
                                                .isNotNull()
                                        ) {
                                            DateTimeUtils.estimatedDeliveryOn(
                                                requireNotNull(
                                                    selectedReward?.estimatedDeliveryOn()
                                                )
                                            )
                                        } else ""
                                    )
                                }

                                3 -> {
                                    // Pledge page
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
