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
    val isEnabled = this
        ?.features()
        ?.get(text)

    return isEnabled ?: false
}

/**
 * @return The actual list of variants
 */
fun Config.currentVariants(): Array<String>? {
    return this
        ?.abExperiments()
        ?.toSortedMap()
        ?.let {
            mutableListOf<String>().apply {
                for (feature in it) {
                    add("${feature.key}[${feature.value}]")
                }
            }
        }
        ?.toTypedArray()
}

/**
 * @return The actual list of enabled feature flags
 */
fun Config.enabledFeatureFlags(): JSONArray? {
    return this
        ?.features()
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