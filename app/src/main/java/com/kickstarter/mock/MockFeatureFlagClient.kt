package com.kickstarter.mock

import android.app.Activity
import com.kickstarter.libs.featureflag.FFKey
import com.kickstarter.libs.featureflag.FeatureFlagClientType

open class MockFeatureFlagClient : FeatureFlagClientType {
    override var isEnabled: Boolean = true
    override var isActive: Boolean = true

    override fun initialize() {}

    override fun fetch(context: Activity) {}

    override fun activate(context: Activity) {}

    override fun fetchAndActivate(context: Activity) {}

    override fun getBoolean(FFKey: FFKey) = false

    override fun getDouble(FFKey: FFKey) = 0.0

    override fun getLong(FFKey: FFKey) = 0L

    override fun getString(FFKey: FFKey) = ""
}
