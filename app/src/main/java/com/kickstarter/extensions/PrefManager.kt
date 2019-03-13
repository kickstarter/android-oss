package com.kickstarter.extensions

import android.content.Context
import android.preference.PreferenceManager

const val ENABLE_HORIZONTAL_REWARD = "Enable Horizontal Rewards"

fun getHorizontalToggle(context: Context): Boolean {
    return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(ENABLE_HORIZONTAL_REWARD, false)
}

fun setHorizontalToggle(context: Context, isEnabled: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(ENABLE_HORIZONTAL_REWARD, isEnabled)
            .apply()
}