import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.extensions.format
import com.kickstarter.mock.factories.PaymentIncrementFactory
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.compose.designsystem.KSClickableText
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import org.joda.time.DateTime

enum class CollectionPlanTestTags {
    OPTION_PLEDGE_IN_FULL,
    OPTION_PLEDGE_OVER_TIME,
    DESCRIPTION_TEXT,
    BADGE_TEXT,
    EXPANDED_DESCRIPTION_TEXT,
    TERMS_OF_USE_TEXT,
    CHARGE_ITEM,
    RADIO_BUTTON,
    CHARGE_SCHEDULE
}

enum class CollectionOptions {
    PLEDGE_IN_FULL,
    PLEDGE_OVER_TIME,
}



@Preview(name = "Light Mode - Pledge In Full", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Mode - Pledge In Full", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewPledgeInFull() {
    KSTheme {
        CollectionPlan(
            isEligible = true,
            initialSelectedOption = CollectionOptions.PLEDGE_IN_FULL
        )
    }
}

@Preview(name = "Light Mode - Pledge Over Time", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Mode - Pledge Over Time", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewPledgeOverTime() {
    KSTheme {
        CollectionPlan(
            isEligible = true,
            initialSelectedOption = CollectionOptions.PLEDGE_OVER_TIME,
            pledgeOverTimeShortPitch = "You will be charged over four payments.",
            pledgeOverTimeCollectionPlanChargeExplanation = "First charge occurs when the project ends successfully.",
            paymentIncrements = listOf(
                PaymentIncrementFactory.incrementUsdCollected(DateTime.now(), "150"),
                PaymentIncrementFactory.incrementUsdCollected(DateTime.now().plusWeeks(2), "150"),
                PaymentIncrementFactory.incrementUsdCollected(DateTime.now().plusWeeks(4), "150"),
                PaymentIncrementFactory.incrementUsdCollected(DateTime.now().plusWeeks(6), "150"),
            )
        )
    }
}

@Preview(name = "Light Mode - Not Eligible", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Mode - Not Eligible", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewNotEligible() {
    KSTheme {
        CollectionPlan(
            isEligible = false,
            initialSelectedOption = CollectionOptions.PLEDGE_OVER_TIME,
            plotMinimum = "$10 minimum to split"
        )
    }
}


@Composable
fun CollectionPlan(
    isEligible: Boolean,
    initialSelectedOption: CollectionOptions = CollectionOptions.PLEDGE_IN_FULL,
    changeCollectionPlan: (CollectionOptions) -> Unit = {},
    paymentIncrements: List<PaymentIncrement>? = null,
    plotMinimum: String? = null,
    ksCurrency: KSCurrency? = null,
    projectCurrency: String? = null,
    projectCurrentCurrency: String? = null,
    termsOfUseCallback: (DisclaimerItems) -> Unit = {},
    pledgeOverTimeShortPitch: String? = null,
    pledgeOverTimeCollectionPlanChargeExplanation: String? = null,
) {
    var selectedOption by remember { mutableStateOf(initialSelectedOption) }
    changeCollectionPlan.invoke(selectedOption)

    val onOptionSelected: (CollectionOptions) -> Unit = {
        selectedOption = it
        changeCollectionPlan.invoke(it)
    }

    Column(modifier = Modifier.padding(start = dimensions.paddingMedium, end = dimensions.paddingMedium)) {
        PledgeOption(
            modifier = Modifier.testTag(CollectionPlanTestTags.OPTION_PLEDGE_IN_FULL.name),
            optionText = stringResource(id = R.string.Pledge_in_full),
            selected = selectedOption == CollectionOptions.PLEDGE_IN_FULL,
            onSelect = { onOptionSelected.invoke(CollectionOptions.PLEDGE_IN_FULL) }
        )
        Spacer(modifier = Modifier.height(dimensions.paddingSmall))
        PledgeOption(
            modifier = Modifier.testTag(CollectionPlanTestTags.OPTION_PLEDGE_OVER_TIME.name),
            optionText = stringResource(id = R.string.Pledge_Over_Time),
            selected = selectedOption == CollectionOptions.PLEDGE_OVER_TIME,
            description = pledgeOverTimeShortPitch,
            expandedDescription = pledgeOverTimeCollectionPlanChargeExplanation,
            onSelect = { if (isEligible) onOptionSelected.invoke(CollectionOptions.PLEDGE_OVER_TIME) },
            isExpanded = selectedOption == CollectionOptions.PLEDGE_OVER_TIME && isEligible,
            isSelectable = isEligible,
            showBadge = !isEligible,
            paymentIncrements = paymentIncrements,
            plotMinimum = plotMinimum,
            ksCurrency = ksCurrency,
            projectCurrency = projectCurrency,
            projectCurrentCurrency = projectCurrentCurrency,
            termsOfUseCallback = termsOfUseCallback
        )
    }
}

