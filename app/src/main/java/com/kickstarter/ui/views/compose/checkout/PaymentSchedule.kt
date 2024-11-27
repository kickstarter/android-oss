import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R

import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

enum class PaymentScheduleTestTags {
    PAYMENT_SCHEDULE_TITLE,
    DATE_TEXT,
    AMOUNT_TEXT,
    EXPAND_ICON,
    BADGE_TEXT,
    TERMS_OF_USE_TEXT,
}

enum class PaymentStatuses {
    COLLECTED,
    AUTHENTICATION_REQUIRED,
    SCHEDULED
}

@Preview(
    name = "Dark Collapsed State", uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    name = "Light Collapsed State", uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
fun PreviewCollapsedPaymentScheduleWhite() {
    KSTheme {
        PaymentSchedule(
            isExpanded = false,
            onExpandChange = {}
        )
    }

}

// Expanded State Preview
@Preview(showBackground = true, name = "Expanded State")
@Composable
fun PreviewExpandedPaymentSchedule() {
    KSTheme {
        PaymentSchedule(
            isExpanded = true,
            onExpandChange = {}
        )
    }
}

@Preview
@Composable
fun InteractivePaymentSchedulePreview() {
    var isExpanded by remember { mutableStateOf(false) }
    KSTheme {
        PaymentSchedule(
            isExpanded = isExpanded,
            onExpandChange = { isExpanded = it }
        )
    }
}


@Composable
fun PaymentSchedule(
    isExpanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit = {},
    //TODO: Add payment schedule data model when available
) {
    Card(
        elevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensions.paddingMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.paddingMedium)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.testTag(PaymentScheduleTestTags.PAYMENT_SCHEDULE_TITLE.name),
                    text = stringResource(id = R.string.fpo_payment_schedule),
                    style = typography.body2Medium,
                )
                Icon(
                    modifier = Modifier
                        .testTag(PaymentScheduleTestTags.EXPAND_ICON.name)
                        .clickable { onExpandChange(!isExpanded) },
                    painter =
                    if (isExpanded) painterResource(id = R.drawable.ic_arrow_up) else painterResource(
                        id = R.drawable.ic_arrow_down
                    ),
                    contentDescription = "Expand",
                    tint = colors.textSecondary,
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(dimensions.paddingSmall))
                PaymentRow("Mar 15, 2024", " $20.00", PaymentStatuses.SCHEDULED)
                PaymentRow("Mar 29, 2024", "$20.00", PaymentStatuses.COLLECTED)
                PaymentRow(
                    "Apr 11, 2024",
                    "$20.00",
                    PaymentStatuses.AUTHENTICATION_REQUIRED,
                )
                PaymentRow("Apr 26, 2024", "$20.00", PaymentStatuses.COLLECTED)
                Spacer(modifier = Modifier.height(dimensions.paddingSmall))
                Text(
                    modifier = Modifier.testTag(PaymentScheduleTestTags.TERMS_OF_USE_TEXT.name),
                    text = stringResource(id = R.string.fpo_terms_of_use),
                    style = typography.subheadline,
                    color = colors.textAccentGreen
                )
            }
        }
    }
}

@Composable
fun PaymentRow(date: String, amount: String, status: PaymentStatuses) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensions.paddingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensions.paddingXSmall)
        ) {
            Text(
                modifier = Modifier.testTag(PaymentScheduleTestTags.DATE_TEXT.name),
                text = date,
                style = typography.body2Medium,
            )
            StatusBadge(status)
        }
        Text(
            modifier = Modifier.testTag(PaymentScheduleTestTags.AMOUNT_TEXT.name),
            text = amount,
            style = typography.title1
        )
    }
}

@Composable
fun StatusBadge(status: PaymentStatuses) {
    when (status) {
        PaymentStatuses.COLLECTED -> {
            Box(
                modifier = Modifier
                    .background(
                        color = colors.textAccentGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    modifier = Modifier.testTag(PaymentScheduleTestTags.BADGE_TEXT.name),
                    text = stringResource(id = R.string.fpo_collected),
                    style = typography.caption1Medium,
                    color = colors.textAccentGreen
                )
            }
        }

        PaymentStatuses.AUTHENTICATION_REQUIRED -> {
            Box(
                modifier = Modifier
                    .background(
                        color = colors.backgroundAccentOrangeSubtle,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    modifier = Modifier.testTag(PaymentScheduleTestTags.BADGE_TEXT.name),
                    text = stringResource(id = R.string.fpo_authentication_required),
                    style = typography.caption1Medium,
                    color = colors.textSecondary
                )
            }
        }

        PaymentStatuses.SCHEDULED -> {
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    modifier = Modifier.testTag(PaymentScheduleTestTags.BADGE_TEXT.name),
                    text = stringResource(id = R.string.fpo_scheduled),
                    style = typography.caption1Medium,
                    color = colors.textSecondary
                )
            }
        }
    }
}


