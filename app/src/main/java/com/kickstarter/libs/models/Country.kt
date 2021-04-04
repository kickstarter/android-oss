package com.kickstarter.libs.models

enum class Country(
    val countryCode: String,
    val currencyCode: String,
    val currencySymbol: String,
    val minPledge: Int,
    val maxPledge: Int,
    val trailingCode: Boolean
) {
    AT("AT", "EUR", "€", 1, 8_500, false),
    AU("AU", "AUD", "$", 1, 13_000, true),
    BE("BE", "EUR", "€", 1, 8_500, false),
    CA("CA", "CAD", "$", 1, 13_000, true),
    CH("CH", "CHF", "Fr", 1, 9_500, true),
    DE("DE", "EUR", "€", 1, 8_500, false),
    DK("DK", "DKK", "kr", 5, 65_000, true),
    ES("ES", "EUR", "€", 1, 8_500, false),
    FR("FR", "EUR", "€", 1, 8_500, false),
    GB("GB", "GBP", "£", 1, 8_000, false),
    GR("GR", "EUR", "€", 1, 8_500, false),
    HK("HK", "HKD", "$", 10, 75_000, true),
    IE("IE", "EUR", "€", 1, 8_500, false),
    IT("IT", "EUR", "€", 1, 8_500, false),
    JP("JP", "JPY", "¥", 100, 1_200_000, false),
    LU("LU", "EUR", "€", 1, 8_500, false),
    MX("MX", "MXN", "$", 10, 200_000, true),
    NL("NL", "EUR", "€", 1, 8_500, false),
    NO("NO", "NOK", "kr", 5, 80_000, true),
    NZ("NZ", "NZD", "$", 1, 14_000, true),
    PL("PL", "PLN", "zł", 5, 37_250, false),
    SE("SE", "SEK", "kr", 5, 85_000, true),
    SI("SI", "EUR", "€", 1, 8_500, false),
    SG("SG", "SGD", "$", 2, 13_000, true),
    US("US", "USD", "$", 1, 10_000, true);

    companion object {
        @JvmStatic
        fun findByCurrencyCode(currencyCode: String): Country? {
            for (c in values()) {
                if (c.currencyCode == currencyCode) {
                    return c
                }
            }
            return null
        }
    }
}
