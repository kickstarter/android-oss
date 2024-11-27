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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import java.time.Instant
import java.util.Locale

enum class PaymentScheduleTestTags {
    PAYMENT_SCHEDULE_TITLE,
    EXPAND_ICON,
    DATE_TEXT,
    AMOUNT_TEXT,
    BADGE_TEXT,
    TERMS_OF_USE_TEXT
}

val samplePaymentIncrements = listOf(
    PaymentIncrement(
        id = 1234,
        amount = 3400,
        state = PaymentIncrement.State.UNATTEMPTED,
        paymentIncrementalId = 1,
        paymentIncrementalType = "pledge",
        date = Instant.parse("2024-10-14T18:12:00Z") // Mon, 14 Oct 2024 18:12 UTC
    ),
    PaymentIncrement(
        id = 1235,
        amount = 2500,
        state = PaymentIncrement.State.COLLECTED,
        paymentIncrementalId = 2,
        paymentIncrementalType = "pledge",
        date = Instant.parse("2024-10-15T14:00:00Z") // Tue, 15 Oct 2024 14:00 UTC
    ),
    PaymentIncrement(
        id = 1236,
        amount = 4500,
        state = PaymentIncrement.State.UNATTEMPTED,
        paymentIncrementalId = 3,
        paymentIncrementalType = "pledge",
        date = Instant.parse("2024-10-16T10:00:00Z") // Wed, 16 Oct 2024 10:00 UTC
    ),
    PaymentIncrement(
        id = 1237,
        amount = 5200,
        state = PaymentIncrement.State.COLLECTED,
        paymentIncrementalId = 4,
        paymentIncrementalType = "pledge",
        date = Instant.parse("2024-10-17T16:30:00Z") // Thu, 17 Oct 2024 16:30 UTC
    )
)

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
            onExpandChange = {},
            paymentIncrements = samplePaymentIncrements
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
            onExpandChange = { isExpanded = it },
            paymentIncrements = samplePaymentIncrements

        )
    }
}

@Composable
fun PaymentSchedule(
    isExpanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit = {},
    paymentIncrements: List<PaymentIncrement> = listOf()
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
                    .fillMaxWidth(),
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
                paymentIncrements.forEach { paymentIncrement ->
                    PaymentRow(paymentIncrement)
                }
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
fun PaymentRow(paymentIncrement: PaymentIncrement) {
    val formattedAmount = String.format(Locale.US, "%.2f", paymentIncrement.amount / 100.0)
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
                text = paymentIncrement.formattedDate,
                style = typography.body2Medium,
            )
            StatusBadge(paymentIncrement.state)
        }
        Text(
            modifier = Modifier.testTag(PaymentScheduleTestTags.AMOUNT_TEXT.name),
            text = "USD$ $formattedAmount",
            style = typography.title3
        )
    }
}

@Composable
fun StatusBadge(state: PaymentIncrement.State) {
    when (state) {
        PaymentIncrement.State.UNATTEMPTED -> {
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
                    text = stringResource(id = R.string.fpo_unattempted),
                    style = typography.caption1Medium,
                    color = colors.textSecondary
                )
            }
        }

        PaymentIncrement.State.COLLECTED -> {
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
    }
}
