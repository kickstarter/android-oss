package com.kickstarter.ui.views.compose.checkout

import android.content.res.Configuration
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
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.parseToDouble
import com.kickstarter.models.Reward
import com.kickstarter.ui.compose.designsystem.KSStepper
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.compose.designsystem.shapes

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BonusSupportContainerPreview() {
    KSTheme {
        Scaffold(
            backgroundColor = KSTheme.colors.backgroundAccentGraySubtle
        ) { padding ->
            BonusSupportContainer(
                modifier = Modifier
                    .padding(paddingValues = padding),
                selectedReward = Reward.builder().build(),
                initialAmount = 5.0,
                maxAmount = 10.0,
                minPledge = 5.0,
                currencySymbolAtStart = "CAD",
                currencySymbolAtEnd = "$",
                totalAmount = 100.0,
                totalBonusSupport = 5.0,
                onBonusSupportPlusClicked = {},
                onBonusSupportMinusClicked = {},
                onBonusSupportInputted = {},
                environment = Environment.builder().build()
            )
        }
    }
}

@Composable
fun BonusSupportContainer(
    modifier: Modifier = Modifier,
    selectedReward: Reward,
    initialAmount: Double,
    maxAmount: Double,
    minPledge: Double,
    currencySymbolAtStart: String?,
    currencySymbolAtEnd: String?,
    totalAmount: Double,
    totalBonusSupport: Double,
    onBonusSupportPlusClicked: (amount: Double) -> Unit,
    onBonusSupportMinusClicked: (amount: Double) -> Unit,
    onBonusSupportInputted: (amount: Double) -> Unit,
    environment: Environment
) {
    val bonusAmountMaxDigits = integerResource(R.integer.max_length)
    val isNoReward = RewardUtils.isNoReward(selectedReward)
    val displayedTotalBonusAmount =
        if (isNoReward) totalBonusSupport + minPledge else totalBonusSupport

    Column {
        if (isNoReward) {
            Text(
                text = stringResource(id = R.string.Customize_your_reward),
                style = typography.title3Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(dimensions.paddingMedium))
        }

        Text(
            text = if (isNoReward) stringResource(id = R.string.Your_pledge_amount)
            else stringResource(id = R.string.Bonus_support),
            style = typography.subheadlineMedium,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(dimensions.paddingSmall))

        if (!isNoReward && !selectedReward.hasAddons()) {
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
                    onBonusSupportPlusClicked(totalBonusSupport + minPledge)
                },
                isPlusEnabled = displayedTotalBonusAmount < maxAmount,
                onMinusClicked = {
                    onBonusSupportMinusClicked(totalBonusSupport - minPledge)
                },
                isMinusEnabled = initialAmount < displayedTotalBonusAmount,
                enabledButtonBackgroundColor = colors.kds_white
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(text = "+", style = typography.calloutMedium, color = colors.textSecondary)

            Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))

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
                    value =
                    if (displayedTotalBonusAmount % 1.0 == 0.0) displayedTotalBonusAmount.toInt().toString()
                    else displayedTotalBonusAmount.toString(),
                    onValueChange = {
                        if (it.length <= bonusAmountMaxDigits) onBonusSupportInputted(it.parseToDouble())
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

        if (totalAmount > maxAmount) {
            val maxInputString = RewardViewUtils.getMaxInputString(
                context = LocalContext.current,
                selectedReward = selectedReward,
                maxPledgeAmount = maxAmount,
                totalAmount = totalAmount,
                totalBonusSupport = totalBonusSupport,
                currencySymbolStartAndEnd = Pair(currencySymbolAtStart, currencySymbolAtEnd),
                environment = environment
            )

            Text(
                text = maxInputString,
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
