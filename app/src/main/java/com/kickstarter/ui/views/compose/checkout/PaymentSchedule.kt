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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.extensions.findCurrencySymbolIndex
import com.kickstarter.libs.utils.extensions.trimAllWhitespace
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.PaymentIncrementFactory
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.type.PaymentIncrementState
import com.kickstarter.type.PaymentIncrementStateReason
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.compose.designsystem.KSClickableText
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography

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

private fun getMockKSCurrencyForUS(): KSCurrency {
    val config = ConfigFactory.configForUSUser()

    val currentConfig = MockCurrentConfigV2()
    currentConfig.config(config)
    val mockCurrency = KSCurrency(currentConfig)

    return mockCurrency
}

// Expanded State Preview
@Preview(showBackground = true, name = "Expanded State", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewExpandedPaymentScheduleDark() {
    KSTheme {
        PaymentSchedule(
            isExpanded = true,
            onExpandChange = {},
            paymentIncrements = PaymentIncrementFactory.samplePaymentIncrements(),
            ksCurrency = getMockKSCurrencyForUS()
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
            paymentIncrements = PaymentIncrementFactory.samplePaymentIncrements(),
            ksCurrency = getMockKSCurrencyForUS()
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
            paymentIncrements = PaymentIncrementFactory.samplePaymentIncrements(),
            ksCurrency = getMockKSCurrencyForUS()
        )
    }
}

@Composable
fun PaymentSchedule(
    isExpanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit = {},
    paymentIncrements: List<PaymentIncrement> = listOf(),
    onDisclaimerClicked: (DisclaimerItems) -> Unit = {},
    ksCurrency: KSCurrency? = null,
    projectCurrentCurrency: String? = "",
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.kds_support_100)
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
                color = colors.textPrimary
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
                PaymentRow(
                    paymentIncrement,
                    ksCurrency = ksCurrency
                )
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
fun PaymentRow(
    paymentIncrement: PaymentIncrement,
    ksCurrency: KSCurrency?
) {
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
                color = colors.textPrimary
            )
            paymentIncrement.stateReason?.let { StatusBadge(paymentIncrement.state, it) }
        }
        Text(
            modifier = Modifier.testTag(PaymentScheduleTestTags.AMOUNT_TEXT.name),
            text = paymentIncrementStyledCurrency(paymentIncrement, ksCurrency),
            style = typography.title3,
            color = colors.textPrimary
        )
    }
}

@Composable
private fun paymentIncrementStyledCurrency(
    paymentIncrement: PaymentIncrement,
    ksCurrency: KSCurrency?
): AnnotatedString {
    val country = Country.findByCurrencyCode(paymentIncrement.amount().currencyCode ?: "")
    val currencySymbol = country?.let { ksCurrency?.getCurrencySymbol(it, false) } ?: ""

    val currencyToFormat = "${currencySymbol.trimAllWhitespace()} ${paymentIncrement.amount().amountAsFloat}"
    val annotatedString = currencyToFormat.let {
        return@let buildAnnotatedString {
            val currencySymbolIndex = it.findCurrencySymbolIndex()
            val dotIndex = it.indexOf('.')

            if (currencySymbolIndex != null && dotIndex != -1) {
                // Append "USD $" with smaller size and top alignment
                withStyle(
                    style = SpanStyle(
                        fontSize = typography.title3.fontSize * 0.6f, // Relative to typography style
                        baselineShift = BaselineShift(0.25f) // Align on top
                    )
                ) {
                    append(it.substring(0, currencySymbolIndex + 1))
                }
                append(it.substring(currencySymbolIndex + 1, dotIndex))
                // Append ".75" with smaller size and top alignment
                withStyle(
                    style = SpanStyle(
                        fontSize = typography.title3.fontSize * 0.6f, // Relative to typography style
                        baselineShift = BaselineShift(0.25f) // Align on top
                    )
                ) {
                    append(it.substring(dotIndex))
                }
            } else {
                append(it)
            }
        }
    }
    return annotatedString
}

