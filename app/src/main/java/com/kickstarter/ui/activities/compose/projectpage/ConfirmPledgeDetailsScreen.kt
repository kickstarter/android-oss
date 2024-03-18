package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSStepper
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.compose.designsystem.shapes

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ConfirmPledgeDetailsScreenPreviewNoRewards() {
    KSTheme {
        ConfirmPledgeDetailsScreen(
            modifier = Modifier,
            onContinueClicked = {},
            initialShippingLocation = "United States",
            totalAmount = "$1",
            totalAmountCurrencyConverted = "About $1",
            initialBonusSupport = "$1",
            totalBonusSupport = "$1",
            onShippingRuleSelected = {}
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ConfirmPledgeDetailsScreenPreviewNoAddOnsOrBonusSupport() {
    KSTheme {
        ConfirmPledgeDetailsScreen(
            modifier = Modifier,
            onContinueClicked = {},
            rewardsList = (1..2).map {
                Pair("Cool Item $it", "$20")
            },
            shippingAmount = "$5",
            initialShippingLocation = "United States",
            totalAmount = "$55",
            totalAmountCurrencyConverted = "About $",
            initialBonusSupport = "$0",
            totalBonusSupport = "$0",
            countryList = listOf(ShippingRule.builder().build()),
            onShippingRuleSelected = {},
            deliveryDateString = stringResource(id = R.string.Estimated_delivery) + " May 2024"
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ConfirmPledgeDetailsScreenPreviewAddOnsOnly() {
    KSTheme {
        ConfirmPledgeDetailsScreen(
            modifier = Modifier,
            onContinueClicked = {},
            rewardsList = (1..5).map {
                Pair("Cool Item $it", "$20")
            },
            shippingAmount = "$5",
            initialShippingLocation = "United States",
            totalAmount = "$105",
            totalAmountCurrencyConverted = "About $",
            initialBonusSupport = "$0",
            totalBonusSupport = "$0",
            onShippingRuleSelected = {},
            deliveryDateString = stringResource(id = R.string.Estimated_delivery) + " May 2024"
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ConfirmPledgeDetailsScreenPreviewBonusSupportOnly() {
    KSTheme {
        ConfirmPledgeDetailsScreen(
            modifier = Modifier,
            onContinueClicked = {},
            rewardsList = (1..2).map {
                Pair("Cool Item $it", "$20")
            },
            shippingAmount = "$5",
            initialShippingLocation = "United States",
            totalAmount = "$55",
            totalAmountCurrencyConverted = "About $",
            initialBonusSupport = "$0",
            totalBonusSupport = "$10",
            countryList = listOf(ShippingRule.builder().build()),
            onShippingRuleSelected = {},
            deliveryDateString = stringResource(id = R.string.Estimated_delivery) + " May 2024"
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ConfirmPledgeDetailsScreenPreviewAddOnsAndBonusSupport() {
    KSTheme {
        ConfirmPledgeDetailsScreen(
            modifier = Modifier,
            onContinueClicked = {},
            rewardsList = (1..5).map {
                Pair("Cool Item $it", "$20")
            },
            shippingAmount = "$5",
            initialShippingLocation = "United States",
            totalAmount = "$115",
            totalAmountCurrencyConverted = "About $",
            initialBonusSupport = "$0",
            totalBonusSupport = "$10",
            onShippingRuleSelected = {},
            deliveryDateString = stringResource(id = R.string.Estimated_delivery) + " May 2024"
        )
    }
}

@Composable
fun ConfirmPledgeDetailsScreen(
    modifier: Modifier,
    ksString: KSString? = null,
    onContinueClicked: () -> Unit,
    rewardsList: List<Pair<String, String>> = listOf(),
    shippingAmount: String = "",
    initialShippingLocation: String? = null,
    countryList: List<ShippingRule> = listOf(),
    onShippingRuleSelected: (ShippingRule) -> Unit,
    totalAmount: String,
    totalAmountCurrencyConverted: String = "",
    initialBonusSupport: String,
    totalBonusSupport: String,
    deliveryDateString: String = ""
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Column {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(
                        topStart = dimensions.radiusLarge,
                        topEnd = dimensions.radiusLarge
                    ),
                    color = colors.backgroundSurfacePrimary,
                    elevation = dimensions.elevationLarge,
                ) {
                    Box(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensions.paddingMediumLarge)
                    ) {
                        Column {
                            if (rewardsList.isNotEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = stringResource(id = R.string.Total_amount),
                                        style = typography.subheadlineMedium,
                                        color = colors.textPrimary
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Text(
                                        text = totalAmount,
                                        style = typography.subheadlineMedium,
                                        color = colors.textPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(dimensions.paddingSmall))
                            }
                            KSPrimaryGreenButton(
                                onClickAction = onContinueClicked,
                                text = stringResource(id = R.string.Continue),
                                isEnabled = true
                            )
                        }
                    }
                }
            }
        },
        backgroundColor = colors.backgroundAccentGraySubtle
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
        ) {

            item {
                Text(
                    modifier = Modifier.padding(
                        start = dimensions.paddingMedium,
                        top = dimensions.paddingMedium
                    ),
                    text = "Confirm your pledge details.",
                    style = typography.title3Bold,
                    color = colors.textPrimary
                )
            }

            if (rewardsList.isNotEmpty() && shippingAmount.isNotEmpty() && !initialShippingLocation.isNullOrEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(
                            start = dimensions.paddingMedium,
                            end = dimensions.paddingMedium,
                            top = dimensions.paddingMedium
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.Your_shipping_location),
                            style = typography.subheadlineMedium,
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (countryList.isNotEmpty()) {
                                CountryInputWithDropdown(
                                    interactionSource = interactionSource,
                                    countryList = countryList,
                                    onShippingRuleSelected = onShippingRuleSelected
                                )
                            } else {
                                Text(
                                    text = initialShippingLocation,
                                    style = typography.subheadline,
                                    color = colors.textPrimary
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = "+ $shippingAmount",
                                style = typography.title3,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            item {
                BonusSupportContainer(
                    isForNoRewardPledge = rewardsList.isEmpty(),
                    initialValue = initialBonusSupport,
                    totalBonusAmount = totalBonusSupport,
                    onBonusSupportPlusClicked = {},
                    onBonusSupportMinusClicked = {}
                )
            }

            if (rewardsList.isEmpty()) {
                item {
                    Column(modifier = Modifier.padding(all = dimensions.paddingMedium)) {
                        KSDividerLineGrey()

                        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                        Row {
                            Text(
                                text = stringResource(id = R.string.Total),
                                style = typography.headline,
                                color = colors.textPrimary
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = totalAmount,
                                    style = typography.headline,
                                    color = colors.textPrimary
                                )

                                if (totalAmountCurrencyConverted.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(dimensions.paddingXSmall))

                                    Text(
                                        text = ksString?.format(
                                            stringResource(id = R.string.About_reward_amount),
                                            "reward_amount",
                                            totalAmount
                                        ) ?: "About $totalAmount",
                                        style = typography.footnote,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    ItemizedRewardListContainer(
                    ksString = ksString,
                    rewardsList = rewardsList,
                    shippingAmount = shippingAmount,
                    initialShippingLocation = initialShippingLocation,
                    totalAmount = totalAmount,
                    totalAmountCurrencyConverted = totalAmountCurrencyConverted,
                    initialBonusSupport = initialBonusSupport,
                    totalBonusSupport = totalBonusSupport,
                    deliveryDateString = deliveryDateString
                    )
                }
            }

        }
    }
}

@Composable
fun BonusSupportContainer(
    isForNoRewardPledge: Boolean,
    initialValue: String,
    totalBonusAmount: String,
    onBonusSupportPlusClicked: () -> Unit,
    onBonusSupportMinusClicked: () -> Unit
) {
    Column(
        modifier = Modifier.padding(all = dimensions.paddingMedium)
    ) {
        Text(
            text =
            if (isForNoRewardPledge) stringResource(id = R.string.Your_pledge_amount)
            else stringResource(id = R.string.Bonus_support),
            style = typography.subheadlineMedium,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(dimensions.paddingSmall))

        if (!isForNoRewardPledge) {
            Text(
                text = stringResource(id = R.string.A_little_extra_to_help),
                style = typography.body2,
                color = colors.textSecondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KSStepper(
                onPlusClicked = onBonusSupportPlusClicked,
                isPlusEnabled = true,
                onMinusClicked = onBonusSupportMinusClicked,
                isMinusEnabled = initialValue != totalBonusAmount,
                enabledButtonBackgroundColor = colors.kds_white
            )

            Spacer(modifier = Modifier.weight(1f))

            if (!isForNoRewardPledge) {
                Text(text = "+", style = typography.calloutMedium, color = colors.textSecondary)

                Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))
            }

            Text(
                modifier = Modifier
                        .background(
                                color = colors.kds_white,
                                shape = shapes.small
                        )
                        .padding(
                                start = dimensions.paddingMediumSmall,
                                top = dimensions.paddingMediumSmall,
                                bottom = dimensions.paddingMediumSmall,
                                end = dimensions.paddingMediumSmall
                        ),
                text = totalBonusAmount,
                style = typography.headline,
                color = colors.textAccentGreen
            )
        }
    }
}

@Composable
fun ItemizedRewardListContainer(
        ksString: KSString? = null,
        rewardsList: List<Pair<String, String>> = listOf(),
        shippingAmount: String = "",
        initialShippingLocation: String? = null,
        totalAmount: String,
        totalAmountCurrencyConverted: String = "",
        initialBonusSupport: String,
        totalBonusSupport: String,
        deliveryDateString: String = ""

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
                        text = stringResource(id = R.string.Your_pledge),
                        style = typography.headline,
                        color = colors.textPrimary
                )

                if (deliveryDateString.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(dimensions.paddingXSmall))

                    Text(
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
                                text = it.first,
                                style = typography.subheadlineMedium,
                                color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                                text = it.second,
                                style = typography.subheadlineMedium,
                                color = colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                    KSDividerLineGrey()
                }

                if (shippingAmount.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                    Row {
                        Text(
                                text = ksString?.format(
                                        stringResource(id = R.string.Shipping_to_country),
                                        "country",
                                        totalAmount
                                ) ?: "Shipping: $initialShippingLocation",
                                style = typography.subheadlineMedium,
                                color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                                text = shippingAmount,
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
                                text = stringResource(id = R.string.Bonus_support),
                                style = typography.subheadlineMedium,
                                color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
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
                            text = stringResource(id = R.string.Total_amount),
                            style = typography.calloutMedium,
                            color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                                text = totalAmount,
                                style = typography.subheadlineMedium,
                                color = colors.textPrimary
                        )

                        if (totalAmountCurrencyConverted.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(dimensions.paddingXSmall))

                            Text(
                                    text = ksString?.format(
                                            stringResource(id = R.string.About_reward_amount),
                                            "reward_amount",
                                            totalAmount
                                    ) ?: "About $totalAmount",
                                    style = typography.footnote,
                                    color = colors.textPrimary
                            )
                        }
                    }
                }
            }
        }