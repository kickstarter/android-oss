package com.kickstarter.models

import android.os.Parcelable
import com.kickstarter.R
import com.stripe.android.model.CardBrand
import kotlinx.parcelize.Parcelize
import type.CreditCardTypes
import java.util.Date

@Parcelize
class StoredCard private constructor(
    private val id: String,
    private val expiration: Date,
    private val lastFourDigits: String,
    private val type: CreditCardTypes
) : Parcelable {
    fun id() = this.id
    fun expiration() = this.expiration
    fun lastFourDigits() = this.lastFourDigits
    fun type() = this.type

    @Parcelize
    data class Builder(
        private var id: String = "0L",
        private var lastFourDigits: String = "",
        private var expiration: Date = Date(),
        private var type: CreditCardTypes = CreditCardTypes.`$UNKNOWN`
    ) : Parcelable {
        fun id(id: String) = apply { this.id = id }
        fun lastFourDigits(lastFourDigits: String) = apply { this.lastFourDigits = lastFourDigits }
        fun expiration(expiration: Date) = apply { this.expiration = expiration }
        fun type(type: CreditCardTypes) = apply { this.type = type }
        fun build() = StoredCard(
            id = id,
            lastFourDigits = lastFourDigits,
            expiration = expiration,
            type = type
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is StoredCard) {
            equals = id() == obj.id() &&
                lastFourDigits() == obj.lastFourDigits() &&
                expiration() == obj.expiration() &&
                type() == obj.type()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun toBuilder() = Builder(
        id = id,
        lastFourDigits = lastFourDigits,
        expiration = expiration,
        type = type
    )

    companion object {

        fun builder(): Builder {
            return Builder()
        }

        internal fun getCardTypeDrawable(cardType: CreditCardTypes): Int {
            return when (cardType) {
                CreditCardTypes.AMEX -> R.drawable.amex_md
                CreditCardTypes.DINERS -> R.drawable.diners_md
                CreditCardTypes.DISCOVER -> R.drawable.discover_md
                CreditCardTypes.JCB -> R.drawable.jcb_md
                CreditCardTypes.MASTERCARD -> R.drawable.mastercard_md
                CreditCardTypes.UNION_PAY -> R.drawable.union_pay_md
                CreditCardTypes.VISA -> R.drawable.visa_md
                else -> R.drawable.generic_bank_md
            }
        }

        internal fun issuer(cardType: CreditCardTypes): String {
            return when (cardType) {
                CreditCardTypes.AMEX -> CardBrand.AmericanExpress.code
                CreditCardTypes.DINERS -> CardBrand.DinersClub.code
                CreditCardTypes.DISCOVER -> CardBrand.Discover.code
                CreditCardTypes.JCB -> CardBrand.JCB.code
                CreditCardTypes.MASTERCARD -> CardBrand.MasterCard.code
                CreditCardTypes.UNION_PAY -> CardBrand.UnionPay.code
                CreditCardTypes.VISA -> CardBrand.Visa.code
                else -> CardBrand.Unknown.code
            }
        }

        const val DATE_FORMAT = "MM/yyyy"
    }
}