@Composable
fun StatusBadge(state: PaymentIncrementState, stateReason: PaymentIncrementStateReason) {
    when (state) {
        PaymentIncrementState.ERRORED -> {
            if (stateReason == PaymentIncrementStateReason.REQUIRES_ACTION) {
                Box(
                    modifier = Modifier
                        .background(
                            color = colors.backgroundAccentOrangeSubtle,
                            shape = RoundedCornerShape(
                                topStart = dimensions.radiusSmall,
                                topEnd = dimensions.radiusSmall,
                                bottomStart = dimensions.radiusSmall,
                                bottomEnd = dimensions.radiusSmall
                            ),
                        )
                        .padding(
                            horizontal = dimensions.paddingSmall,
                            vertical = dimensions.paddingXSmall
                        )
                ) {
                    Text(
                        modifier = Modifier.testTag(PaymentScheduleTestTags.BADGE_TEXT.name),
                        text = stringResource(id = R.string.fpo_authentication_required),
                        style = typography.caption1Medium,
                        color = colors.kds_support_400
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(
                            color = colors.backgroundDangerSubtle,
                            shape = RoundedCornerShape(
                                topStart = dimensions.radiusSmall,
                                topEnd = dimensions.radiusSmall,
                                bottomStart = dimensions.radiusSmall,
                                bottomEnd = dimensions.radiusSmall
                            ),
                        )
                        .padding(
                            horizontal = dimensions.paddingSmall,
                            vertical = dimensions.paddingXSmall
                        )
                ) {
                    Text(
                        modifier = Modifier.testTag(PaymentScheduleTestTags.BADGE_TEXT.name),
                        text = stringResource(id = R.string.fpo_errored_payment),
                        style = typography.caption1Medium,
                        color = colors.textAccentRedBold
                    )
                }
            }
        }

        PaymentIncrementState.COLLECTED -> {
            Box(
                modifier = Modifier
                    .background(
                        color = colors.textAccentGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(
                            topStart = dimensions.radiusSmall,
                            topEnd = dimensions.radiusSmall,
                            bottomStart = dimensions.radiusSmall,
                            bottomEnd = dimensions.radiusSmall
                        ),
                    )
                    .padding(
                        horizontal = dimensions.paddingSmall,
                        vertical = dimensions.paddingXSmall
                    )
            ) {
                Text(
                    modifier = Modifier.testTag(PaymentScheduleTestTags.BADGE_TEXT.name),
                    text = stringResource(id = R.string.fpo_collected),
                    style = typography.caption1Medium,
                    color = colors.textAccentGreen
                )
            }
        }

        PaymentIncrementState.UNATTEMPTED -> {
            Box(
                modifier = Modifier
                    .background(
                        color = colors.kds_support_200,
                        shape = RoundedCornerShape(
                            topStart = dimensions.radiusSmall,
                            topEnd = dimensions.radiusSmall,
                            bottomStart = dimensions.radiusSmall,
                            bottomEnd = dimensions.radiusSmall
                        ),
                    )
                    .padding(
                        horizontal = dimensions.paddingSmall,
                        vertical = dimensions.paddingXSmall
                    )
            ) {
                Text(
                    modifier = Modifier.testTag(PaymentScheduleTestTags.BADGE_TEXT.name),
                    text = stringResource(id = R.string.fpo_scheduled),
                    style = typography.caption1Medium,
                    color = colors.kds_support_400
                )
            }
        }

        PaymentIncrementState.CANCELLED -> {
            Box(
                modifier = Modifier
                    .background(
                        color = colors.kds_support_200,
                        shape = RoundedCornerShape(
                            topStart = dimensions.radiusSmall,
                            topEnd = dimensions.radiusSmall,
                            bottomStart = dimensions.radiusSmall,
                            bottomEnd = dimensions.radiusSmall
                        ),
                    )
                    .padding(
                        horizontal = dimensions.paddingSmall,
                        vertical = dimensions.paddingXSmall
                    )
            ) {
                Text(
                    modifier = Modifier.testTag(PaymentScheduleTestTags.BADGE_TEXT.name),
                    text = stringResource(id = R.string.fpo_cancelled),
                    style = typography.caption1Medium,
                    color = colors.kds_support_400
                )
            }
        }
        PaymentIncrementState.UNKNOWN__ -> {}
    }
}
