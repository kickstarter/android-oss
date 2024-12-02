package com.kickstarter.ui.data

import android.os.Parcelable
import com.kickstarter.type.CreditCardPaymentType
import kotlinx.parcelize.Parcelize

@Parcelize
class CheckoutData private constructor(
    private val amount: Double,
    private val id: Long?,
    private val paymentType: CreditCardPaymentType,
    private val shippingAmount: Double,
    private val bonusAmount: Double?
) : Parcelable {

    fun amount() = this.amount
    fun id() = this.id
    fun paymentType() = this.paymentType
    fun shippingAmount() = this.shippingAmount
    fun bonusAmount() = this.bonusAmount

    @Parcelize
    data class Builder(
        private var id: Long? = null,
        private var amount: Double = 0.0,
        private var paymentType: CreditCardPaymentType = CreditCardPaymentType.UNKNOWN__,
        private var shippingAmount: Double = 0.0,
        private var bonusAmount: Double? = null
    ) : Parcelable {
        fun id(id: Long?) = apply { this.id = id }
        fun amount(amount: Double) = apply { this.amount = amount }
        fun paymentType(paymentType: CreditCardPaymentType) = apply { this.paymentType = paymentType }
        fun shippingAmount(shippingAmount: Double) = apply { this.shippingAmount = shippingAmount }
        fun bonusAmount(bonusAmount: Double?) = apply { this.bonusAmount = bonusAmount }
        fun build() = CheckoutData(
            id = id,
            amount = amount,
            paymentType = paymentType,
            shippingAmount = shippingAmount,
            bonusAmount = bonusAmount
        )
    }

    fun toBuilder() = Builder(
        id = id,
        amount = amount,
        paymentType = paymentType,
        shippingAmount = shippingAmount,
        bonusAmount = bonusAmount
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is CheckoutData) {
            equals = id() == obj.id() &&
                amount() == obj.amount() &&
                paymentType() == obj.paymentType() &&
                shippingAmount() == obj.shippingAmount() &&
                bonusAmount() == obj.bonusAmount()
        }
        return equals
    }
}
