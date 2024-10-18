package com.kickstarter.libs

import android.app.Activity

interface InternalToolsType {
    fun maybeStartInternalToolsActivity(activity: Activity)
    fun basicAuthorizationHeader(): String?
}
