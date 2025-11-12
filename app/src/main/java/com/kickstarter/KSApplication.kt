package com.kickstarter

import android.text.TextUtils
import androidx.annotation.CallSuper
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.apollographql.apollo3.exception.ApolloHttpException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.Build
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.FirebaseHelper.Companion.identifier
import com.kickstarter.libs.FirebaseHelper.Companion.initialize
import com.kickstarter.libs.PushNotifications
import com.kickstarter.libs.SegmentTrackingClient
import com.kickstarter.libs.braze.RemotePushClientType
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.featureflag.StatsigClient
import com.kickstarter.libs.utils.ApplicationLifecycleUtil
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.viewmodels.InitializationState
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.joda.time.DateTime
import timber.log.Timber
import timber.log.Timber.Forest.plant
import java.net.CookieHandler
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI
import java.util.UUID
import javax.inject.Inject

open class KSApplication : MultiDexApplication(), IKSApplicationComponent {
    private var component: ApplicationComponent? = null

    @Inject
    lateinit var cookieManager: CookieManager

    @Inject
    lateinit var pushNotifications: PushNotifications

    @Inject
    lateinit var remotePushClientType: RemotePushClientType

    @Inject
    lateinit var segmentTrackingClient: SegmentTrackingClient

    @Inject
    lateinit var build: Build

    @Inject
    lateinit var currentUser: CurrentUserTypeV2

    @Inject
    lateinit var ffClient: FeatureFlagClientType

    @Inject
    lateinit var statsigClient: StatsigClient

    /**
     * - A CoroutineScope tied to the Application lifecycle
     *  used to initialize dependencies that require coroutines and early on network calls.
     *  and experiments dependencies potentially affecting launch activities (Discovery/ProjectPage)
     */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val mutableInitializationState = MutableStateFlow(InitializationState.NOT_STARTED)
    val initializationState = mutableInitializationState.asStateFlow()

    @CallSuper
    override fun onCreate() {
        super.onCreate()

        this.component = getComponent()
        component()?.inject(this)

        if (!isInUnitTests) {
            initApplication()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }

    override fun getComponent(): ApplicationComponent {
        val component = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
        return component
    }

    private fun initApplication() {
        MultiDex.install(this)

        // Only log for internal builds
        if (BuildConfig.FLAVOR == "internal") {
            plant(Timber.DebugTree())
        }

        createErrorHandler()
        initialize(applicationContext, ffClient) { this.initializeDependencies() }
    }

    // - Returns Boolean because incompatible Java "void" type with kotlin "Void" type for the lambda declaration
    private fun initializeDependencies(): Boolean {
        setVisitorCookie()
        pushNotifications.initialize()

        val appUtil = ApplicationLifecycleUtil(this)
        registerActivityLifecycleCallbacks(appUtil)
        registerComponentCallbacks(appUtil)

        // - Initialize Segment SDK
        if (this.segmentTrackingClient != null) {
            segmentTrackingClient.initialize()
        }

        // - Register lifecycle callback for Braze
        remotePushClientType.registerActivityLifecycleCallbacks(this)

        // - Initialize Statsig SDK
        statsigClient.initialize(
            scope = applicationScope,
            errorCallback = { exception ->
                FirebaseCrashlytics.getInstance().recordException(exception)
            }
        )

        return true
    }

    fun component(): ApplicationComponent? {
        return this.component
    }

    open val isInUnitTests: Boolean
        /**
         * Method override in tha child class for testings purposes
         */
        get() = false

    private fun setVisitorCookie() {
        val deviceId = identifier.value
        val uniqueIdentifier =
            if (TextUtils.isEmpty(deviceId)) UUID.randomUUID().toString() else deviceId
        val cookie = HttpCookie("vis", uniqueIdentifier)
        cookie.maxAge = DateTime.now().plusYears(100).millis
        cookie.secure = true
        val webUri = URI.create(Secrets.WebEndpoint.PRODUCTION)
        val apiUri = URI.create(ApiEndpoint.PRODUCTION.url())
        cookieManager.cookieStore.add(webUri, cookie)
        cookieManager.cookieStore.add(apiUri, cookie)
        CookieHandler.setDefault(this.cookieManager)
    }

    private fun createErrorHandler() {
        RxJavaPlugins.setErrorHandler { t: Throwable ->
            if (t is OnErrorNotImplementedException &&
                t.cause != null && t.cause is ApolloHttpException &&
                (t.cause as ApolloHttpException).statusCode == 429
            ) {
                Timber.e(t, "RxJavaPlugins.setErrorHandler")
                val apolloHttpException = t.cause as ApolloHttpException?
                val value =
                    if (apolloHttpException?.message != null) apolloHttpException.message else ""
                FirebaseCrashlytics.getInstance().setCustomKey("ApolloHttpException (429)", value!!)
                FirebaseCrashlytics.getInstance().recordException(t)
            } else if (t is UndeliverableException) {
                Timber.w(t, "Undeliverable Exception")
                if (t.message != null) {
                    FirebaseCrashlytics.getInstance().setCustomKey(
                        "Undeliverable Exception",
                        t.message ?: ""
                    )
                }
                FirebaseCrashlytics.getInstance().recordException(t)
            } else {
                Timber.e(t, "RxJavaPlugins.setErrorHandler")
                FirebaseCrashlytics.getInstance().recordException(t)
            }
        }
    }
}
