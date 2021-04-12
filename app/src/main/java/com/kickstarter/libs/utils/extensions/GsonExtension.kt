package com.kickstarter.libs.utils.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)
