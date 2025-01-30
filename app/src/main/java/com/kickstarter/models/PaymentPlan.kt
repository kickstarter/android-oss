package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentPlan(
    val amountIsPledgeOverTimeEligible: Boolean,
    val paymentIncrements: List<PaymentIncrement>?,
) : Parcelable {
    fun amountIsPledgeOverTimeEligible() = this.amountIsPledgeOverTimeEligible
    fun paymentIncrements() = this.paymentIncrements

    @Parcelize
    data class Builder(
        var amountIsPledgeOverTimeEligible: Boolean = false,
        var paymentIncrements: List<PaymentIncrement>? = null,
    ) : Parcelable {
        fun amountIsPledgeOverTimeEligible(amountIsPledgeOverTimeEligible: Boolean) = apply { this.amountIsPledgeOverTimeEligible = amountIsPledgeOverTimeEligible }
        fun paymentIncrements(paymentIncrements: List<PaymentIncrement>?) = apply { this.paymentIncrements = paymentIncrements }
        fun build() = PaymentPlan(
            amountIsPledgeOverTimeEligible = amountIsPledgeOverTimeEligible,
            paymentIncrements = paymentIncrements,
        )
    }

    companion object {
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        amountIsPledgeOverTimeEligible = amountIsPledgeOverTimeEligible,
        paymentIncrements = paymentIncrements,
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is PaymentPlan) {
            equals = amountIsPledgeOverTimeEligible() == other.amountIsPledgeOverTimeEligible() &&
                paymentIncrements() == other.paymentIncrements()
        }
        return equals
    }
}
