package com.kickstarter.mock.factories

import com.kickstarter.features.pledgedprojectsoverview.data.AddressEnvelope
import type.CountryCode

class AddressEnvelopeFactory private constructor() {
    companion object {

        fun addressEnvelope(
            addressLine1: String? = null,
            addressLine2: String? = null,
            city: String? = null,
            countryCode: CountryCode? = null,
            postalCode: String? = null,
            id: String? = null,
        ): AddressEnvelope {
            return AddressEnvelope.builder()
                .addressLine1(addressLine1)
                .addressLine2(addressLine2)
                .city(city)
                .countryCode(countryCode)
                .postalCode(postalCode)
                .id(id)
                .build()
        }

        fun usaAddress(): AddressEnvelope {
            return addressEnvelope(
                addressLine1 = "1234 Maple Lane",
                addressLine2 = "",
                city = "Pleasantville",
                countryCode = CountryCode.US,
                postalCode = "12345",
                id = "1234567890",
            )
        }
    }
}
