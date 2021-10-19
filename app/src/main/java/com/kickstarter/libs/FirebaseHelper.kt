package com.kickstarter.libs

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import com.kickstarter.libs.utils.extensions.isKSApplication
import java.nio.file.Files.delete

class FirebaseHelper(context: Context, callback: () -> Boolean) {

    companion object {
        @JvmStatic var identifier: String = ""
        // - Should be called just one time
        @JvmStatic fun initialize(context: Context, callback: () -> Boolean): FirebaseHelper {
            return FirebaseHelper(context, callback)
        }

        @JvmStatic fun delete() =  FirebaseInstallations.getInstance().delete()
    }

    init {
        if (context.isKSApplication()) {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
                FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(true)
                FirebaseInstallations.getInstance().id.addOnSuccessListener { s: String ->
                    identifier = s
                    callback()
                }
            }
        } else {
            identifier = "Test Id"
        }
    }
}
