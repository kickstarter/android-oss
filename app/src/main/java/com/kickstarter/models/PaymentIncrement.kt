package com.kickstarter.models

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class PaymentIncrement(
    val id: Long,
    val amount: Int,
    val state: State,
    val paymentIncrementalType: String,
    val paymentIncrementalId: Long,
    val date: Instant
) {
    val formattedDate: String
        get() {
            val zonedDateTime = ZonedDateTime.ofInstant(date, ZoneOffset.UTC)
            val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")
            return zonedDateTime.format(formatter)
        }
    enum class State {
        UNATTEMPTED,
        COLLECTED
    }

    fun stateAsString(): String {
        return state.name.lowercase()
    }

    companion object {
        fun create(
            id: Long,
            amount: Int,
            state: State,
            paymentIncrementalType: String,
            paymentIncrementalId: Long,
            date: Instant
        ): PaymentIncrement {
            return PaymentIncrement(
                id, amount, state, paymentIncrementalType, paymentIncrementalId, date
            )
        }
    }
}
