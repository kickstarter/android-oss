package com.kickstarter.mock.factories

import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.PaymentIncrementAmount
import com.kickstarter.models.PaymentIncrementBadge
import com.kickstarter.models.PaymentIncrementBadgeVariant
import com.kickstarter.type.CurrencyCode
import com.kickstarter.type.PaymentIncrementState
import com.kickstarter.type.PaymentIncrementStateReason
import org.joda.time.DateTime

class PaymentIncrementFactory {
    companion object {

        fun paymentIncrement(
            paymentIncrementAmount: PaymentIncrementAmount,
            paymentIncrementBadge: PaymentIncrementBadge,
            paymentIncrementableId: String,
            paymentIncrementableType: String,
            scheduledCollection: DateTime,
            state: PaymentIncrementState,
            stateReason: PaymentIncrementStateReason?,
            refundedAmount: PaymentIncrementAmount? = null,
            refundUpdatedAmountInProjectNativeCurrency: String? = null
        ): PaymentIncrement {
            return PaymentIncrement.builder()
                .amount(paymentIncrementAmount)
                .paymentIncrementBadge(paymentIncrementBadge)
                .paymentIncrementableId(paymentIncrementableId)
                .paymentIncrementableType(paymentIncrementableType)
                .scheduledCollection(scheduledCollection)
                .state(state)
                .stateReason(stateReason)
                .refundedAmount(refundedAmount)
                .refundUpdatedAmountInProjectNativeCurrency(refundUpdatedAmountInProjectNativeCurrency)
                .build()
        }

        fun amount(
            formattedAmount: String?,
            formattedAmountWithCode: String?,
            amountAsCents: String?,
            amountAsFloat: String?,
            currencyCode: String?,
            amountFormattedInProjectNativeCurrency: String?,

        ): PaymentIncrementAmount {
            return PaymentIncrementAmount.builder()
                .formattedAmount(formattedAmount)
                .formattedAmountWithCode(formattedAmountWithCode)
                .amountFormattedInProjectNativeCurrency(amountFormattedInProjectNativeCurrency)
                .amountAsCents(amountAsCents)
                .amountAsFloat(amountAsFloat)
                .currencyCode(currencyCode)
                .build()
        }

        fun badge(
            copy: String,
            variant: PaymentIncrementBadgeVariant
        ) : PaymentIncrementBadge {
            return PaymentIncrementBadge.builder()
                .copy(copy)
                .variant(variant)
                .build()
        }

        fun incrementUsdUncollected(dateTime: DateTime, formattedAmount: String): PaymentIncrement {
            return paymentIncrement(
                paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = formattedAmount, formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                paymentIncrementBadge = PaymentIncrementFactory.badge(copy = "Scheduled", variant = PaymentIncrementBadgeVariant.PURPLE),
                scheduledCollection = dateTime,
                paymentIncrementableId = "",
                paymentIncrementableType = "",
                state = PaymentIncrementState.UNATTEMPTED,
                stateReason = PaymentIncrementStateReason.REQUIRES_ACTION
            )
        }

        fun incrementUsdCollected(dateTime: DateTime, formattedAmount: String): PaymentIncrement {
            return paymentIncrement(
                paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = formattedAmount, formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                paymentIncrementBadge = PaymentIncrementFactory.badge(copy = "Collected", variant = PaymentIncrementBadgeVariant.GREEN),
                scheduledCollection = dateTime,
                paymentIncrementableId = "",
                paymentIncrementableType = "",
                state = PaymentIncrementState.COLLECTED,
                stateReason = PaymentIncrementStateReason.UNKNOWN__
            )
        }

        fun incrementUsdRefunded(dateTime: DateTime, formattedAmount: String): PaymentIncrement {
            val amount = PaymentIncrementFactory.amount(formattedAmount = formattedAmount, formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$")
            return paymentIncrement(
                paymentIncrementAmount = amount,
                paymentIncrementBadge = PaymentIncrementFactory.badge(copy = "Refunded", variant = PaymentIncrementBadgeVariant.GRAY),
                scheduledCollection = dateTime,
                paymentIncrementableId = "",
                paymentIncrementableType = "",
                state = PaymentIncrementState.REFUNDED,
                stateReason = PaymentIncrementStateReason.UNKNOWN__,
                refundedAmount = amount
            )
        }

        fun samplePaymentIncrements(): List<PaymentIncrement> {
            val now = DateTime.now()

            return listOf(
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    paymentIncrementBadge = PaymentIncrementFactory.badge(copy = "Scheduled", variant = PaymentIncrementBadgeVariant.PURPLE),
                    state = PaymentIncrementState.UNATTEMPTED,
                    paymentIncrementableId = "1",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(15),
                    stateReason = PaymentIncrementStateReason.REQUIRES_ACTION
                ),
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    paymentIncrementBadge = PaymentIncrementFactory.badge(copy = "Scheduled (adjusted)", variant = PaymentIncrementBadgeVariant.PURPLE),
                    state = PaymentIncrementState.UNATTEMPTED,
                    paymentIncrementableId = "1",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(15),
                    stateReason = PaymentIncrementStateReason.REFUND_ADJUSTED
                ),
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    paymentIncrementBadge = PaymentIncrementFactory.badge(copy = "Collected", variant = PaymentIncrementBadgeVariant.GREEN),
                    state = PaymentIncrementState.COLLECTED,
                    paymentIncrementableId = "2",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(30),
                    stateReason = PaymentIncrementStateReason.REQUIRES_ACTION
                ),
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    paymentIncrementBadge = PaymentIncrementFactory.badge(copy = "Collected (adjusted)", variant = PaymentIncrementBadgeVariant.GREEN),
                    state = PaymentIncrementState.COLLECTED,
                    paymentIncrementableId = "2",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(30),
                    stateReason = PaymentIncrementStateReason.REQUIRES_ACTION,
                    refundedAmount = PaymentIncrementFactory.amount(formattedAmount = "$42.00", formattedAmountWithCode = "USD $70.75", amountAsFloat = "70.75", amountAsCents = "7075", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "70.75$"),
                    refundUpdatedAmountInProjectNativeCurrency = "18.00$"
                ),
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    paymentIncrementBadge = PaymentIncrementFactory.badge(copy = "Authentication required", variant = PaymentIncrementBadgeVariant.DANGER),
                    state = PaymentIncrementState.ERRORED,
                    paymentIncrementableId = "3",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(45),
                    stateReason = PaymentIncrementStateReason.REQUIRES_ACTION
                ),
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    paymentIncrementBadge = PaymentIncrementFactory.badge(copy = "Canceled", variant = PaymentIncrementBadgeVariant.GRAY),
                    state = PaymentIncrementState.CANCELLED,
                    paymentIncrementableId = "4",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(60),
                    stateReason = PaymentIncrementStateReason.UNKNOWN__
                ),
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    paymentIncrementBadge = PaymentIncrementFactory.badge(copy = "Errored payment", variant = PaymentIncrementBadgeVariant.DANGER),
                    state = PaymentIncrementState.ERRORED,
                    paymentIncrementableId = "4",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(60),
                    stateReason = PaymentIncrementStateReason.UNKNOWN__
                ),
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    paymentIncrementBadge = PaymentIncrementFactory.badge(copy = "Refunded", variant = PaymentIncrementBadgeVariant.GRAY),
                    state = PaymentIncrementState.REFUNDED,
                    paymentIncrementableId = "4",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(60),
                    stateReason = PaymentIncrementStateReason.UNKNOWN__,
                    refundedAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$")
                ),
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    paymentIncrementBadge = PaymentIncrementFactory.badge(copy = "Dropped", variant = PaymentIncrementBadgeVariant.GRAY),
                    state = PaymentIncrementState.CHARGEBACK_LOST,
                    paymentIncrementableId = "4",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(60),
                    stateReason = PaymentIncrementStateReason.UNKNOWN__,
                ),
            )
        }
    }
}
