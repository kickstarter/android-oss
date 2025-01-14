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
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.extensions.parseToDouble
import com.kickstarter.mock.factories.PaymentIncrementFactory
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.compose.designsystem.KSClickableText
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import java.util.Locale

enum class PaymentScheduleTestTags {
    PAYMENT_SCHEDULE_TITLE,
    EXPAND_ICON,
    DATE_TEXT,
    AMOUNT_TEXT,
    BADGE_TEXT,
    TERMS_OF_USE_TEXT
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
            onExpandChange = {},
            paymentIncrements = PaymentIncrementFactory.samplePaymentIncrements()
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
            paymentIncrements = PaymentIncrementFactory.samplePaymentIncrements()
        )
    }
}

@Composable
fun PaymentSchedule(
    isExpanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit = {},
    paymentIncrements: List<PaymentIncrement> = listOf(),
    onDisclaimerClicked: (DisclaimerItems) -> Unit = {}
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
            KSClickableText(
                modifier = Modifier.testTag(PaymentScheduleTestTags.TERMS_OF_USE_TEXT.name),
                resourceId = R.string.fpo_terms_of_use,
                clickCallback = { onDisclaimerClicked.invoke(DisclaimerItems.TERMS) }
            )
        }
    }
}

@Composable
fun PaymentRow(paymentIncrement: PaymentIncrement) {
    val formattedAmount = paymentIncrement.paymentIncrementAmount.formattedAmount
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
                text = DateTimeUtils.mediumDate(paymentIncrement.scheduledCollection),
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

        PaymentIncrement.State.UNKNOWN -> {}
    }
}
