package com.kickstarter.mock

import android.app.Activity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.featureflag.FlipperFlagKey
import com.kickstarter.models.User

open class MockFeatureFlagClient : FeatureFlagClientType {
    override fun initialize(config: FirebaseRemoteConfig?) {}

    override fun isBackendEnabledFlag(key: FlipperFlagKey): Boolean = false

    override fun setCurrentUser(currentUser: User) {}

    override fun fetch(context: Activity) {}

    override fun activate(context: Activity) {}

    override fun fetchAndActivate(context: Activity) {}

    override fun getBoolean(FlagKey: FlagKey) = false

    override fun getDouble(FlagKey: FlagKey) = 0.0

    override fun getLong(FlagKey: FlagKey) = 0L

    override fun getString(FlagKey: FlagKey) = ""
}
