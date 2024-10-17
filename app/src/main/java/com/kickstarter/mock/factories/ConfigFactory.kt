package com.kickstarter.mock.factories

import com.kickstarter.libs.Config
import com.kickstarter.libs.Config.LaunchedCountry

object ConfigFactory {
    @JvmStatic
    fun config(): Config {
        val US = LaunchedCountry.builder()
            .name("US")
            .currencyCode("USD")
            .currencySymbol("$")
            .trailingCode(true)
            .build()
        val GB = LaunchedCountry.builder()
            .name("GB")
            .currencyCode("GBP")
            .currencySymbol("£")
            .trailingCode(false)
            .build()
        val CA = LaunchedCountry.builder()
            .name("CA")
            .currencyCode("CAD")
            .currencySymbol("$")
            .trailingCode(true)
            .build()
        val JP = LaunchedCountry.builder()
            .name("JP")
            .currencyCode("JPY")
            .currencySymbol("¥")
            .trailingCode(false)
            .build()
        return Config.builder()
            .countryCode("US")
            .features(mutableMapOf())
            .launchedCountries(listOf(US, GB, CA, JP))
            .build()
    }

    fun configForUSUser(): Config {
        return config()
            .toBuilder()
            .countryCode("US")
            .build()
    }

    fun configForCA(): Config {
        return config().toBuilder()
            .countryCode("CA")
            .build()
    }

    fun configForITUser(): Config {
        return config().toBuilder()
            .countryCode("IT")
            .build()
    }

    fun configWithExperiment(experiment: String, variant: String): Config {
        return config().toBuilder()
            .abExperiments(
                mutableMapOf<String, String>().apply {
                    this[experiment] = variant
                }
            )
            .build()
    }

    fun configWithExperiments(abExperiments: Map<String, String>): Config {
        return config().toBuilder()
            .abExperiments(abExperiments)
            .build()
    }

    fun configWithFeatureEnabled(featureKey: String): Config {
        return config().toBuilder()
            .features(
                mutableMapOf<String, Boolean>().apply {
                    this[featureKey] = true
                }
            )
            .build()
    }

    fun configWithFeatureDisabled(featureKey: String): Config {
        return config().toBuilder()
            .features(
                mutableMapOf<String, Boolean>().apply {
                    this[featureKey] = false
                }
            )
            .build()
    }

    fun configWithFeaturesEnabled(features: Map<String, Boolean>): Config {
        return config().toBuilder()
            .features(features.toMutableMap())
            .build()
    }
}
