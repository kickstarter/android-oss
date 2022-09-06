@file:JvmName("ContextExt")
package com.kickstarter.libs.utils.extensions

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.stripe.android.paymentsheet.PaymentSheet

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

/**
 * Provides the configuration for the PaymentSheet, following the specs
 *  @see [link](https://stripe.com/docs/payments/accept-a-payment?platform=android&ui=elements#android-flowcontroller)
 */
fun Context.getPaymentSheetConfiguration(): PaymentSheet.Configuration {
    return PaymentSheet.Configuration(
        merchantDisplayName = getString(R.string.app_name),
        allowsDelayedPaymentMethods = true,
        appearance = this.getPaymentSheetAppearance()
    )
}

/**
 * Provides the color configuration for the PaymentSheet, following the specs
 *  @see [link](https://stripe.com/docs/elements/appearance-api?platform=android#colors-android)
 */
fun Context.getPaymentSheetAppearance(): PaymentSheet.Appearance {
    return PaymentSheet.Appearance(
        colorsLight = PaymentSheet.Colors(
            primary = getColor(R.color.primary),
            surface = getColor(R.color.kds_white),
            component = getColor(R.color.kds_white),
            componentBorder = getColor(R.color.kds_support_400),
            componentDivider = getColor(R.color.kds_black),
            onComponent = getColor(R.color.kds_black),
            subtitle = getColor(R.color.kds_black),
            placeholderText = getColor(R.color.kds_support_500),
            onSurface = getColor(R.color.kds_black),
            appBarIcon = getColor(R.color.kds_black),
            error = getColor(R.color.kds_alert),
        ),
        colorsDark = PaymentSheet.Colors(
            primary = getColor(R.color.primary),
            surface = getColor(R.color.kds_white),
            component = getColor(R.color.kds_white),
            componentBorder = getColor(R.color.kds_support_400),
            componentDivider = getColor(R.color.kds_black),
            onComponent = getColor(R.color.kds_black),
            subtitle = getColor(R.color.kds_black),
            placeholderText = getColor(R.color.kds_support_500),
            onSurface = getColor(R.color.kds_black),
            appBarIcon = getColor(R.color.kds_black),
            error = getColor(R.color.kds_alert),
        ),
        shapes = PaymentSheet.Shapes(
            cornerRadiusDp = 12.0f,
            borderStrokeWidthDp = 0.5f
        ),
        primaryButton = PaymentSheet.PrimaryButton(
            shape = PaymentSheet.PrimaryButtonShape(
                cornerRadiusDp = 12.0f
            ),
        )
    )
}
