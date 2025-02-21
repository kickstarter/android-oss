package com.kickstarter.mock.factories

import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.PaymentIncrementAmount
import com.kickstarter.type.CurrencyCode
import com.kickstarter.type.PaymentIncrementState
import com.kickstarter.type.PaymentIncrementStateReason
import org.joda.time.DateTime

class PaymentIncrementFactory {
    companion object {

        fun paymentIncrement(
            paymentIncrementAmount: PaymentIncrementAmount,
            paymentIncrementableId: String,
            paymentIncrementableType: String,
            scheduledCollection: DateTime,
            state: PaymentIncrementState,
            stateReason: PaymentIncrementStateReason?,
        ): PaymentIncrement {
            return PaymentIncrement.builder()
                .amount(paymentIncrementAmount)
                .paymentIncrementableId(paymentIncrementableId)
                .paymentIncrementableType(paymentIncrementableType)
                .scheduledCollection(scheduledCollection)
                .state(state)
                .stateReason(stateReason)
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

        fun incrementUsdUncollected(dateTime: DateTime, formattedAmount: String): PaymentIncrement {
            return paymentIncrement(
                paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = formattedAmount, formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
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
                scheduledCollection = dateTime,
                paymentIncrementableId = "",
                paymentIncrementableType = "",
                state = PaymentIncrementState.COLLECTED,
                stateReason = PaymentIncrementStateReason.UNKNOWN__
            )
        }

        fun samplePaymentIncrements(): List<PaymentIncrement> {
            val now = DateTime.now()

            return listOf(
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    state = PaymentIncrementState.UNATTEMPTED,
                    paymentIncrementableId = "1",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(15),
                    stateReason = PaymentIncrementStateReason.REQUIRES_ACTION
                ),
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    state = PaymentIncrementState.COLLECTED,
                    paymentIncrementableId = "2",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(30),
                    stateReason = PaymentIncrementStateReason.REQUIRES_ACTION
                ),
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    state = PaymentIncrementState.ERRORED,
                    paymentIncrementableId = "3",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(45),
                    stateReason = PaymentIncrementStateReason.REQUIRES_ACTION
                ),
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    state = PaymentIncrementState.CANCELLED,
                    paymentIncrementableId = "4",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(60),
                    stateReason = PaymentIncrementStateReason.UNKNOWN__
                ),
                PaymentIncrementFactory.paymentIncrement(
                    paymentIncrementAmount = PaymentIncrementFactory.amount(formattedAmount = "$60.00", formattedAmountWithCode = "USD $99.75", amountAsFloat = "99.75", amountAsCents = "9975", currencyCode = CurrencyCode.USD.rawValue, amountFormattedInProjectNativeCurrency = "99.75$"),
                    state = PaymentIncrementState.ERRORED,
                    paymentIncrementableId = "4",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(60),
                    stateReason = PaymentIncrementStateReason.UNKNOWN__
                )

            )
        }
    }
}
