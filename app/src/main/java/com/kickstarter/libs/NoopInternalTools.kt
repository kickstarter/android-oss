package com.kickstarter.libs

import android.app.Activity

class NoopInternalTools : InternalToolsType {
    override fun maybeStartInternalToolsActivity(activity: Activity) {}

    override fun basicAuthorizationHeader(): String? {
        return null
    }
}
