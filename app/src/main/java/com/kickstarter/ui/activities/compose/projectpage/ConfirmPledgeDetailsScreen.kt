package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.parseToDouble
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSDividerLineGrey
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSStepper
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.compose.designsystem.shapes
import java.math.RoundingMode

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ConfirmPledgeDetailsScreenPreviewNoRewards() {
    KSTheme {
        ConfirmPledgeDetailsScreen(
            modifier = Modifier,
            environment = Environment.builder().build(),
            project = Project.builder().build(),
            selectedReward = null,
            onContinueClicked = {},
            rewardsContainAddOns = false,
            rewardsHaveShippables = true,
            currentShippingRule = ShippingRule.builder().build(),
            totalAmount = 1.0,
            initialBonusSupport = 1.0,
            totalBonusSupport = 1.0,
            maxPledgeAmount = 1000.0,
            minPledgeStep = 1.0,
            onShippingRuleSelected = {},
            onBonusSupportMinusClicked = {},
            onBonusSupportPlusClicked = {},
            onBonusSupportInputted = {}
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun ConfirmPledgeDetailsScreenPreviewNoRewardsWarning() {
    KSTheme {
        ConfirmPledgeDetailsScreen(
            modifier = Modifier,
            environment = Environment.builder().build(),
            project = Project.builder().build(),
            selectedReward = null,
            onContinueClicked = {},
            rewardsContainAddOns = false,
            rewardsHaveShippables = true,
            currentShippingRule = ShippingRule.builder().build(),
            totalAmount = 1001.0,
            initialBonusSupport = 1.0,
            totalBonusSupport = 1.0,
            maxPledgeAmount = 1000.0,
            minPledgeStep = 1.0,
            onShippingRuleSelected = {},
            onBonusSupportMinusClicked = {},
            onBonusSupportPlusClicked = {},
            onBonusSupportInputted = {}
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
            environment = Environment.builder().build(),
            project = Project.builder().build(),
            selectedReward = Reward.builder().build(),
            onContinueClicked = {},
            rewardsList = (1..2).map {
                Pair("Cool Item $it", "$20")
            },
            rewardsContainAddOns = false,
            rewardsHaveShippables = true,
            shippingAmount = 5.0,
            currentShippingRule = ShippingRule.builder().build(),
            totalAmount = 55.0,
            initialBonusSupport = 0.0,
            totalBonusSupport = 0.0,
            maxPledgeAmount = 1000.0,
            minPledgeStep = 1.0,
            countryList = listOf(ShippingRule.builder().build()),
            onShippingRuleSelected = {},
            onBonusSupportMinusClicked = {},
            onBonusSupportPlusClicked = {},
            onBonusSupportInputted = {}
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
            environment = Environment.builder().build(),
            project = Project.builder().build(),
            selectedReward = Reward.builder().build(),
            onContinueClicked = {},
            rewardsList = (1..5).map {
                Pair("Cool Item $it", "$20")
            },
            rewardsContainAddOns = true,
            rewardsHaveShippables = true,
            shippingAmount = 5.0,
            currentShippingRule = ShippingRule.builder().build(),
            totalAmount = 105.0,
            initialBonusSupport = 0.0,
            totalBonusSupport = 0.0,
            maxPledgeAmount = 1000.0,
            minPledgeStep = 1.0,
            onShippingRuleSelected = {},
            onBonusSupportMinusClicked = {},
            onBonusSupportPlusClicked = {},
            onBonusSupportInputted = {}
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
            environment = Environment.builder().build(),
            project = Project.builder().build(),
            selectedReward = Reward.builder().build(),
            onContinueClicked = {},
            rewardsList = (1..2).map {
                Pair("Cool Item $it", "$20")
            },
            rewardsContainAddOns = false,
            rewardsHaveShippables = true,
            shippingAmount = 5.0,
            currentShippingRule = ShippingRule.builder().build(),
            totalAmount = 55.0,
            initialBonusSupport = 0.0,
            totalBonusSupport = 10.0,
            maxPledgeAmount = 1000.0,
            minPledgeStep = 1.0,
            countryList = listOf(ShippingRule.builder().build()),
            onShippingRuleSelected = {},
            onBonusSupportMinusClicked = {},
            onBonusSupportPlusClicked = {},
            onBonusSupportInputted = {}
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
            environment = Environment.builder().build(),
            project = Project.builder().build(),
            selectedReward = Reward.builder().build(),
            onContinueClicked = {},
            rewardsList = (1..5).map {
                Pair("Cool Item $it", "$20")
            },
            rewardsContainAddOns = true,
            rewardsHaveShippables = true,
            shippingAmount = 5.0,
            currentShippingRule = ShippingRule.builder().build(),
            totalAmount = 115.0,
            initialBonusSupport = 0.0,
            totalBonusSupport = 10.0,
            maxPledgeAmount = 1000.0,
            minPledgeStep = 1.0,
            onShippingRuleSelected = {},
            onBonusSupportMinusClicked = {},
            onBonusSupportPlusClicked = {},
            onBonusSupportInputted = {}
        )
    }
}

@Composable
fun ConfirmPledgeDetailsScreen(
    modifier: Modifier,
    environment: Environment?,
    project: Project,
    selectedReward: Reward?,
    onContinueClicked: () -> Unit,
    rewardsList: List<Pair<String, String>> = listOf(),
    rewardsContainAddOns: Boolean,
    rewardsHaveShippables: Boolean,
    shippingAmount: Double = 0.0,
    currentShippingRule: ShippingRule,
    countryList: List<ShippingRule> = listOf(),
    onShippingRuleSelected: (ShippingRule) -> Unit,
    totalAmount: Double,
    initialBonusSupport: Double,
    totalBonusSupport: Double,
    maxPledgeAmount: Double,
    minPledgeStep: Double,
    isLoading: Boolean = false,
    onBonusSupportPlusClicked: () -> Unit,
    onBonusSupportMinusClicked: () -> Unit,
    onBonusSupportInputted: (input: Double) -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val totalAmountString = environment?.ksCurrency()?.let {
        RewardViewUtils.styleCurrency(
            totalAmount,
            project,
            it
        ).toString()
    } ?: ""

    val totalAmountConvertedString = environment?.ksCurrency()?.formatWithUserPreference(
        totalAmount,
        project,
        RoundingMode.UP,
        2
    ) ?: ""

    val aboutTotalString = if (project.currentCurrency() == project.currency()) "" else {
        environment?.ksString()?.format(
            stringResource(id = R.string.About_reward_amount),
            "reward_amount",
            totalAmountConvertedString
        ) ?: "About $totalAmountConvertedString"
    }

    val shippingAmountString = environment?.ksCurrency()?.let {
        RewardViewUtils.styleCurrency(
            shippingAmount,
            project,
            it
        ).toString()
    } ?: ""

    val shippingLocation = currentShippingRule.location()?.displayableName() ?: ""

    val initialBonusSupportString = environment?.ksCurrency()?.let {
        RewardViewUtils.styleCurrency(
            initialBonusSupport,
            project,
            it
        ).toString()
    } ?: ""

    val totalBonusSupportString = environment?.ksCurrency()?.let {
        RewardViewUtils.styleCurrency(
            totalBonusSupport,
            project,
            it
        ).toString()
    } ?: ""

    // Currency symbol, which can be positioned at start or end of amount depending on country
    val currencySymbolStartAndEnd = environment?.ksCurrency()?.let {
        val symbolAndStart = RewardViewUtils.currencySymbolAndPosition(
            project,
            it
        )
        val symbol = symbolAndStart.first
        val symbolAtStart = symbolAndStart.second
        if (symbolAtStart) {
            Pair(symbol.toString(), null)
        } else {
            Pair(null, symbol.toString())
        }
    } ?: Pair(null, null)

    val deliveryDateString = if (selectedReward?.estimatedDeliveryOn().isNotNull()) {
        DateTimeUtils.estimatedDeliveryOn(
            requireNotNull(
                selectedReward?.estimatedDeliveryOn()
            )
        )
    } else ""

    val maxPledgeString = environment?.ksString()?.format(
        stringResource(R.string.Enter_an_amount_less_than_max_pledge),
        "max_pledge",
        maxPledgeAmount.toString()
    ) ?: ""

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
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
                                            text = totalAmountString,
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
                        text = stringResource(id = R.string.Confirm_your_pledge_details),
                        style = typography.title3Bold,
                        color = colors.textPrimary
                    )
                }

                if (rewardsList.isNotEmpty() && shippingLocation.isNotEmpty() && rewardsHaveShippables) {
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
                                if (countryList.isNotEmpty() && !rewardsContainAddOns && rewardsHaveShippables) {
                                    CountryInputWithDropdown(
                                        interactionSource = interactionSource,
                                        initialCountryInput = shippingLocation,
                                        countryList = countryList,
                                        onShippingRuleSelected = onShippingRuleSelected
                                    )
                                } else {
                                    Text(
                                        text = shippingLocation,
                                        style = typography.subheadline,
                                        color = colors.textPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Text(
                                    text = "+ $shippingAmountString",
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
                        initialBonusSupport = initialBonusSupport,
                        totalBonusSupport = totalBonusSupport,
                        currencySymbolAtStart = currencySymbolStartAndEnd.first,
                        currencySymbolAtEnd = currencySymbolStartAndEnd.second,
                        canAddMore = totalAmount + minPledgeStep <= maxPledgeAmount,
                        onBonusSupportPlusClicked = onBonusSupportPlusClicked,
                        onBonusSupportMinusClicked = onBonusSupportMinusClicked,
                        onBonusSupportInputted = onBonusSupportInputted
                    )

                    if (totalAmount >= maxPledgeAmount) {
                        Spacer(modifier = Modifier.height(dimensions.paddingXSmall))

                        Text(
                            text = maxPledgeString,
                            style = typography.title3,
                            color = colors.textPrimary
                        )
                    }
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
                                        text = totalAmountString,
                                        style = typography.headline,
                                        color = colors.textPrimary
                                    )

                                    if (!aboutTotalString.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(dimensions.paddingXSmall))

                                        Text(
                                            text = aboutTotalString,
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
                            ksString = environment?.ksString(),
                            rewardsList = rewardsList,
                            shippingAmount = shippingAmount,
                            shippingAmountString = shippingAmountString,
                            initialShippingLocation = shippingLocation,
                            totalAmount = totalAmountString,
                            totalAmountCurrencyConverted = aboutTotalString,
                            initialBonusSupport = initialBonusSupportString,
                            totalBonusSupport = totalBonusSupportString,
                            deliveryDateString = deliveryDateString,
                            rewardsHaveShippables = rewardsHaveShippables
                        )
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.backgroundAccentGraySubtle.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                KSCircularProgressIndicator()
            }
        }
    }
}

