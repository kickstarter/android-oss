@file:JvmName("ConfigExtension")
package com.kickstarter.libs.utils.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kickstarter.libs.Config
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.utils.ConfigFeatureName
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
        .features()
        ?.get(text)

    return isEnabled ?: false
}

/**
 * @return The actual list of variants
 */
fun Config.currentVariants(): Array<String>? {
    return this
        .abExperiments()
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
        .features()
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
 * set the saved feature flags in to config feature object
 */

fun Config.syncUserFeatureFlagsFromPref(featuresFlagPreference: StringPreferenceType) {
    val featuresFlagsMap = Gson().fromJson<Map<String?, Boolean?>>(
        featuresFlagPreference.get(), object : TypeToken<HashMap<String?, Boolean?>>() {}.type
    )

    featuresFlagsMap[ConfigFeatureName.SEGMENT_ENABLED.configFeatureName]?.let {
        this.features()?.put(ConfigFeatureName.SEGMENT_ENABLED.configFeatureName, it)
    }
}

/**
 * set the saved feature flags in to config feature object
 */

fun Config.setUserFeatureFlagsPrefWithFeatureFlag(
    featuresFlagPreference: StringPreferenceType?,
    featureName: String,
    isEnabled: Boolean
) {
    featuresFlagPreference?.let {
        val jsonString = it.get()
        val featuresFlagsMap = if (jsonString.isNullOrEmpty()) {
            mutableMapOf()
        } else {
            Gson().fromJson<Map<String?, Boolean?>>(
                it.get(), object : TypeToken<HashMap<String?, Boolean?>>() {}.type
            ).toMutableMap()
        }
        featuresFlagsMap[featureName] = isEnabled
        it.set(Gson().toJson(featuresFlagsMap).toString())
    }

    this.features()?.set(featureName, isEnabled)
}
