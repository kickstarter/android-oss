package com.kickstarter.libs.utils

import com.kickstarter.libs.Config
import org.json.JSONArray

object ConfigUtils {
    fun currentVariants(config: Config?): JSONArray? {
        return config
                ?.abExperiments()
                ?.toSortedMap()
                ?.let {
                    JSONArray().apply {
                        for (feature in it) {
                            put("${feature.key}[${feature.value}]")
                        }
                    }
                }
    }

    fun enabledFeatureFlags(config: Config?): JSONArray? {
        return config
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
}