@Composable
fun BonusSupportContainer(
    isForNoRewardPledge: Boolean,
    initialBonusSupport: Double,
    totalBonusSupport: Double,
    currencySymbolAtStart: String?,
    currencySymbolAtEnd: String?,
    canAddMore: Boolean,
    onBonusSupportPlusClicked: () -> Unit,
    onBonusSupportMinusClicked: () -> Unit,
    onBonusSupportInputted: (input: Double) -> Unit
) {
    val bonusAmountMaxDigits = integerResource(R.integer.max_length)

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
            Spacer(modifier = Modifier.height(dimensions.paddingSmall))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KSStepper(
                onPlusClicked = onBonusSupportPlusClicked,
                isPlusEnabled = canAddMore,
                onMinusClicked = onBonusSupportMinusClicked,
                isMinusEnabled = initialBonusSupport != totalBonusSupport,
                enabledButtonBackgroundColor = colors.kds_white
            )

            Spacer(modifier = Modifier.weight(1f))

            if (!isForNoRewardPledge) {
                Text(text = "+", style = typography.calloutMedium, color = colors.textSecondary)

                Spacer(modifier = Modifier.width(dimensions.paddingMediumSmall))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(
                        color = colors.kds_white,
                        shape = shapes.small
                ).padding(
                    start = dimensions.paddingXSmall,
                    top = dimensions.paddingXSmall,
                    bottom = dimensions.paddingXSmall,
                    end = dimensions.paddingXSmall
                ),

            ) {
                Text(
                    text = currencySymbolAtStart ?: "",
                    color = colors.kds_create_700
                )
                BasicTextField(
                    modifier = Modifier.width(IntrinsicSize.Min),
                    value = if (totalBonusSupport % 1.0 == 0.0) totalBonusSupport.toInt().toString() else totalBonusSupport.toString(),
                    onValueChange = {
                        if (it.length <= bonusAmountMaxDigits) onBonusSupportInputted(it.parseToDouble())
                    },
                    textStyle = typography.title1.copy(color = colors.kds_create_700),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Text(
                    text = currencySymbolAtEnd ?: "",
                    color = colors.kds_create_700
                )
            }

        }
    }
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
