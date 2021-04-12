package com.kickstarter.model

data class FeatureFlagsModel(
    val featureFlagsName: String,
    val isFeatureFlagEnabled: Boolean,
    val isFeatureFlagChangeable: Boolean = false
)
