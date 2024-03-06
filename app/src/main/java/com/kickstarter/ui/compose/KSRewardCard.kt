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
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                "$20",
                "about $400",
                "Deck of cards",
                backerCountBadgeText = "23 backers",
                description = "this is a description",
                isEnabled = true,
                estimatedDelivery = "June 10th, 2026",
                includes = listOf("1 Comic Book", "2 pins", "3 happy meals"),
                yourSelectionIsVisible = true,
                scaffoldState = rememberScaffoldState(),

        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KSRewardCard(
        amount: String,
        conversion: String? = null,
        title: String? = null,
        backerCountBadgeText: String? = null,
        description: String? = null,
        includes: List<String>? = null,
        estimatedDelivery: String? = null,
        isEnabled : Boolean,
        yourSelectionIsVisible: Boolean = false,
        localPickup: String? = null,
        scaffoldState: ScaffoldState) {

    Card(
            modifier = Modifier
                    .fillMaxWidth(),
            shape = RoundedCornerShape(dimensions.radiusMediumSmall),
    ) {
        Column(
                modifier = Modifier.background(KSTheme.colors.kds_white)
        ) {
            if (yourSelectionIsVisible) {
                Box(
                        modifier = Modifier
                                .background(
                                        color = KSTheme.colors.kds_trust_500,
                                        shape = RoundedCornerShape(topStart = dimensions.radiusMediumSmall,
                                                bottomEnd = dimensions.radiusMediumSmall)
                                )
                                .padding(
                                        top = dimensions.paddingSmall,
                                        bottom = dimensions.paddingSmall,
                                        start = dimensions.paddingMediumLarge,
                                        end = dimensions.paddingMediumLarge
                                ),
                ) {
                    Text(
                            text = "Your Selection",
                            style = KSTheme.typography.subheadline,
                            color = KSTheme.colors.kds_white,
                    )
                }

            }

            Column(Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(dimensions.paddingMediumLarge)) {

                Text(text = amount, style = KSTheme.typography.titleRewardMedium, color = KSTheme.colors.textAccentGreenBold)

                if (!conversion.isNullOrEmpty()) {
                    Text(text = conversion, style = KSTheme.typography.footnote, color = KSTheme.colors.textAccentGreenBold)
                }

                Spacer(modifier = Modifier.height(dimensions.paddingMediumSmall))

                if (!title.isNullOrEmpty()) {
                    Text(
                            text = title,
                            style = KSTheme.typography.titleRewardBold,
                            color = KSTheme.colors.kds_black
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
                            text = "Description",
                            color = KSTheme.colors.kds_support_400,
                            style = KSTheme.typography.calloutMedium
                    )

                    Text(
                            modifier = Modifier.padding(top = dimensions.textInputTopPadding),
                            text = description,
                            color = KSTheme.colors.kds_support_700,
                            style = KSTheme.typography.body2
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

                }

                if (!includes.isNullOrEmpty()) {
                    Text(
                            text = "Includes",
                            color = colors.kds_support_400,
                            style = typography.calloutMedium)

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

                if (!estimatedDelivery.isNullOrEmpty()) {
                    Text(
                            text = "Estimated Delivery",
                            color = colors.kds_support_400,
                            style = typography.calloutMedium)

                    Text(
                            modifier = Modifier.padding(top = dimensions.radiusSmall),
                            text = estimatedDelivery,
                            color = KSTheme.colors.kds_support_700,
                            style = KSTheme.typography.body2
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

                }

                if (!localPickup.isNullOrEmpty()) {
                    Text(
                            text = "Reward Location",
                            color = colors.kds_support_400,
                            style = typography.calloutMedium)

                    Text(
                            modifier = Modifier.padding(top = dimensions.radiusSmall),
                            text = "Oakland, CA Plus a super long description here because we need to know how it is gonna behave",
                            color = KSTheme.colors.kds_support_700,
                            style = KSTheme.typography.body2
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))
                }

                FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {

                    KSGreenBadge(
                            text = "3 days left"
                    )

                    KSGreenBadge(
                            text = "30 left"
                    )

                    KSGreenBadge(
                            text = "Anywhere in the world"
                    )

                    KSGreenBadge(
                            text = "Add-ons"
                    )

                    Spacer(modifier = Modifier.height(dimensions.paddingMediumLarge))

                }

            }

            KSPrimaryGreenButton(
                    modifier = Modifier.padding(bottom = dimensions.paddingMediumLarge, start = dimensions.paddingMediumLarge, end = dimensions.paddingMediumLarge),
                    onClickAction = { },
                    isEnabled = isEnabled,
                    text = if (isEnabled) "Select" else "Unavailable",
            )
        }
    }
}