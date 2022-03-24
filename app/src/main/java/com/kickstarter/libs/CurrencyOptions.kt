package com.kickstarter.libs

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class CurrencyOptions internal constructor(
    private val country: String?,
    private val currencyCode: String?,
    private val currencySymbol: String?,
    private val value: Float?
) : Parcelable {

    fun country() = this.country
    fun currencyCode() = this.currencyCode
    fun currencySymbol() = this.currencySymbol
    fun value() = this.value

    @Parcelize
    data class Builder(
        private var country: String? = null,
        private var currencyCode: String? = null,
        private var currencySymbol: String? = null,
        private var value: Float? = null
    ) : Parcelable {
        fun country(country: String?) = apply { this.country = country }
        fun currencyCode(currencyCode: String?) = apply { this.currencyCode = currencyCode }
        fun currencySymbol(currencySymbol: String?) = apply { this.currencySymbol = currencySymbol }
        fun value(value: Float?) = apply { this.value = value }
        fun build() = CurrencyOptions(
            country = country,
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            value = value
        )
    }

    fun toBuilder() = Builder(
        country = country,
        currencyCode = currencyCode,
        currencySymbol = currencySymbol,
        value = value
    )

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is CurrencyOptions) {
            equals = country() == other.country() &&
                currencyCode() == other.currencyCode() &&
                currencySymbol() == other.currencySymbol() &&
                value() == other.value()
        }
        return equals
    }
}
