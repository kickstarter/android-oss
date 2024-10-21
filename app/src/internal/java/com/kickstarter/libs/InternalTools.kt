package com.kickstarter.libs

import android.content.Intent
import com.kickstarter.R
import com.kickstarter.ui.activities.InternalToolsActivity
import com.kickstarter.ui.extensions.startActivityWithTransition

class InternalTools : InternalToolsType {
    override fun maybeStartInternalToolsActivity(activity: android.app.Activity) {
        val intent = Intent(activity, InternalToolsActivity::class.java)
        activity.startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    override fun basicAuthorizationHeader(): String {
        return "Basic ZnV6enk6d3V6enk="
    }
}
