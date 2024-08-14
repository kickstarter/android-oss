package com.kickstarter.ui.views.compose.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.KSString
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

@Preview
@Composable
fun ItemizedRewardListContainerPreview() {
    ItemizedRewardListContainer(
        ksString = null,
        rewardsList = emptyList<Pair<String, String>>(),
        shippingAmount = 20.0,
        shippingAmountString = "",
        initialShippingLocation = "",
        totalAmount = "50$",
        totalAmountCurrencyConverted = "",
        initialBonusSupport = "0",
        totalBonusSupport = "0",
        deliveryDateString = "",
        rewardsHaveShippables = false
    )
}

@Composable
fun ItemizedRewardListContainer(
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
    rewardsHaveShippables: Boolean = false
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

        if (shippingAmount > 0 && initialShippingLocation.isNotEmpty() && rewardsHaveShippables) {
            Spacer(modifier = Modifier.height(dimensions.paddingMedium))

            Row {
                Text(
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
                        text = totalAmountCurrencyConverted,
                        style = typography.footnote,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}