@file:JvmName("ConfigExtension")
package com.kickstarter.libs.utils.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kickstarter.libs.Config
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.models.ShippingRule
import org.json.JSONArray

/**
 * Helper method to know if a feature flag is enabled
 *
 * @param text string feature flag you are looking for
 * @return true if the feature flag is enable
 *         false if the feature flag is disabled
 *         false if the feature flag does not exist
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
}

/**
 * set the saved feature flags in to config feature object
 */
fun Config.setUserFeatureFlagsPrefWithFeatureFlag(
    featuresFlagPreference: StringPreferenceType?,
    featureName: String,
    isEnabled: Boolean
): Config {
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

    return this.toBuilder().features(
        features()?.toMutableMap()?.apply {
            set(featureName, isEnabled)
        }?.toMap()
    ).build()
}

/**
 * From a selected list of shipping rules, select the one that matches the config location
 * if none matches return the first one.
 * Config countryCode is based on IP location,
 * example: if your network is within Canada, it will return Canada
 * example: if your network is within Canada, but the given shipping Rules does not include
 * Canada, it will return the first rule given in tha shipping rules list.
 */
fun Config.getDefaultLocationFrom(shippingRules: List<ShippingRule>): ShippingRule {
    return if (shippingRules.isNotEmpty()) {
        shippingRules.firstOrNull { it.location()?.country() == this.countryCode() }
            ?: shippingRules.first()
    } else {
        ShippingRule.builder().build()
    }
//    val location = Location.builder()
//        .id(23424814)
//        .country("FK")
//        .displayableName("Falkland Islands")
//        .name("Falkland Islands")
//        .build()
//    return ShippingRule.builder()
//        .id(23424814)
//        .location(location)
//        .build()
}
