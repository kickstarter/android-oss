package com.kickstarter.extensions

import android.app.Activity
import com.kickstarter.R

fun Activity.bottomSlideAnimation() = overridePendingTransition(R.anim.settings_bottom_slide, R.anim.fade_out)