package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ConfigFactory
import org.json.JSONArray
import org.junit.Test
import java.util.Collections

class ConfigExtensionTest : KSRobolectricTestCase() {

    @Test
    fun isFeatureFlagEnabled_whenFeatureFlagTrue_returnTrue() {
        val config = ConfigFactory.configWithFeaturesEnabled(mapOf(Pair(EMAIL_VERIFICATION_FLOW, true)))
        assertTrue(config.isFeatureFlagEnabled(EMAIL_VERIFICATION_FLOW))
    }

    @Test
    fun isFeatureFlagEnabled_whenFeatureFlagFalse_returnFalse() {
        val config = ConfigFactory.configWithFeaturesEnabled(mapOf(Pair(EMAIL_VERIFICATION_FLOW, false)))
        assertFalse(config.isFeatureFlagEnabled(EMAIL_VERIFICATION_FLOW))
    }

    @Test
    fun isEnabledFeature_whenFeatureFlagEmpty_returnFalse() {
        val config = ConfigFactory.configWithFeaturesEnabled(mapOf(Pair("", true)))
        assertFalse(config.isFeatureFlagEnabled(EMAIL_VERIFICATION_FLOW))
    }

    @Test
    fun testAbExperiments() {
        val configNull = ConfigFactory.config().toBuilder().abExperiments(null).build()
        assertEquals(null, configNull.currentVariants())
    }

    @Test
    fun currentVariants_whenExperimentsEmpty_currentVariantsShouldBeEmpty() {
        val configEmpty = ConfigFactory.configWithExperiments(Collections.emptyMap())
        assertEquals(JSONArray(), configEmpty.currentVariants())
    }

    @Test
    fun currentVarients_whenGivenOneExperiment_shouldReturnOneCurrentVariant() {
        val configWithExperiment = ConfigFactory.configWithExperiment("pledge_button_copy", "experiment")
        assertEquals(JSONArray().apply { put("pledge_button_copy[experiment]") }, configWithExperiment.currentVariants())
    }

    @Test
    fun currentVarients_whenGivenMapOfExperiments_shouldReturnCorrectNumberOfCurrentVariants() {
        val configWithExperiments = ConfigFactory.configWithExperiments(mapOf(Pair("pledge_button_copy", "experiment"),
                Pair("add_new_card_vertical", "control")))

        val correctValue = JSONArray().apply {
            put("add_new_card_vertical[control]")
            put("pledge_button_copy[experiment]")
        }
        assertEquals(correctValue, configWithExperiments.currentVariants())
    }

    @Test
    fun enabledFeatureFlags_whenFeaturesNull_enabledFeatureFlagsShouldBeNull() {
        val configEmptyFeatureFlags = ConfigFactory.config().toBuilder().features(null).build()
        assertEquals(null, configEmptyFeatureFlags.enabledFeatureFlags())
    }

    @Test
    fun enabledFeatureFlags_whenGiveOneFeatureFlag_shouldReturnOneEnabledFeatureFlag() {
        val configOneFeatureFlag = ConfigFactory.configWithFeatureEnabled("ios_native_checkout")
        assertEquals(JSONArray(), configOneFeatureFlag.enabledFeatureFlags())
    }

    @Test
    fun enabledFeatureFlags_whenGivenMapOfFeatureFlags_shouldReturnCorrectNumberOfEnabledFeatureFlags() {
        val configSeveralFeatureFlags = ConfigFactory.configWithFeaturesEnabled(mapOf(Pair("android_native_checkout", false),
                Pair("ios_go_rewardless", true),
                Pair("ios_native_checkout", true)))

        assertEquals(JSONArray(), configSeveralFeatureFlags.enabledFeatureFlags())
    }

    @Test
    fun enabledFeatureFlags_whenGivenTrueFeatureFlag_shouldReturnTrueEnabledFeatureFlag() {
        val configEnableFeatureFlag = ConfigFactory.configWithFeaturesEnabled(mapOf(Pair("android_native_checkout", true),
                Pair("ios_go_rewardless", true),
                Pair("ios_native_checkout", true)))

        assertEquals(JSONArray().apply {
            put("android_native_checkout")
        }, configEnableFeatureFlag.enabledFeatureFlags())
    }
}