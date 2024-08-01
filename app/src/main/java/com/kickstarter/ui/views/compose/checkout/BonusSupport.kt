package com.kickstarter.ui.views.compose.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.parseToDouble
import com.kickstarter.ui.compose.designsystem.KSStepper
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.compose.designsystem.shapes

@Preview
@Composable
fun BonusSupportContainerPreview() {
    BonusSupportContainer(
        noAddOnsRw = true,
        initialAmount = 5.0,
        maxAmount = 10.0,
        currencySymbolAtStart = "CAD",
        currencySymbolAtEnd = "$",
        onBonusSupportPlusClicked = {},
        onBonusSupportMinusClicked = {},
        onBonusSupportInputted = {}
    )
}

@Composable
fun BonusSupportContainer(
    noAddOnsRw: Boolean,
    initialAmount: Double,
    maxAmount: Double,
    currencySymbolAtStart: String?,
    currencySymbolAtEnd: String?,
    onBonusSupportPlusClicked: (amount: Double) -> Unit,
    onBonusSupportMinusClicked: (amount: Double) -> Unit,
    onBonusSupportInputted: (amount: Double) -> Unit
) {
    val bonusAmountMaxDigits = integerResource(R.integer.max_length)
    var totalBonusSupport by rememberSaveable { mutableDoubleStateOf(initialAmount) }

    Column {
        Text(
            text = if (noAddOnsRw) stringResource(id = R.string.Your_pledge_amount)
            else stringResource(id = R.string.Bonus_support),
            style = typography.subheadlineMedium,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(dimensions.paddingSmall))

        if (!noAddOnsRw) {
            Text(
                text = stringResource(id = R.string.A_little_extra_to_help),
                style = typography.body2,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(dimensions.paddingSmall))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KSStepper(
                onPlusClicked = {
                    totalBonusSupport++
                    onBonusSupportPlusClicked(totalBonusSupport)
                },
                isPlusEnabled = totalBonusSupport != maxAmount,
                onMinusClicked = {
                    totalBonusSupport--
                    onBonusSupportMinusClicked(totalBonusSupport)
                },
                isMinusEnabled = initialAmount != totalBonusSupport,
                enabledButtonBackgroundColor = colors.kds_white
            )

            Spacer(modifier = Modifier.weight(1f))

            if (!noAddOnsRw) {
                Text(text = "+", style = typography.calloutMedium, color = colors.textSecondary)

                Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = colors.kds_white,
                        shape = shapes.small
                    )
                    .padding(
                        start = dimensions.paddingXSmall,
                        top = dimensions.paddingXSmall,
                        bottom = dimensions.paddingXSmall,
                        end = dimensions.paddingXSmall
                    ),

            ) {
                Text(
                    text = currencySymbolAtStart ?: "",
                    color = colors.textAccentGreen
                )
                BasicTextField(
                    modifier = Modifier.width(IntrinsicSize.Min),
                    value = if (totalBonusSupport % 1.0 == 0.0) totalBonusSupport.toInt().toString() else totalBonusSupport.toString(),
                    onValueChange = {
                        if (it.length <= bonusAmountMaxDigits) totalBonusSupport = it.parseToDouble()
                        onBonusSupportInputted(totalBonusSupport)
                    },
                    textStyle = typography.title1.copy(color = colors.textAccentGreen),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    cursorBrush = SolidColor(colors.iconSubtle)

                )
                Text(
                    text = currencySymbolAtEnd ?: "",
                    color = colors.textAccentGreen
                )
            }
        }

        if (totalBonusSupport > maxAmount) {
            // TODO error message
            // val maxInputString = RewardViewUtils.getMaxInputString(LocalContext.current, selectedReward = , maxPledgeAmount, totalAmount, totalBonusSupport, currencySymbolStartAndEnd, environment)

            Text(
                text = "Enter amount less that $totalBonusSupport",
                textAlign = TextAlign.Right,
                style = typography.footnoteMedium,
                color = colors.textAccentRed,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensions.paddingMedium,
                        end = dimensions.paddingMedium,
                    )
            )
        }
    }
}
