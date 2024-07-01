package com.kickstarter.features.pledgedprojectsoverview.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import type.CountryCode


@Parcelize
class AddressEnvelope private constructor(
    val addressLine1: String?,
    val addressLine2: String?,
    val city: String?,
    val countryCode: CountryCode?,
    val postalCode: String?,
    val id: String?,

) : Parcelable {

    fun addressLine1() = this.addressLine1
    fun addressLine2() = this.addressLine2
    fun city() = this.city
    fun countryCode() = this.countryCode
    fun postalCode() = this.postalCode
    fun id() = this.id

    @Parcelize
    data class Builder(
        var addressLine1: String? = null,
        var addressLine2: String? = null,
        var city: String? = null,
        var countryCode: CountryCode? = null,
        var postalCode: String? = null,
        var id: String? = null,
    ) : Parcelable {

        fun addressLine1(addressLine1 : String?) = apply { this.addressLine1 = addressLine1 }
        fun addressLine2(addressLine2 : String?) = apply { this.addressLine2 = addressLine2 }
        fun city(city : String?) = apply { this.city = city }
        fun countryCode(countryCode : CountryCode?) = apply { this.countryCode = countryCode }
        fun postalCode(postalCode : String?) = apply { this.postalCode = postalCode }
        fun id(id : String?) = apply { this.id = id }

        fun build() = AddressEnvelope(
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            city = city,
            countryCode = countryCode,
            postalCode = postalCode,
            id = id
        )
    }

    fun toBuilder() = Builder(
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        city = city,
        countryCode = countryCode,
        postalCode = postalCode,
        id = id    )

    companion object {
        @JvmStatic
        fun builder() =  Builder()
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is AddressEnvelope) {
            equals =  addressLine1() == other.addressLine1() &&
                    addressLine2() == other.addressLine2() &&
                    city() == other.city() &&
                    countryCode() == other.countryCode() &&
                    postalCode() == other.postalCode() &&
                    id() == other.id()
        }
        return equals
    }

}