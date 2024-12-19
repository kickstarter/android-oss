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
    }
}
