package com.kickstarter.libs.utils

import androidx.work.Constraints
import androidx.work.NetworkType

object WorkUtils {
    val baseConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun uniqueWorkName(tag: String) = tag + System.currentTimeMillis()
}
