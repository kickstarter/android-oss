@file:JvmName("ApplicationExt")
package com.kickstarter.libs.utils.extensions

import android.app.Application
import android.content.Context
import com.kickstarter.KSApplication

fun Context.isKSApplication() = (this is KSApplication) && !this.isInUnitTests

/**
 * if the current context is an instance of Application android base class
 * register the callbacks provided on the parameter.
 *
 * @param callbacks
 */
fun Context.registerActivityLifecycleCallbacks(callbacks: Application.ActivityLifecycleCallbacks) {
    if (this is Application) {
        this.registerActivityLifecycleCallbacks(callbacks)
    }
}
