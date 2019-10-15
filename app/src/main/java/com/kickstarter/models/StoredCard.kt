package com.kickstarter.models

import android.os.Parcelable
import auto.parcel.AutoParcel
import com.kickstarter.R
import com.stripe.android.model.Card
import type.CreditCardTypes
import java.util.*

@AutoParcel
abstract class StoredCard : Parcelable {
    abstract fun id(): String
    abstract fun expiration(): Date
    abstract fun lastFourDigits(): String
    abstract fun type(): CreditCardTypes

    @AutoParcel.Builder
    abstract class Builder {
        abstract fun id(id: String): Builder
        abstract fun lastFourDigits(lastFourDigits: String): Builder
        abstract fun expiration(expiration: Date): Builder
        abstract fun type(creditCardType: CreditCardTypes): Builder
        abstract fun build(): StoredCard
    }

    abstract fun toBuilder(): Builder

    companion object {

        fun builder(): Builder {
            return AutoParcel_StoredCard.Builder()
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
                CreditCardTypes.AMEX -> Card.CardBrand.AMERICAN_EXPRESS
                CreditCardTypes.DINERS -> Card.CardBrand.DINERS_CLUB
                CreditCardTypes.DISCOVER -> Card.CardBrand.DISCOVER
                CreditCardTypes.JCB -> Card.CardBrand.JCB
                CreditCardTypes.MASTERCARD -> Card.CardBrand.MASTERCARD
                CreditCardTypes.UNION_PAY -> Card.CardBrand.UNIONPAY
                CreditCardTypes.VISA -> Card.CardBrand.VISA
                else -> Card.CardBrand.UNKNOWN
            }
        }

        const val DATE_FORMAT = "MM/yyyy"
    }

}
