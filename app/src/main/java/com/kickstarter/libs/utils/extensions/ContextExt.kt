@file:JvmName("ContextExt")
package com.kickstarter.libs.utils.extensions

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
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

fun Context.checkPermissions(permission: String) : Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_DENIED
}

fun Context.showAlertDialog(
    title: String? = "",
    message: String? = "",
    positiveActionTitle: String? = null,
    negativeActionTitle: String? = null,
    isCancelable: Boolean = true,
    positiveAction: (() -> Unit)? = null,
    negativeAction: (() -> Unit)? = null
) {

    // setup the alert builder
    val builder = AlertDialog.Builder(this).apply {
        setTitle(title)
        setMessage(message)

        // add a button
        positiveActionTitle?.let {
            setPositiveButton(positiveActionTitle) { dialog, _ ->
                dialog.dismiss()
                positiveAction?.invoke()
            }
        }

        negativeActionTitle?.let {
            setNegativeButton(negativeActionTitle) { dialog, _ ->
                dialog.dismiss()
                negativeAction?.invoke()
            }
        }

        setCancelable(isCancelable)
    }

    // create and show the alert dialog
    val dialog: AlertDialog = builder.create()
    dialog.show()
}
