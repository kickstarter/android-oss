package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentPlan(
    val amountIsPledgeOverTimeEligible: Boolean,
    val paymentIncrements: List<PaymentIncrement>?,
    val projectIsPledgeOverTimeAllowed: Boolean,
) : Parcelable {
    fun amountIsPledgeOverTimeEligible() = this.amountIsPledgeOverTimeEligible
    fun paymentIncrements() = this.paymentIncrements
    fun projectIsPledgeOverTimeAllowed() = this.projectIsPledgeOverTimeAllowed

    @Parcelize
    data class Builder(
        var amountIsPledgeOverTimeEligible: Boolean = false,
        var paymentIncrements: List<PaymentIncrement>? = null,
        var projectIsPledgeOverTimeAllowed: Boolean = false,
    ) : Parcelable {
        fun amountIsPledgeOverTimeEligible(amountIsPledgeOverTimeEligible: Boolean) = apply { this.amountIsPledgeOverTimeEligible = amountIsPledgeOverTimeEligible }
        fun paymentIncrements(paymentIncrements: List<PaymentIncrement>?) = apply { this.paymentIncrements = paymentIncrements }
        fun projectIsPledgeOverTimeAllowed(projectIsPledgeOverTimeAllowed: Boolean) = apply { this.projectIsPledgeOverTimeAllowed = projectIsPledgeOverTimeAllowed }
        fun build() = PaymentPlan(
            amountIsPledgeOverTimeEligible = amountIsPledgeOverTimeEligible,
            paymentIncrements = paymentIncrements,
            projectIsPledgeOverTimeAllowed = projectIsPledgeOverTimeAllowed,
        )
    }

    companion object {
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        amountIsPledgeOverTimeEligible = amountIsPledgeOverTimeEligible,
        paymentIncrements = paymentIncrements,
        projectIsPledgeOverTimeAllowed = projectIsPledgeOverTimeAllowed,
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is PaymentPlan) {
            equals = amountIsPledgeOverTimeEligible() == other.amountIsPledgeOverTimeEligible() &&
                paymentIncrements() == other.paymentIncrements() &&
                projectIsPledgeOverTimeAllowed() == other.projectIsPledgeOverTimeAllowed()
        }
        return equals
    }
}
