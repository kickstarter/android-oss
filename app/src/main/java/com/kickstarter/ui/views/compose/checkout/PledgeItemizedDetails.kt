package com.kickstarter.ui.views.compose.checkout

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ItemizedContainerForNoRewardPreview() {
    KSTheme {
        Scaffold(
            backgroundColor = KSTheme.colors.backgroundAccentGraySubtle
        ) { padding ->
            ItemizedRewardListContainer(
                modifier = Modifier.padding(paddingValues = padding),
                totalAmount = "US$ 1",
                totalAmountCurrencyConverted = "About CA$ 1.38",
                initialBonusSupport = "US$ 0",
                totalBonusSupport = "US$ 1",
                shippingAmount = -1.0,
                plotSelected = false
            )
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ItemizedRewardListContainerPreview() {
    KSTheme {
        Scaffold(
            backgroundColor = KSTheme.colors.backgroundAccentGraySubtle
        ) { padding ->
            ItemizedRewardListContainer(
                modifier = Modifier
                    .padding(paddingValues = padding),
                ksString = null,
                rewardsList = emptyList<Pair<String, String>>(),
                shippingAmount = 20.0,
                shippingAmountString = "",
                initialShippingLocation = "",
                totalAmount = "50$",
                totalAmountCurrencyConverted = "About CA\$ 1.38",
                initialBonusSupport = "0",
                totalBonusSupport = "0",
                deliveryDateString = "",
                rewardsHaveShippables = false,
                plotSelected = false,
                disclaimerText = stringResource(id = R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline_and_receive_proof_of_pledge)
            )
        }
    }
}

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ItemizedContainerForNoRewardPlotSelectedPreview() {
    KSTheme {
        Scaffold(
            backgroundColor = KSTheme.colors.backgroundAccentGraySubtle
        ) { padding ->
            ItemizedRewardListContainer(
                modifier = Modifier.padding(paddingValues = padding),
                totalAmount = "US$ 1",
                totalAmountCurrencyConverted = "About CA$ 1.38",
                initialBonusSupport = "US$ 0",
                totalBonusSupport = "US$ 1",
                shippingAmount = -1.0,
                plotSelected = true
            )
        }
    }
}

enum class PledgeItemizedDetailsTestTag {
    DELIVERY_DATE,
    PAGE_TITLE,
    ITEM_NAME,
    ITEM_COST,
    PLEDGE_AMOUNT_TITLE,
    TOTAL_AMOUNT,
    BONUS_SUPPORT_TITLE,
    DISCLAIMER_TEXT,
    BONUS_SUPPORT,
    SHIPPING_TITLE,
    SHIPPING_AMOUNT,
    CURRENCY_CONVERSION,
    PLOT_SELECTED_BADGE,
}

@Composable
fun ItemizedRewardListContainer(
    modifier: Modifier = Modifier,
    ksString: KSString? = null,
    rewardsList: List<Pair<String, String>> = listOf(),
    shippingAmount: Double,
    shippingAmountString: String = "",
    initialShippingLocation: String = "",
    totalAmount: String,
    totalAmountCurrencyConverted: String = "",
    initialBonusSupport: String,
    totalBonusSupport: String,
    deliveryDateString: String = "",
    rewardsHaveShippables: Boolean = false,
    disclaimerText: String = "",
    plotSelected: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.backgroundSurfacePrimary)
            .padding(
                start = dimensions.paddingMedium,
                end = dimensions.paddingMedium,
                bottom = dimensions.paddingLarge,
                top = dimensions.paddingMediumLarge
            )
    ) {
        Text(
            modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.PAGE_TITLE.name),
            text = stringResource(id = R.string.Your_pledge),
            style = typography.headline,
            color = colors.textPrimary
        )

        if (deliveryDateString.isNotEmpty()) {
            Spacer(modifier = Modifier.height(dimensions.paddingXSmall))

            Text(
                modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.DELIVERY_DATE.name),
                text = deliveryDateString,
                style = typography.caption1,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        KSDividerLineGrey()

        rewardsList.forEach {
            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            Row {
                Text(
                    modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.ITEM_NAME.name),
                    text = it.first,
                    style = typography.subheadlineMedium,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.ITEM_COST.name),
                    text = it.second,
                    style = typography.subheadlineMedium,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            KSDividerLineGrey()
        }

        if (shippingAmount > 0 && initialShippingLocation.isNotEmpty() && rewardsHaveShippables) {
            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            Row {
                Text(
                    modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.SHIPPING_TITLE.name),
                    text = ksString?.format(
                        stringResource(id = R.string.Shipping_to_country),
                        "country",
                        initialShippingLocation
                    ) ?: "Shipping: $initialShippingLocation",
                    style = typography.subheadlineMedium,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.SHIPPING_AMOUNT.name),
                    text = shippingAmountString,
                    style = typography.subheadlineMedium,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            KSDividerLineGrey()
        }

        if (totalBonusSupport != initialBonusSupport) {
            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            Row {
                Text(
                    modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.BONUS_SUPPORT_TITLE.name),
                    text = stringResource(
                        id =
                        if (rewardsList.isNotEmpty()) R.string.Bonus_support
                        else R.string.Pledge_without_a_reward
                    ),
                    style = typography.subheadlineMedium,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.BONUS_SUPPORT.name),
                    text = totalBonusSupport,
                    style = typography.subheadlineMedium,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            KSDividerLineGrey()
        }

        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        Row {
            Text(
                modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.PLEDGE_AMOUNT_TITLE.name),
                text = stringResource(id = R.string.Pledge_amount),
                style = typography.calloutMedium,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.TOTAL_AMOUNT.name),
                    text = totalAmount,
                    style = typography.subheadlineMedium,
                    color = colors.textPrimary
                )

                if (totalAmountCurrencyConverted.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(dimensions.paddingXSmall))

                    Text(
                        modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.CURRENCY_CONVERSION.name),
                        text = totalAmountCurrencyConverted,
                        style = typography.footnote,
                        color = colors.textPrimary
                    )
                }
            }
        }

        if (plotSelected) {
            Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Box(
                    modifier = modifier
                        .background(
                            color = colors.kds_create_700.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(dimensions.radiusSmall)
                        )
                        .padding(
                            start = dimensions.paddingSmall,
                            end = dimensions.paddingSmall,
                            top = dimensions.paddingXSmall,
                            bottom = dimensions.paddingXSmall,
                        )
                ) {
                    Text(
                        modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.PLOT_SELECTED_BADGE.name),
                        text = stringResource(
                            id = R.string.fpo_pledge_over_time
                        ),
                        style = typography.body2Medium,
                        color = colors.kds_create_700
                    )
                }
                Text(
                    modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.DISCLAIMER_TEXT.name),
                    text = stringResource(id = R.string.fpo_charged_as_4_payments),
                    style = typography.footnote,
                    color = colors.textPrimary
                )
            }
        }

        if (disclaimerText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(dimensions.paddingMedium))
            Row {
                Text(
                    modifier = Modifier.testTag(PledgeItemizedDetailsTestTag.DISCLAIMER_TEXT.name),
                    text = disclaimerText,
                    style = typography.footnote,
                    color = colors.textPrimary
                )
            }
        }
    }
}
