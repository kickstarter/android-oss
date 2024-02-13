package com.kickstarter.services.mutations

import type.CountryCode

data class CreateAddressData(val name: String, val referenceName: String, val addressLine1 : String, val addressLine2: String?, val city : String, val region: String, val postalCode : String, val countryCode: CountryCode, val phoneNumber : String, val clientMutationID: String? = null)