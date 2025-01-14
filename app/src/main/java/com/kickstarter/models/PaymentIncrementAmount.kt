package com.kickstarter.models

import android.os.Parcelable
import com.kickstarter.type.CurrencyCode
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentIncrementAmount(
    val amountAsCents: String?,
    val amountAsFloat: String?,
    val formattedAmount: String?,
    val formattedAmountWithCode: String?,
    val currencyCode: String?,
) : Parcelable {
    fun amountAsCents() = this.amountAsCents
    fun amountAsFloat() = this.amountAsFloat
    fun formattedAmount() = this.formattedAmount
    fun formattedAmountWithCode() = this.formattedAmountWithCode
    fun currencyCode() = this.currencyCode

    @Parcelize
    data class Builder(
        var amountAsCents: String? = null,
        var amountAsFloat: String? = null,
        var formattedAmount: String? = null,
        var formattedAmountWithCode: String? = null,
        var currencyCode: String? = null,

    ) : Parcelable {
        fun amountAsCents(amountAsCents: String?) = apply { this.amountAsCents = amountAsCents }
        fun amountAsFloat(amountAsFloat: String?) = apply { this.amountAsFloat = amountAsFloat }
        fun formattedAmount(formattedAmount: String?) = apply { this.formattedAmount = formattedAmount }
        fun formattedAmountWithCode(formattedAmountWithCode: String?) = apply { this.formattedAmountWithCode = formattedAmountWithCode }
        fun currencyCode(currencyCode: String?) = apply { this.currencyCode = currencyCode }
        fun build() = PaymentIncrementAmount(
            amountAsCents = amountAsCents,
            amountAsFloat = amountAsFloat,
            formattedAmount = formattedAmount,
            formattedAmountWithCode = formattedAmountWithCode,
            currencyCode = currencyCode,
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is PaymentIncrementAmount) {
            equals = amountAsCents() == obj.amountAsCents() &&
                    amountAsFloat() == obj.amountAsFloat() &&
                    formattedAmount() == obj.formattedAmount() &&
                    formattedAmountWithCode() == obj.formattedAmountWithCode() &&
                currencyCode() == obj.currencyCode()
        }
        return equals
    }

    fun toBuilder() = Builder(
        amountAsCents = amountAsCents,
        amountAsFloat = amountAsFloat,
        formattedAmount = formattedAmount,
        formattedAmountWithCode = formattedAmountWithCode,
        currencyCode = currencyCode,
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
