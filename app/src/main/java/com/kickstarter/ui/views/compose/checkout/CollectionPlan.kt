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
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

enum class CollectionPlanTestTags {
    OPTION_PLEDGE_IN_FULL,
    OPTION_PLEDGE_OVER_TIME,
    DESCRIPTION_TEXT,
    BADGE_TEXT,
    EXPANDED_DESCRIPTION_TEXT,
    TERMS_OF_USE_TEXT,
    CHARGE_ITEM,
}

enum class CollectionOptions {
    PLEDGE_IN_FULL,
    PLEDGE_OVER_TIME,
}

@Preview(
    name = "Light Eligible - Pledge in Full Selected",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark Eligible - Pledge in Full Selected",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PreviewPledgeInFullSelected() {
    KSTheme {
        CollectionPlan(
            isEligible = true,
            initialSelectedOption = CollectionOptions.PLEDGE_IN_FULL.name
        )
    }
}

@Preview(
    name = "Light Eligible - Pledge Over Time Selected",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark Eligible - Pledge Over Time Selected",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewPledgeOverTimeSelected() {
    KSTheme {
        CollectionPlan(
            isEligible = true,
            initialSelectedOption = CollectionOptions.PLEDGE_OVER_TIME.name
        )
    }
}

@Preview(name = "Light Not Eligible", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Not Eligible", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewNotEligibleComponent() {
    KSTheme {
        CollectionPlan(
            isEligible = false,
            initialSelectedOption = CollectionOptions.PLEDGE_IN_FULL.name
        )
    }
}

@Composable
fun CollectionPlan(
    isEligible: Boolean,
    initialSelectedOption: String = CollectionOptions.PLEDGE_IN_FULL.name
) {
    var selectedOption by remember { mutableStateOf(initialSelectedOption) }

    Column(modifier = Modifier.padding(start = dimensions.paddingMedium, end = dimensions.paddingMedium)) {
        PledgeOption(
            optionText = stringResource(id = R.string.fpo_pledge_in_full),
            selected = selectedOption == CollectionOptions.PLEDGE_IN_FULL.name,
            onSelect = { selectedOption = CollectionOptions.PLEDGE_IN_FULL.name },
            modifier = Modifier.testTag(CollectionPlanTestTags.OPTION_PLEDGE_IN_FULL.name)
        )
        Spacer(Modifier.height(dimensions.paddingSmall))
        PledgeOption(
            modifier = Modifier.testTag(CollectionPlanTestTags.OPTION_PLEDGE_OVER_TIME.name),
            optionText = stringResource(id = R.string.fpo_pledge_over_time),
            selected = selectedOption == CollectionOptions.PLEDGE_OVER_TIME.name,
            description = if (isEligible) stringResource(id = R.string.fpo_you_will_be_charged_for_your_pledge_over_four_payments_at_no_extra_cost) else null,
            onSelect = {
                if (isEligible) selectedOption = CollectionOptions.PLEDGE_OVER_TIME.name
            },
            isExpanded = selectedOption == CollectionOptions.PLEDGE_OVER_TIME.name && isEligible,
            isSelectable = isEligible,
            showBadge = !isEligible,
        )
    }
}

@Composable
fun PledgeOption(
    modifier: Modifier = Modifier,
    optionText: String,
    selected: Boolean,
    description: String? = null,
    onSelect: () -> Unit,
    isExpanded: Boolean = false,
    isSelectable: Boolean = true,
    showBadge: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.radiusSmall))
            .background(colors.kds_white)
            .clickable(enabled = isSelectable, onClick = onSelect)
            .padding(bottom = dimensions.paddingSmall, end = dimensions.paddingMedium)
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
                RadioButton(
                    modifier = if (!isSelectable) Modifier.padding(end = dimensions.paddingMediumSmall) else Modifier,
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
                        bottom = dimensions.paddingSmall
                    ),
                    text = optionText,
                    style = typography.subheadlineMedium,
                    color = if (isSelectable) colors.kds_black else colors.textDisabled
                )
                if (showBadge) {
                    Spacer(modifier = Modifier.height(dimensions.paddingXSmall))
                    PledgeBadge()
                } else if (description != null) {
                    Text(
                        modifier = Modifier
                            .padding(bottom = dimensions.paddingSmall)
                            .testTag(CollectionPlanTestTags.DESCRIPTION_TEXT.name),
                        text = description,
                        style = typography.caption2,
                        color = colors.textDisabled
                    )
                }
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(dimensions.paddingSmall))
                    Text(
                        modifier = Modifier.testTag(CollectionPlanTestTags.EXPANDED_DESCRIPTION_TEXT.name),
                        text = stringResource(id = R.string.fpo_the_first_charge_will_be_24_hours_after_the_project_ends_successfully),
                        style = typography.caption2,
                        color = colors.textDisabled
                    )
                    Spacer(modifier = Modifier.height(dimensions.paddingXSmall))
                    Text(
                        modifier = Modifier.testTag(CollectionPlanTestTags.TERMS_OF_USE_TEXT.name),
                        text = stringResource(id = R.string.fpo_see_our_terms_of_use),
                        style = typography.caption2,
                        color = colors.textAccentGreen
                    )
                    ChargeSchedule()
                }
            }
        }
    }
}

@Composable
fun PledgeBadge(modifier: Modifier = Modifier) {
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
        Text(
            modifier = Modifier.testTag(CollectionPlanTestTags.BADGE_TEXT.name),
            text = stringResource(
                id = R.string.fpo_available_for_pledges_over_150
            ),
            style = typography.body2Medium,
            color = colors.textDisabled
        )
    }
}

@Composable
fun ChargeSchedule() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        ChargeItem("Charge 1", "Aug 11, 2024", "$250")
        ChargeItem("Charge 2", "Aug 15, 2024", "$250")
        ChargeItem("Charge 3", "Aug 29, 2024", "$250")
        ChargeItem("Charge 4", "Sep 12, 2024", "$250")
    }
}

@Composable
fun ChargeItem(title: String, date: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(bottom = dimensions.paddingMediumLarge)) {
            Text(
                modifier = Modifier.testTag(CollectionPlanTestTags.CHARGE_ITEM.name),
                text = title, style = typography.body2Medium
            )

            Row(modifier = Modifier.padding(top = dimensions.paddingXSmall)) {
                Text(text = date, color = colors.textSecondary, style = typography.footnote)
                Spacer(modifier = Modifier.width(dimensions.paddingXLarge))
                Text(text = amount, color = colors.textSecondary, style = typography.footnote)
            }
        }
    }
}
