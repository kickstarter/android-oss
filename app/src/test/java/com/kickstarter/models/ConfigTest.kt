package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Config
import org.junit.Test

class ConfigTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val countryCode = "US"
        val currencyCode = "USD"
        val currencySymbol = "$"
        val US = Config.LaunchedCountry.builder()
            .name(countryCode)
            .currencyCode(currencyCode)
            .currencySymbol(currencySymbol)
            .trailingCode(true)
            .build()

        val launchedCountries = listOf(US)

        val features = mapOf<String, Boolean>()

        val config = Config.builder()
            .countryCode(countryCode)
            .features(features)
            .launchedCountries(launchedCountries)
            .build()

        assertEquals(config.countryCode(), countryCode)
        assertEquals(config.launchedCountries(), launchedCountries)
        assertEquals(config.features(), features)
        assertEquals(config.launchedCountries()[0].name(), countryCode)
        assertEquals(config.launchedCountries()[0].currencyCode(), currencyCode)
        assertEquals(config.launchedCountries()[0].currencySymbol(), currencySymbol)
        assertEquals(config.launchedCountries()[0].trailingCode(), true)
    }

    @Test
    fun testDefaultToBuilderInit() {
        val countryCode = "US"
        val currencyCode = "USD"
        val currencySymbol = "$"
        var US = Config.LaunchedCountry.builder().build()

        US = US.toBuilder().name(countryCode)
            .currencyCode(currencyCode)
            .currencySymbol(currencySymbol)
            .trailingCode(true)
            .build()

        assertEquals(US.name(), countryCode)
        assertEquals(US.currencyCode(), currencyCode)
        assertEquals(US.currencySymbol(), currencySymbol)
        assertEquals(US.trailingCode(), true)

        val launchedCountries = listOf(US)

        val features = mapOf<String, Boolean>()

        var config = Config.builder()
            .build()

        config = config.toBuilder().launchedCountries(launchedCountries).features(features).build()

        assertEquals(config.launchedCountries(), launchedCountries)
        assertEquals(config.features(), features)
    }

    @Test
    fun testConfig_equalFalse() {

        val US = Config.LaunchedCountry.builder()
            .name("US")
            .currencyCode("USD")
            .currencySymbol("$")
            .trailingCode(true)
            .build()
        val GB = Config.LaunchedCountry.builder()
            .name("GB")
            .currencyCode("GBP")
            .currencySymbol("£")
            .trailingCode(false)
            .build()
        val CA = Config.LaunchedCountry.builder()
            .name("CA")
            .currencyCode("CAD")
            .currencySymbol("$")
            .trailingCode(true)
            .build()

        val config = Config.builder().build()
        val config2 = Config.builder().launchedCountries(listOf(US, GB)).build()
        val config3 = Config.builder().launchedCountries(listOf(US, CA)).build()
        val config4 = Config.builder().launchedCountries(listOf(CA, GB)).build()

        assertFalse(config == config2)
        assertFalse(config == config3)
        assertFalse(config == config4)

        assertFalse(config3 == config2)
        assertFalse(config3 == config4)
    }

    @Test
    fun testConfig_equalTrue() {
        val config1 = Config.builder().build()
        val config2 = Config.builder().build()

        assertEquals(config1, config2)
    }
}
