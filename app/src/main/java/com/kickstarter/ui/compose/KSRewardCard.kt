package com.kickstarter.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSGreenBadge
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSRewardCardPreview() {
    KSTheme {
        KSRewardCard(
            amount = "$20",
            conversion = "about $400",
            title = "Deck of cards",
            backerCountBadgeText = "23 backers",
            description = "this is a description",
            isCTAButtonEnabled = true,
            estimatedDelivery = "June 10th, 2026",
            includes = listOf("1 Comic Book", "2 pins", "3 happy meals"),
            yourSelectionIsVisible = true,
            ctaButtonText = "Select",
            expirationDateText = "4 Days",
            shippingSummaryText = "Anywhere",
            addonsPillVisible = true,
            remainingText = "5 left",
            estimatedShippingCost = "About $10-$15",
            onRewardSelectClicked = { }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KSRewardCard(
    amount: String? = null,
    conversion: String? = null,
    title: String? = null,
    backerCountBadgeText: String? = null,
    description: String? = null,
    includes: List<String> = emptyList(),
    estimatedDelivery: String? = null,
    yourSelectionIsVisible: Boolean = false,
    isCTAButtonEnabled: Boolean,
    isCTAButtonVisible: Boolean = true,
    ctaButtonText: String,
    expirationDateText: String? = null,
    localPickup: String? = null,
    shippingSummaryText: String? = null,
    addonsPillVisible: Boolean = false,
    remainingText: String? = null,
    estimatedShippingCost: String? = null,
    onRewardSelectClicked: () -> Unit
) {

    Card(
        modifier = Modifier
            .width(294.dp),
        shape = RoundedCornerShape(dimensions.radiusMediumSmall),
    ) {
        Column(
            modifier = Modifier.background(colors.kds_white)
        ) {
            if (yourSelectionIsVisible) {
                Box(
                    modifier = Modifier
                        .background(
                            color = colors.kds_trust_500,
                            shape = RoundedCornerShape(
                                topStart = dimensions.radiusMediumSmall,
                                bottomEnd = dimensions.radiusMediumSmall
                            )
                        )
                        .padding(
                            top = dimensions.paddingSmall,
                            bottom = dimensions.paddingSmall,
                            start = dimensions.paddingMediumLarge,
                            end = dimensions.paddingMediumLarge
                        ),
                ) {
                    Text(
                        text = stringResource(id = R.string.Your_selection),
                        style = typography.subheadline,
                        color = colors.kds_white,
                    )
                }
            }

            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(dimensions.paddingMediumLarge)
                    .weight(weight = 1f, fill = false)
            ) {

                if (!amount.isNullOrEmpty()) {
                    Text(text = amount, style = typography.titleRewardMedium, color = colors.textAccentGreenBold)
                }

                if (!conversion.isNullOrEmpty()) {
                    Text(text = conversion, style = typography.footnote, color = colors.textAccentGreenBold)
                }

                Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

                if (!title.isNullOrEmpty()) {
                    Text(
                        text = title,
                        style = typography.titleRewardBold,
                        color = colors.kds_black
                    )
                    Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))
                }

                if (!backerCountBadgeText.isNullOrEmpty()) {
                    KSGreenBadge(
                        text = backerCountBadgeText
                    )
                    Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))
                }

                if (!description.isNullOrEmpty()) {
                    Text(
                        text = stringResource(id = R.string.Description),
                        color = colors.kds_support_400,
                        style = typography.calloutMedium
                    )

                    Text(
                        modifier = Modifier.padding(top = dimensions.textInputTopPadding),
                        text = description,
                        color = colors.kds_support_700,
                        style = typography.body2
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))
                }

                if (includes.isNotEmpty()) {
                    Text(
                        text = stringResource(id = R.string.rewards_info_includes),
                        color = colors.kds_support_400,
                        style = typography.calloutMedium
                    )

                    includes.forEachIndexed { index, itemDescription ->
                        Row(modifier = Modifier.padding(top = dimensions.radiusSmall, bottom = dimensions.radiusSmall), verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))

                            Box(
                                modifier = Modifier
                                    .padding(end = dimensions.paddingSmall)
                                    .size(dimensions.paddingXSmall)
                                    .background(color = colors.kds_support_400, shape = CircleShape),
                            )

                            Text(
                                text = itemDescription,
                                style = typography.body2,
                                color = colors.textPrimary
                            )

                            Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))
                }

                if (!estimatedShippingCost.isNullOrEmpty()) {
                    Text(
                        text = stringResource(id = R.string.Estimated_Shipping),
                        color = colors.kds_support_400,
                        style = typography.calloutMedium
                    )

                    Text(
                        modifier = Modifier.padding(top = dimensions.radiusSmall),
                        text = estimatedShippingCost,
                        color = colors.kds_support_700,
                        style = typography.body2
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))
                }

                if (!estimatedDelivery.isNullOrEmpty()) {
                    Text(
                        text = stringResource(id = R.string.Estimated_delivery),
                        color = colors.kds_support_400,
                        style = typography.calloutMedium
                    )

                    Text(
                        modifier = Modifier.padding(top = dimensions.radiusSmall),
                        text = estimatedDelivery,
                        color = colors.kds_support_700,
                        style = typography.body2
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))
                }

                if (!localPickup.isNullOrEmpty()) {
                    Text(
                        text = stringResource(id = R.string.Reward_location),
                        color = colors.kds_support_400,
                        style = typography.calloutMedium
                    )

                    Text(
                        modifier = Modifier.padding(top = dimensions.radiusSmall),
                        text = localPickup,
                        color = colors.kds_support_700,
                        style = typography.body2
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {

                    if (!expirationDateText.isNullOrEmpty()) {
                        KSGreenBadge(
                            text = expirationDateText
                        )
                    }

                    if (!remainingText.isNullOrEmpty()) {
                        KSGreenBadge(
                            text = remainingText
                        )
                    }

                    if (!shippingSummaryText.isNullOrEmpty()) {
                        KSGreenBadge(
                            text = shippingSummaryText
                        )
                    }

                    if (addonsPillVisible) {
                        KSGreenBadge(
                            text = stringResource(id = R.string.Add_ons)
                        )

                        Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))
                    }
                }
            }

            if (isCTAButtonVisible) {
                KSPrimaryGreenButton(
                    modifier = Modifier
                        .padding(
                            bottom = dimensions.paddingMediumLarge,
                            start = dimensions.paddingMediumLarge,
                            end = dimensions.paddingMediumLarge
                        )
                        .fillMaxWidth(),
                    onClickAction = onRewardSelectClicked,
                    isEnabled = isCTAButtonEnabled,
                    text = ctaButtonText
                )
            }
        }
    }
}
