package com.kickstarter.libs.utils

enum class ConfigFeatureFlagName(val featureFlag: String) {

    /**
     * Internal name for "android_segment"
     * @{link https://github.com/kickstarter/kickstarter/blob/e9c61ea9f1e4817bb64560db7c32f3b9704cdc60/config/features.yml#L219}
     */
    SEGMENT_ENABLED("android_segment")
}
