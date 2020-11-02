@file:JvmName("ConfigExtension")
package com.kickstarter.libs.utils.extensions

import com.kickstarter.libs.Config
import org.json.JSONArray
/**
 * Helper method to know if a feature flag is enabled
 *
 * @param text string feature flag you are looking for
 * @return true if the feature flag is enable
 *         false if the feature flag is disabled or does not exist
 */
fun Config.isFeatureFlagEnabled(text: String): Boolean {
    val isEnabled = this.features()
            ?.get(text)

    return isEnabled ?: false
}

/**
 * @return The actual list of variants
 */
fun Config.currentVariants(): JSONArray? {
    return this.abExperiments()
            ?.toSortedMap()
            ?.let {
                JSONArray().apply {
                    for (feature in it) {
                        put("${feature.key}[${feature.value}]")
                    }
                }
            }
}

/**
 * @return The actual list of enabled feature flags
 */
fun Config.enabledFeatureFlags(): JSONArray? {
    return this.features()
            ?.filter { it.key.startsWith("android_") && it.value }
            ?.keys
            ?.sorted()
            ?.let {
                JSONArray().apply {
                    for (feature in it) {
                        put(feature)
                    }
                }
            }
}

/**
 * Internal name for "android_email_verification_flow"
 * @{link TODO: Add link to the features.yml on kickstarter repo
 */
const val EMAIL_VERIFICATION = "android_email_verification_flow"