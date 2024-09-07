package com.kickstarter.features.pledgedprojectsoverview.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class DeliveryAddress private constructor(
    val addressID: String?,
    val addressLine1: String?,
    val addressLine2: String?,
    val city: String?,
    val region: String?,
    val postalCode: String?,
    val countryCode: String?,
    val phoneNumber: String?,
    val recipientName: String?
) : Parcelable {

    fun addressId() = this.addressID
    fun addressLine1() = this.addressLine1
    fun addressLine2() = this.addressLine2
    fun city() = this.city
    fun region() = this.region
    fun postalCode() = this.postalCode
    fun countryCode() = this.countryCode
    fun phoneNumber() = this.phoneNumber
    fun recipientName() = this.recipientName

    @Parcelize
    data class Builder(
        var addressID: String? = null,
        var addressLine1: String? = null,
        var addressLine2: String? = null,
        var city: String? = null,
        var region: String? = null,
        var postalCode: String? = null,
        var countryCode: String? = null,
        var phoneNumber: String? = null,
        var recipientName: String? = null
    ) : Parcelable {

        fun addressId(addressID: String?) = apply { this.addressID = addressID }
        fun addressLine1(addressLine1: String?) = apply { this.addressLine1 = addressLine1 }
        fun addressLine2(addressLine2: String?) = apply { this.addressLine2 = addressLine2 }
        fun city(city: String?) = apply { this.city = city }
        fun region(region: String?) = apply { this.region = region }
        fun postalCode(postalCode: String?) = apply { this.postalCode = postalCode }
        fun countryCode(countryCode: String?) = apply { this.countryCode = countryCode }
        fun phoneNumber(phoneNumber: String?) = apply { this.phoneNumber = phoneNumber }
        fun recipientName(recipientName: String?) = apply { this.recipientName = recipientName }

        fun build() = DeliveryAddress(
            addressID = addressID,
            addressLine1 = addressLine1,
            addressLine2 = addressLine2,
            city = city,
            region = region,
            postalCode = postalCode,
            countryCode = countryCode,
            phoneNumber = phoneNumber,
            recipientName = recipientName
        )
    }

    fun toBuilder() = Builder(
        addressID = addressID,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2,
        city = city,
        region = region,
        postalCode = postalCode,
        countryCode = countryCode,
        phoneNumber = phoneNumber,
        recipientName = recipientName
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is DeliveryAddress) {
            equals = addressId() == other.addressId() &&
                addressLine1() == other.addressLine1() &&
                addressLine2() == other.addressLine2() &&
                city() == other.city() &&
                region() == other.region() &&
                postalCode() == other.postalCode() &&
                countryCode() == other.countryCode() &&
                phoneNumber() == other.phoneNumber() &&
                recipientName() == other.recipientName()
        }
        return equals
    }

    /* Formats the address to be:
        recipientName
        addressLine1
        addressLine2 (optional)
        city, region postalCode
        countryCode (optional)
        phoneNumber (optional)
     */
    fun getFormattedAddress(): String {
        return "${recipientName ?: ""}\n${addressLine1 ?: ""}\n" + if (!addressLine2.isNullOrEmpty()) "${addressLine2}\n" else "" + "${city ?: ""}, ${region ?: ""} ${postalCode ?: ""}\n" + if (!countryCode.isNullOrEmpty()) "${countryCode}\n" else "" + if (!phoneNumber.isNullOrEmpty()) "$phoneNumber" else ""
    }
}
