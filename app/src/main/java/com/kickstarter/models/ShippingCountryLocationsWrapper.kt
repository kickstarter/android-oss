package com.kickstarter.models

data class ShippingCountryLocationsWrapper(
    val shippingCountryLocations: List<Location> = emptyList(),
    val shippableCountriesForProject: List<Location>? = null
)
