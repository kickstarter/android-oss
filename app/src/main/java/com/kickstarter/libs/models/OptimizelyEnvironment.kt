package com.kickstarter.libs.models

import com.kickstarter.libs.utils.Secrets

enum class OptimizelyEnvironment(private val environmentKey: String, val sdkKey: String) {
    DEVELOPMENT("development", Secrets.Optimizely.DEVELOPMENT),
    PRODUCTION("production", Secrets.Optimizely.PRODUCTION),
    STAGING("staging", Secrets.Optimizely.STAGING)
}
