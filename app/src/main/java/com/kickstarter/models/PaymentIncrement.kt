package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
data class PaymentIncrement(
    val paymentIncrementAmount: PaymentIncrementAmount,
    val paymentIncrementableId: String,
    val paymentIncrementableType: String,
    val scheduledCollection: DateTime,
    val state: State,
    val stateReason: String?
) : Parcelable {
    fun amount() = this.paymentIncrementAmount
    fun paymentIncrementableId() = this.paymentIncrementableId
    fun paymentIncrementableType() = this.paymentIncrementableType
    fun scheduledCollection() = this.scheduledCollection
    fun state() = this.state
    fun stateReason() = this.stateReason

    @Parcelize
    data class Builder(
        private var paymentIncrementAmount: PaymentIncrementAmount = PaymentIncrementAmount.builder().build(),
        private var paymentIncrementableId: String = "",
        private var paymentIncrementableType: String = "",
        private var scheduledCollection: DateTime = DateTime.now(),
        private var state: State = State.UNKNOWN,
        private var stateReason: String? = null
    ) : Parcelable {
        fun amount(paymentIncrementAmount: PaymentIncrementAmount) = apply { this.paymentIncrementAmount = paymentIncrementAmount }
        fun paymentIncrementableId(paymentIncrementableId: String) = apply { this.paymentIncrementableId = paymentIncrementableId }
        fun paymentIncrementableType(paymentIncrementableType: String) = apply { this.paymentIncrementableType = paymentIncrementableType }
        fun scheduledCollection(scheduledCollection: DateTime) = apply { this.scheduledCollection = scheduledCollection }
        fun state(state: State) = apply { this.state = state }
        fun stateReason(stateReason: String?) = apply { this.stateReason = stateReason }
        fun build() = PaymentIncrement(
            paymentIncrementAmount = paymentIncrementAmount,
            paymentIncrementableId = paymentIncrementableId,
            paymentIncrementableType = paymentIncrementableType,
            scheduledCollection = scheduledCollection,
            state = state,
            stateReason = stateReason
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is PaymentIncrement) {
            equals = amount() == obj.amount() &&
                paymentIncrementableId() == obj.paymentIncrementableId() &&
                paymentIncrementableType() == obj.paymentIncrementableType() &&
                scheduledCollection() == obj.scheduledCollection() &&
                state() == obj.state() &&
                stateReason() == obj.stateReason()
        }
        return equals
    }

    fun toBuilder() = Builder(
        paymentIncrementAmount = paymentIncrementAmount,
        paymentIncrementableId = paymentIncrementableId,
        paymentIncrementableType = paymentIncrementableType,
        scheduledCollection = scheduledCollection,
        state = state,
        stateReason = stateReason,
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    enum class State(val rawValue: String) {
        COLLECTED("collected"),
        UNATTEMPTED("unattempted"),
        UNKNOWN("unknown");

        companion object {
            fun fromRawValue(value: String): State {
                // Return the matched state or UNKNOWN for unrecognized values
                return values().find { it.rawValue == value } ?: UNKNOWN
            }
        }
    }
}
