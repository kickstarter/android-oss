package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
data class PaymentIncrement(
    val amount: Money,
    val paymentIncrementableId: String,
    val paymentIncrementableType: String,
    val scheduledCollection: DateTime,
    val state: State,
    val stateReason: String?
) : Parcelable {
    fun amount() = this.amount
    fun paymentIncrementableId() = this.paymentIncrementableId
    fun paymentIncrementableType() = this.paymentIncrementableType
    fun scheduledCollection() = this.scheduledCollection
    fun state() = this.state
    fun stateReason() = this.stateReason

    @Parcelize
    data class Builder(
        private var amount: Money = Money.builder().build(),
        private var paymentIncrementableId: String = "",
        private var paymentIncrementableType: String = "",
        private var scheduledCollection: DateTime = DateTime.now(),
        private var state: State = State.UNKNOWN,
        private var stateReason: String? = null
    ) : Parcelable {
        fun amount(amount: Money) = apply { this.amount = amount }
        fun paymentIncrementableId(paymentIncrementableId: String) = apply { this.paymentIncrementableId = paymentIncrementableId }
        fun paymentIncrementableType(paymentIncrementableType: String) = apply { this.paymentIncrementableType = paymentIncrementableType }
        fun scheduledCollection(scheduledCollection: DateTime) = apply { this.scheduledCollection = scheduledCollection }
        fun state(state: State) = apply { this.state = state }
        fun stateReason(stateReason: String?) = apply { this.stateReason = stateReason }
        fun build() = PaymentIncrement(
            amount = amount,
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
        amount = amount,
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

    enum class State {
        COLLECTED,
        UNATTEMPTED,
        UNKNOWN,
    }
}
