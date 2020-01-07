package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.FeatureKey
import com.kickstarter.mock.factories.ConfigFactory
import org.json.JSONArray
import org.junit.Test
import java.util.*

class ConfigUtilsTest : KSRobolectricTestCase() {
    @Test
    fun testAbExperiments() {
        assertEquals(null, ConfigUtils.currentVariants(ConfigFactory.config().toBuilder().abExperiments(null).build()))

        assertEquals(JSONArray(), ConfigUtils.currentVariants(ConfigFactory.configWithExperiments(Collections.emptyMap())))

        assertEquals(JSONArray().apply { put("pledge_button_copy[experiment]") },
                ConfigUtils.currentVariants(ConfigFactory.configWithExperiment("pledge_button_copy", "experiment")))

        assertEquals(JSONArray().apply {
            put("add_new_card_vertical[control]")
            put("pledge_button_copy[experiment]")
        },
                ConfigUtils.currentVariants(ConfigFactory.configWithExperiments(mapOf(Pair("pledge_button_copy", "experiment"),
                        Pair("add_new_card_vertical", "control")))))
    }

    @Test
    fun testEnabledFeatureFlags() {
        assertEquals(null, ConfigUtils.enabledFeatureFlags(ConfigFactory.config().toBuilder().features(null).build()))

        assertEquals(JSONArray(), ConfigUtils.enabledFeatureFlags(ConfigFactory.configWithFeatureEnabled("ios_native_checkout")))

        assertEquals(JSONArray().apply { put("android_go_rewardless_2") },
                ConfigUtils.enabledFeatureFlags(ConfigFactory.configWithFeatureEnabled(FeatureKey.ANDROID_GO_REWARDLESS)))

        assertEquals(JSONArray().apply {
            put("android_go_rewardless_2")
            put("android_native_checkout")
        }, ConfigUtils.enabledFeatureFlags(ConfigFactory.configWithFeaturesEnabled(mapOf(Pair(FeatureKey.ANDROID_GO_REWARDLESS, true),
                Pair(FeatureKey.ANDROID_NATIVE_CHECKOUT, true),
                Pair("ios_go_rewardless", true),
                Pair("ios_native_checkout", true)))))

        assertEquals(JSONArray().apply { put("android_native_checkout") },
                ConfigUtils.enabledFeatureFlags(ConfigFactory.configWithFeaturesEnabled(mapOf(Pair(FeatureKey.ANDROID_GO_REWARDLESS, false),
                        Pair(FeatureKey.ANDROID_NATIVE_CHECKOUT, true),
                        Pair("ios_go_rewardless", true),
                        Pair("ios_native_checkout", true)))))
    }
}
