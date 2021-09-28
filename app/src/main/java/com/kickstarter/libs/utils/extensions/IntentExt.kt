package com.kickstarter.libs.utils.extensions

import android.content.Context
import android.content.Intent
import com.kickstarter.ui.activities.ProjectPageActivity

fun Intent.projectPageFeatureFlag(context: Context, isEnabled: Boolean) : Intent {
    return Intent(context, ProjectPageActivity::class.java)
}