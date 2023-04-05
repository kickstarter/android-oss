package com.kickstarter.mock

import android.app.Activity
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.FlagKey

open class MockFeatureFlagClient : FeatureFlagClientType {

    override fun initialize() {}

    override fun fetch(context: Activity) {}

    override fun activate(context: Activity) {}

    override fun fetchAndActivate(context: Activity) {}

    override fun getBoolean(FlagKey: FlagKey) = false

    override fun getDouble(FlagKey: FlagKey) = 0.0

    override fun getLong(FlagKey: FlagKey) = 0L

    override fun getString(FlagKey: FlagKey) = ""
}
