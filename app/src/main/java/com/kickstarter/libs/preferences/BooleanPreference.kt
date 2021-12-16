package com.kickstarter.libs.preferences

import android.content.SharedPreferences
import kotlin.jvm.JvmOverloads

class BooleanPreference @JvmOverloads constructor(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Boolean = false
) : BooleanPreferenceType {
    override fun get(): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override val isSet: Boolean
        get() = sharedPreferences.contains(key)

    override fun set(value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun delete() {
        sharedPreferences.edit().remove(key).apply()
    }
}
