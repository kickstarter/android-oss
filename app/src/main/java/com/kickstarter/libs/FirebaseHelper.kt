package com.kickstarter.libs

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.remoteConfig
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.utils.extensions.isKSApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

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

        @JvmStatic fun delete() = FirebaseInstallations.getInstance().delete()
    }

    init {
        //not sure if runBlocking is the correct coroutine launcher we want here, but it seems to work on normal app bootstrap
        runBlocking {
            if (context.isKSApplication()) {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
                    FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(true)
                }

                // - Remote config requires FirebaseApp.initializeApp(context) to be called before initializing
                ffClient.initialize(Firebase.remoteConfig)
                mutableIdentifier.value = FirebaseInstallations.getInstance().id.await().toString()
                callback()
            } else {
                mutableIdentifier.value = "Test Id"
            }

        }
    }
}