@Composable
fun PledgeOption(
    modifier: Modifier = Modifier,
    optionText: String,
    selected: Boolean,
    description: String? = null,
    expandedDescription: String? = null,
    onSelect: () -> Unit,
    isExpanded: Boolean = false,
    isSelectable: Boolean = true,
    showBadge: Boolean = false,
    plotMinimum: String? = null,
    paymentIncrements: List<PaymentIncrement>? = null,
    ksCurrency: KSCurrency? = null,
    projectCurrency: String? = null,
    projectCurrentCurrency: String? = null,
    termsOfUseCallback: (DisclaimerItems) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.radiusSmall))
            .background(colors.backgroundSurfacePrimary)
            .clickable(enabled = isSelectable, onClick = onSelect)
            .padding(end = dimensions.paddingMedium)
            .semantics { this.selected = selected }
            .then(
                if (!isSelectable) Modifier.padding(
                    vertical = dimensions.paddingMediumSmall,
                    horizontal = dimensions.paddingMediumSmall
                ) else Modifier
            )
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(start = dimensions.paddingSmall)
        ) {
            Column {
                val radioButtonModifier = if (!isSelectable) Modifier.padding(end = dimensions.paddingMediumSmall) else Modifier
                RadioButton(
                    modifier = radioButtonModifier.testTag(CollectionPlanTestTags.RADIO_BUTTON.name),
                    selected = selected,
                    onClick = onSelect.takeIf { isSelectable },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = colors.kds_create_700,
                        unselectedColor = colors.kds_support_300
                    )
                )
            }
            Column {
                Text(
                    modifier = Modifier.padding(
                        top = if (isSelectable) dimensions.paddingMedium else dimensions.dialogButtonSpacing,
                    ),
                    text = optionText,
                    style = typographyV2.subHeadlineMedium,
                    color = if (isSelectable) colors.textPrimary else colors.textDisabled
                )

                Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                if (showBadge && plotMinimum != null) {
                    PledgeBadge(plotMinimum = plotMinimum)
                } else if (!description.isNullOrEmpty()) {
                    Text(
                        modifier = Modifier
                            .padding(bottom = dimensions.paddingMedium)
                            .testTag(CollectionPlanTestTags.DESCRIPTION_TEXT.name),
                        text = description,
                        style = typographyV2.bodyXS,
                        color = colors.textSecondary
                    )
                }

                if (isExpanded && !expandedDescription.isNullOrEmpty()) {
                    Text(
                        modifier = Modifier
                            .padding(bottom = dimensions.paddingMedium)
                            .testTag(CollectionPlanTestTags.EXPANDED_DESCRIPTION_TEXT.name),
                        text = expandedDescription,
                        style = typographyV2.bodyXS,
                        color = colors.textSecondary
                    )
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(dimensions.paddingXSmall))
                    KSClickableText(
                        modifier = Modifier.testTag(CollectionPlanTestTags.TERMS_OF_USE_TEXT.name),
                        resourceId = R.string.See_our_terms_of_use,
                        clickCallback = { termsOfUseCallback.invoke(DisclaimerItems.TERMS) }
                    )

                    if (!paymentIncrements.isNullOrEmpty()) {
                        ChargeSchedule(paymentIncrements, ksCurrency, projectCurrency, projectCurrentCurrency)
                    }
                }
            }
        }
    }
}

@Composable
fun PledgeBadge(modifier: Modifier = Modifier, plotMinimum: String?) {
    Box(
        modifier = modifier
            .background(
                color = colors.borderSubtle,
                shape = RoundedCornerShape(dimensions.radiusSmall)
            )
            .padding(
                start = dimensions.paddingSmall,
                end = dimensions.paddingSmall,
                top = dimensions.paddingXSmall,
                bottom = dimensions.paddingXSmall,
            )
    ) {
        if (plotMinimum != null) {
            Text(
                modifier = Modifier.testTag(CollectionPlanTestTags.BADGE_TEXT.name),
                text = plotMinimum,
                style = typographyV2.bodyBoldMD,
                color = colors.textDisabled
            )
        }
    }
}

@Composable
fun ChargeSchedule(paymentIncrements: List<PaymentIncrement>, ksCurrency: KSCurrency?, projectCurrency: String? = null, projectCurrentCurrency: String? = null) {
    var count = 0
    Column(
        modifier = Modifier
            .testTag(CollectionPlanTestTags.CHARGE_SCHEDULE.name)
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        paymentIncrements.forEach { paymentIncrement ->
            paymentIncrement.paymentIncrementAmount.formattedAmount?.let { formattedAmount ->
                count++
                val chargeString = stringResource(R.string.Charge_number).format(key1 = "number", value1 = count.toString())
                ChargeItem(title = chargeString, date = DateTimeUtils.mediumDate(paymentIncrement.scheduledCollection), amount = formattedAmount)
            }
        }
    }
}

@Composable
fun ChargeItem(title: String, date: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(bottom = dimensions.paddingMediumLarge)) {
            Text(
                modifier = Modifier.testTag(CollectionPlanTestTags.CHARGE_ITEM.name),
                text = title,
                style = typographyV2.bodyBoldMD,
                color = colors.textPrimary
            )

            Row(modifier = Modifier.padding(top = dimensions.paddingXSmall)) {
                Text(
                    modifier = Modifier.width(dimensions.plotChargeItemWidth),
                    text = date,
                    color = colors.textSecondary,
                    style = typographyV2.footNote
                )
                Text(
                    text = amount,
                    color = colors.textSecondary,
                    style = typographyV2.footNote
                )
            }
        }
    }
}
