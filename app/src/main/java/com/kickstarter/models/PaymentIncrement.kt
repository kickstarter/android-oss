package com.kickstarter.models

import android.os.Parcelable
import com.kickstarter.type.PaymentIncrementState
import com.kickstarter.type.PaymentIncrementStateReason
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
data class PaymentIncrement(
    val paymentIncrementAmount: PaymentIncrementAmount,
    val paymentIncrementableId: String,
    val paymentIncrementableType: String,
    val scheduledCollection: DateTime,
    val state: PaymentIncrementState,
    val stateReason: PaymentIncrementStateReason?
) : Parcelable {
    fun amount() = this.paymentIncrementAmount
    fun paymentIncrementableId() = this.paymentIncrementableId
    fun paymentIncrementableType() = this.paymentIncrementableType
    fun scheduledCollection() = this.scheduledCollection
    fun state() = this.state
    fun stateReason() = this.stateReason

    @Parcelize
    data class Builder(
        private var paymentIncrementAmount: PaymentIncrementAmount = PaymentIncrementAmount.builder()
            .build(),
        private var paymentIncrementableId: String = "",
        private var paymentIncrementableType: String = "",
        private var scheduledCollection: DateTime = DateTime.now(),
        private var state: PaymentIncrementState = PaymentIncrementState.UNKNOWN__,
        private var stateReason: PaymentIncrementStateReason = PaymentIncrementStateReason.UNKNOWN__,
    ) : Parcelable {
        fun amount(paymentIncrementAmount: PaymentIncrementAmount) =
            apply { this.paymentIncrementAmount = paymentIncrementAmount }

        fun paymentIncrementableId(paymentIncrementableId: String) =
            apply { this.paymentIncrementableId = paymentIncrementableId }

        fun paymentIncrementableType(paymentIncrementableType: String) =
            apply { this.paymentIncrementableType = paymentIncrementableType }

        fun scheduledCollection(scheduledCollection: DateTime) =
            apply { this.scheduledCollection = scheduledCollection }

        fun state(state: PaymentIncrementState) = apply { this.state = state }
        fun stateReason(stateReason: PaymentIncrementStateReason?) = apply {
            if (stateReason != null) {
                this.stateReason = stateReason
            }
        }

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

    fun toBuilder() = stateReason?.let {
        Builder(
            paymentIncrementAmount = paymentIncrementAmount,
            paymentIncrementableId = paymentIncrementableId,
            paymentIncrementableType = paymentIncrementableType,
            scheduledCollection = scheduledCollection,
            state = state,
            stateReason = stateReason,
        )
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
