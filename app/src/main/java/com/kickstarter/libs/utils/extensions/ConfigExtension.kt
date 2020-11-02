package com.kickstarter.libs.utils.extensions

import com.kickstarter.libs.Config

/**
 * Config extension function: it will return a Boolean value
 * telling the actual state for a concrete feature flag
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
 * Internal name for "android_email_verification_flow"
 * @{link TODO: Add link to the features.yml on kickstarter repo
 */
const val EMAIL_VERIFICATION = "android_email_verification_flow"