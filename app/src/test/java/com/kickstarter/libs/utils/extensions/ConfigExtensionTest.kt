package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ConfigFactory
import org.junit.Test

class ConfigExtensionTest : KSRobolectricTestCase() {

    @Test
    fun testIsEnabledFeature_true() {
        val config = ConfigFactory.configWithFeaturesEnabled(mapOf(Pair(EMAIL_VERIFICATION, true)))
        assertTrue(config.isFeatureFlagEnabled(EMAIL_VERIFICATION))
    }

    @Test
    fun testIsEnabledFeature_false() {
        val config = ConfigFactory.configWithFeaturesEnabled(mapOf(Pair(EMAIL_VERIFICATION, false)))
        assertFalse(config.isFeatureFlagEnabled(EMAIL_VERIFICATION))
    }

    @Test
    fun testIsEnabledFeature_NotExistFeatureFlag() {
        val config = ConfigFactory.configWithFeaturesEnabled(mapOf(Pair("", true)))
        assertFalse(config.isFeatureFlagEnabled(EMAIL_VERIFICATION))
    }
}