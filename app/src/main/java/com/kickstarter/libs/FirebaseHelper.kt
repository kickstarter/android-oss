package com.kickstarter.libs

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.remoteConfig
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.utils.extensions.isKSApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FirebaseHelper(context: Context, ffClient: FeatureFlagClientType, callback: () -> Boolean) {

    companion object {
        @JvmStatic private val mutableIdentifier = MutableStateFlow("")
        @JvmStatic val identifier: StateFlow<String> = mutableIdentifier.asStateFlow()

        // - Should be called just one time
        @JvmStatic fun initialize(
            context: Context,
            ffClient: FeatureFlagClientType,
            callback: () -> Boolean
        ): FirebaseHelper {
            return FirebaseHelper(context, ffClient, callback)
        }

        @VisibleForTesting
        fun mutableIdentifier() = mutableIdentifier

        @JvmStatic fun delete() = FirebaseInstallations.getInstance().delete()
    }

    init {
        if (context.isKSApplication()) {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
                FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(true)
            }
            // - Remote config requires FirebaseApp.initializeApp(context) to be called before initializing
            ffClient.initialize(Firebase.remoteConfig)
            FirebaseInstallations.getInstance().id.addOnSuccessListener { s: String ->
                mutableIdentifier.value = s
                callback()
            }
        } else {
            mutableIdentifier.value = "Test Id"
        }
    }
}
