package com.kickstarter.libs.utils.extensions

import android.content.Context
import android.content.Intent
import com.kickstarter.ui.activities.ProjectActivity
import com.kickstarter.ui.activities.ProjectPageActivity

fun Intent.projectPageFeatureFlag(context: Context, isEnabled: Boolean) : Intent {
    return this.setClass(context, if(isEnabled) ProjectPageActivity::class.java else ProjectActivity::class.java)
}