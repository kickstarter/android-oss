package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.FeatureKey
import com.kickstarter.mock.factories.ConfigFactory
import org.json.JSONArray
import org.junit.Test

class ConfigUtilsTest : KSRobolectricTestCase() {
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
