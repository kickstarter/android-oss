package com.kickstarter.models

import android.os.Parcelable
import com.stripe.android.model.CardBrand
import kotlinx.parcelize.Parcelize
import type.CreditCardTypes
import java.util.Date

@Parcelize
class StoredCard private constructor(
    private val id: String?,
    private val expiration: Date?,
    private val lastFourDigits: String?,
    private val type: CreditCardTypes?,
    private val resourceId: Int?
) : Parcelable {
    fun id() = this.id
    fun expiration() = this.expiration
    fun lastFourDigits() = this.lastFourDigits
    fun type() = this.type
    fun resourceId() = this.resourceId

    @Parcelize
    data class Builder(
        private var id: String? = "0L",
        private var lastFourDigits: String? = "",
        private var expiration: Date? = null,
        private var type: CreditCardTypes? = CreditCardTypes.`$UNKNOWN`,
        private var resourceId: Int? = null
    ) : Parcelable {
        fun id(id: String?) = apply { this.id = id }
        fun lastFourDigits(lastFourDigits: String?) = apply { this.lastFourDigits = lastFourDigits }
        fun expiration(expiration: Date?) = apply { this.expiration = expiration }
        fun type(type: CreditCardTypes?) = apply { this.type = type }
        fun resourceId(resourceId: Int?) = apply { this.resourceId = resourceId }
        fun build() = StoredCard(
            id = id,
            lastFourDigits = lastFourDigits,
            expiration = expiration,
            type = type,
            resourceId = resourceId
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is StoredCard) {
            equals = id() == obj.id() &&
                lastFourDigits() == obj.lastFourDigits() &&
                expiration() == obj.expiration() &&
                type() == obj.type() &&
                resourceId() == obj.resourceId()
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
