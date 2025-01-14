package com.kickstarter.mock.factories

import com.kickstarter.models.Amount
import com.kickstarter.models.PaymentIncrement
import com.kickstarter.type.CurrencyCode
import org.joda.time.DateTime

class PaymentIncrementFactory {
    companion object {

        fun paymentIncrement(
            amount: Amount,
            paymentIncrementableId: String,
            paymentIncrementableType: String,
            scheduledCollection: DateTime,
            state: PaymentIncrement.State,
            stateReason: String?,
        ): PaymentIncrement {
            return PaymentIncrement.builder()
                .amount(amount)
                .paymentIncrementableId(paymentIncrementableId)
                .paymentIncrementableType(paymentIncrementableType)
                .scheduledCollection(scheduledCollection)
                .state(state)
                .stateReason(stateReason)
                .build()
        }

        fun amount(
            amount: String?,
            currencySymbol: String?,
            currencyCode: CurrencyCode?,
        ): Amount {
            return Amount.builder()
                .amount(amount)
                .currencySymbol(currencySymbol)
                .currencyCode(currencyCode)
                .build()
        }

        fun incrementUsdUncollected(dateTime: DateTime, amount: String): PaymentIncrement {
            return paymentIncrement(
                amount = amount(amount, "$", CurrencyCode.USD),
                scheduledCollection = dateTime,
                paymentIncrementableId = "",
                paymentIncrementableType = "",
                state = PaymentIncrement.State.UNATTEMPTED,
                stateReason = ""
            )
        }

        fun incrementUsdCollected(dateTime: DateTime, amount: String): PaymentIncrement {
            return paymentIncrement(
                amount = amount(amount, "$", CurrencyCode.USD),
                scheduledCollection = dateTime,
                paymentIncrementableId = "",
                paymentIncrementableType = "",
                state = PaymentIncrement.State.COLLECTED,
                stateReason = ""
            )
        }

        fun samplePaymentIncrements(): List<PaymentIncrement> {
            val now = DateTime.now()

            return listOf(
                PaymentIncrementFactory.paymentIncrement(
                    amount = PaymentIncrementFactory.amount("60.00", "$", CurrencyCode.USD),
                    state = PaymentIncrement.State.UNATTEMPTED,
                    paymentIncrementableId = "1",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(15),
                    stateReason = ""
                ),
                PaymentIncrementFactory.paymentIncrement(
                    amount = PaymentIncrementFactory.amount("60.00", "$", CurrencyCode.USD),
                    state = PaymentIncrement.State.COLLECTED,
                    paymentIncrementableId = "2",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(30),
                    stateReason = ""
                ),
                PaymentIncrementFactory.paymentIncrement(
                    amount = PaymentIncrementFactory.amount("60.00", "$", CurrencyCode.USD),
                    state = PaymentIncrement.State.UNATTEMPTED,
                    paymentIncrementableId = "3",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(45),
                    stateReason = ""
                ),
                PaymentIncrementFactory.paymentIncrement(
                    amount = PaymentIncrementFactory.amount("60.00", "$", CurrencyCode.USD),
                    state = PaymentIncrement.State.COLLECTED,
                    paymentIncrementableId = "4",
                    paymentIncrementableType = "pledge",
                    scheduledCollection = now.plusDays(60),
                    stateReason = ""
                )
            )
        }
    }
}
