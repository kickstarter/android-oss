package com.kickstarter.libs.preferences

import android.content.SharedPreferences
import kotlin.jvm.JvmOverloads

class StringPreference @JvmOverloads constructor(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: String = ""
) : StringPreferenceType {
    override fun get(): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    override val isSet: Boolean
        get() = sharedPreferences.contains(key)

    override fun set(value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun delete() {
        sharedPreferences.edit().remove(key).apply()
    }
}
