package com.kickstarter.libs.utils

import com.kickstarter.libs.Config
import org.json.JSONArray

object ConfigUtils {
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
