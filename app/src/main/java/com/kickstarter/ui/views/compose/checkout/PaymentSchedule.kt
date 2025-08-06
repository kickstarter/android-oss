import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

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
                text = stringResource(id = R.string.Payment_schedule),
                style = typographyV2.bodyBoldMD,
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
                resourceId = R.string.profile_settings_about_terms,
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
    val isCollectedAdjusted =
        paymentIncrement.state == PaymentIncrementState.COLLECTED &&
            paymentIncrement.refundedAmount != null
    val displayedAmount =
        if (isCollectedAdjusted) {
            paymentIncrement.refundUpdatedAmountInProjectNativeCurrency
        } else if (paymentIncrement.state == PaymentIncrementState.REFUNDED) {
            paymentIncrement.refundedAmount?.amountFormattedInProjectNativeCurrency
        } else {
            paymentIncrement.amount().amountFormattedInProjectNativeCurrency
        }

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
                style = typographyV2.bodyBoldMD,
                color = colors.textPrimary
            )
            StatusBadge(paymentIncrement.state, paymentIncrement.stateReason, isCollectedAdjusted)
        }
        Text(
            modifier = Modifier.testTag(PaymentScheduleTestTags.AMOUNT_TEXT.name),
            text = displayedAmount ?: "",
            style = typographyV2.bodyXL,
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
                        fontSize = typographyV2.bodyXL.fontSize * 0.6f, // Relative to typography style
                        baselineShift = BaselineShift(0.25f) // Align on top
                    )
                ) {
                    append(it.substring(0, currencySymbolIndex + 1))
                }
                append(it.substring(currencySymbolIndex + 1, dotIndex))
                // Append ".75" with smaller size and top alignment
                withStyle(
                    style = SpanStyle(
                        fontSize = typographyV2.bodyXL.fontSize * 0.6f, // Relative to typography style
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
fun StatusBadge(state: PaymentIncrementState, stateReason: PaymentIncrementStateReason, isCollectedAdjusted: Boolean) {
    when (state) {
        PaymentIncrementState.ERRORED -> {
            if (stateReason == PaymentIncrementStateReason.REQUIRES_ACTION) {
                val isLight = !isSystemInDarkTheme()
                val backgroundColor = if (isLight) colors.backgroundWarningSubtle else colors.yellow_03
                val textColor = if (isLight) colors.textAccentYellowBold else colors.yellow_08
                Box(
                    modifier = Modifier
                        .background(
                            color = backgroundColor,
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
                        text = stringResource(id = R.string.Authentication_required),
                        style = typographyV2.headingSM,
                        color = textColor
                    )
                }
            } else {
                val isLight = !isSystemInDarkTheme()
                val backgroundColor = if (isLight) colors.red_light else colors.red_03
                val textColor = if (isLight) colors.kds_alert else colors.red_07
                Box(
                    modifier = Modifier
                        .background(
                            color = backgroundColor,
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
                        text = stringResource(id = R.string.Errored_payment),
                        style = typographyV2.headingSM,
                        color = textColor
                    )
                }
            }
        }

        PaymentIncrementState.COLLECTED -> {
            val isLight = !isSystemInDarkTheme()
            val backgroundColor = if (isLight) colors.green_06.copy(alpha = 0.06f) else colors.green_02
            val textColor = if (isLight) colors.green_06 else colors.green_07
            val labelRes = if (isCollectedAdjusted) {
                R.string.Collected_adjusted
            } else {
                R.string.project_view_pledge_status_collected
            }

            Box(
                modifier = Modifier
                    .background(
                        color = backgroundColor,
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
                    text = stringResource(id = labelRes),
                    style = typographyV2.headingSM,
                    color = textColor
                )
            }
        }

        PaymentIncrementState.UNATTEMPTED -> {
            Box(
                modifier = Modifier
                    .background(
                        color = colors.blue_03,
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
                    text = stringResource(id = R.string.Scheduled),
                    style = typographyV2.headingSM,
                    color = colors.blue_09
                )
            }
        }

        PaymentIncrementState.CANCELLED -> {
            val isLight = !isSystemInDarkTheme()
            val backgroundColor = if (isLight) colors.grey_03 else colors.grey_05
            val textColor = colors.grey_10
            Box(
                modifier = Modifier
                    .background(
                        color = backgroundColor,
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
                    text = stringResource(id = R.string.project_view_pledge_status_canceled),
                    style = typographyV2.headingSM,
                    color = textColor
                )
            }
        }
        PaymentIncrementState.REFUNDED -> {
            Box(
                modifier = Modifier
                    .background(
                        color = colors.purple_03,
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
                    text = stringResource(id = R.string.Refunded),
                    style = typographyV2.headingSM,
                    color = colors.purple_08
                )
            }
        }
        PaymentIncrementState.UNKNOWN__ -> {}
        PaymentIncrementState.CHARGEBACK_LOST -> {}
    }
}
