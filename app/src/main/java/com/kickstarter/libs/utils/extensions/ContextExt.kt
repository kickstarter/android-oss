@file:JvmName("ContextExt")
package com.kickstarter.libs.utils.extensions

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import com.kickstarter.KSApplication
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.activities.AppThemes
import com.stripe.android.paymentsheet.PaymentSheet

fun Context.isKSApplication() = (this is KSApplication) && !this.isInUnitTests

fun Context.getEnvironment(): Environment? {
    return (this.applicationContext as KSApplication).component().environment()
}

@Composable
fun Context.isDarkModeEnabled(env: Environment): Boolean {
    val darkModeEnabled = env.featureFlagClient()?.getBoolean(FlagKey.ANDROID_DARK_MODE_ENABLED) ?: false
    val theme = env.sharedPreferences()
        ?.getInt(SharedPreferenceKey.APP_THEME, AppThemes.MATCH_SYSTEM.ordinal)
        ?: AppThemes.MATCH_SYSTEM.ordinal

    return if (darkModeEnabled) {
        when (theme) {
            AppThemes.MATCH_SYSTEM.ordinal -> isSystemInDarkTheme()
            AppThemes.DARK.ordinal -> true
            AppThemes.LIGHT.ordinal -> false
            else -> false
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        isSystemInDarkTheme() // Force dark mode uses system theme
    } else false
}

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

fun Context.checkPermissions(permission: String): Boolean {
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

/**
 * Provides the configuration for the PaymentSheet, following the specs
 *  @see [link](https://stripe.com/docs/payments/accept-a-payment?platform=android&ui=elements#android-flowcontroller)
 */
fun Context.getPaymentSheetConfiguration(userEmail: String): PaymentSheet.Configuration {
    val stripeLinkEnabled = this.getEnvironment()?.featureFlagClient()?.getBoolean(FlagKey.ANDROID_STRIPE_LINK) ?: false
    // TODO: Wait for stripe to devise a client-side option for turning off link

    return PaymentSheet.Configuration(
        merchantDisplayName = getString(R.string.app_name),
        allowsDelayedPaymentMethods = true,
        appearance = this.getPaymentSheetAppearance(),
        defaultBillingDetails = PaymentSheet.BillingDetails(email = userEmail)
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

        // TODO: Get correct colors for this
        colorsDark = PaymentSheet.Colors(
            primary = getColor(R.color.kds_create_300),
            surface = getColor(R.color.kds_black),
            component = getColor(R.color.kds_black),
            componentBorder = getColor(R.color.kds_support_300),
            componentDivider = getColor(R.color.kds_white),
            onComponent = getColor(R.color.kds_white),
            subtitle = getColor(R.color.kds_white),
            placeholderText = getColor(R.color.kds_support_200),
            onSurface = getColor(R.color.kds_white),
            appBarIcon = getColor(R.color.kds_white),
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
