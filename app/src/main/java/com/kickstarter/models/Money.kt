package com.kickstarter.models

import android.os.Parcelable
import com.kickstarter.type.CurrencyCode
import kotlinx.parcelize.Parcelize

@Parcelize
data class Money(
    val amount: String?,
    val currencyCode: CurrencyCode?,
    val currencySymbol: String?,
) : Parcelable {
    fun amount() = this.amount
    fun currencyCode() = this.currencyCode
    fun currencySymbol() = this.currencySymbol

    @Parcelize
    data class Builder(
        var amount: String? = null,
        var currencyCode: CurrencyCode? = null,
        var currencySymbol: String? = null,

    ) : Parcelable {
        fun amount(amount: String?) = apply { this.amount = amount }
        fun currencyCode(currencyCode: CurrencyCode?) = apply { this.currencyCode = currencyCode }
        fun currencySymbol(currencySymbol: String?) = apply { this.currencySymbol = currencySymbol }
        fun build() = Money(
            amount = amount,
            currencyCode = currencyCode,
            currencySymbol = currencySymbol
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is Money) {
            equals = amount() == obj.amount() &&
                currencyCode() == obj.currencyCode() &&
                currencySymbol() == obj.currencySymbol()
        }
        return equals
    }

    fun toBuilder() = Builder(
        amount = amount,
        currencyCode = currencyCode,
        currencySymbol = currencySymbol,
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
