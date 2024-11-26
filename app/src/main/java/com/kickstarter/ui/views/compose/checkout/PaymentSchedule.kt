import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.typography


enum class PaymentStatuses {
    COLLECTED,
    AUTHENTICATION_REQUIRED,
    SCHEDULED
}

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
) {
    Card(
        elevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandChange(!isExpanded) }  // Icon click toggles expansion
                    .testTag("payment_schedule_title"),  // Test tag for the header
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment schedule",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = if (isExpanded) painterResource(id = R.drawable.ic_arrow_up) else painterResource(
                        id = R.drawable.ic_arrow_down
                    ),
                    contentDescription = "Expand",
                    tint = colors.textSecondary,
                    modifier = Modifier.testTag("expand_icon")  // Test tag for the icon
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                PaymentRow("Mar 15, 2024", "$20.00", PaymentStatuses.SCHEDULED, Color.Green)
                PaymentRow("Mar 29, 2024", "$20.00", PaymentStatuses.COLLECTED, Color.Red)
                PaymentRow(
                    "Apr 11, 2024",
                    "$20.00",
                    PaymentStatuses.AUTHENTICATION_REQUIRED,
                    Color.Gray
                )
                PaymentRow("Apr 26, 2024", "$20.00", PaymentStatuses.COLLECTED, Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.testTag("terms_of_use_text"),  // Test tag for terms text
                    text = stringResource(id = R.string.fpo_terms_of_use),
                    style = typography.subheadline,
                    color = colors.textAccentGreen
                )
            }
        }
    }
}

@Composable
fun PaymentRow(date: String, amount: String, status: PaymentStatuses, statusColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = date,
                style = typography.body2Medium,
            )
            StatusBadge(status)  // Status badge for each payment row
        }
        Text(
            text = amount,
            fontSize = 14.sp
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
                    modifier = Modifier.testTag("badge_text"),  // Test tag for badge text
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
                    modifier = Modifier.testTag("badge_text"),  // Test tag for badge text
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
                    modifier = Modifier.testTag("badge_text"),  // Test tag for badge text
                    text = stringResource(id = R.string.fpo_scheduled),
                    style = typography.caption1Medium,
                    color = colors.textSecondary
                )
            }
        }
    }
}


