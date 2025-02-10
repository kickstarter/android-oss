package com.kickstarter.mock.factories

import com.kickstarter.models.PaymentIncrement
import com.kickstarter.models.PaymentPlan
import com.kickstarter.models.PaymentPlan.Companion.builder

class PaymentPlanFactory {
    companion object {

        fun paymentPlan(
            paymentIncrements: List<PaymentIncrement>?,
            amountIsPledgeOverTimeEligible: Boolean,
        ): PaymentPlan {
            return builder()
                .paymentIncrements(paymentIncrements)
                .amountIsPledgeOverTimeEligible(amountIsPledgeOverTimeEligible)
                .build()
        }

        fun eligibleAllowedPaymentPlan(paymentIncrements: List<PaymentIncrement>): PaymentPlan {
            return this.paymentPlan(
                paymentIncrements = paymentIncrements,
                amountIsPledgeOverTimeEligible = true,
            )
        }

        fun ineligibleAllowedPaymentPlan(): PaymentPlan {
            return this.paymentPlan(
                paymentIncrements = null,
                amountIsPledgeOverTimeEligible = false,
            )
        }
    }
}
