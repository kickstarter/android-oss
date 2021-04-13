package com.kickstarter.libs.utils

enum class ConfigFeatureName(val configFeatureName: String) {
    /**
     * Internal name for "android_email_verification_flow"
     * @{link https://github.com/kickstarter/kickstarter/blob/d0d07b93379efceb9d3030e5f1591e4a931a85fb/config/features.yml#L209 }
     */
    EMAIL_VERIFICATION_FLOW("android_email_verification_flow"),

    /**
     * Internal name for "android_email_verification_skip"
     * @{link https://github.com/kickstarter/kickstarter/blob/d0d07b93379efceb9d3030e5f1591e4a931a85fb/config/features.yml#L212 }
     */
    EMAIL_VERIFICATION_SKIP("android_email_verification_skip"),

    /**
     * Internal name for "android_segment"
     * @{link https://github.com/kickstarter/kickstarter/blob/e9c61ea9f1e4817bb64560db7c32f3b9704cdc60/config/features.yml#L219}
     */
    SEGMENT_ENABLED("android_segment")
}
