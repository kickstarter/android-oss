package com.kickstarter.viewmodels.usecases

import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.models.User

/**
 * Use case meant to check if a [given] feature flag is current state.
 * - When user provided the feature flag will be able to de filtered
 * - by audiences attached to any of the ExperimentData properties.
 * - IE: user_ID or user_logged_in
 */
class FeatureFlagStateUseCase(
    private val optimizely: ExperimentsClientType,
    private val currentUser: User?,
    private val key: OptimizelyFeature.Key
) {

    fun isActive(): Boolean {
        return currentUser?.let {
            this.optimizely.isFeatureEnabled(key, ExperimentData(it))
        } ?: this.optimizely.isFeatureEnabled(key)
    }
}
