package com.kickstarter.libs

import android.content.SharedPreferences
import com.google.gson.Gson
import com.kickstarter.libs.featureflag.FeatureFlagClientType
import com.kickstarter.libs.preferences.BooleanPreferenceType
import com.kickstarter.libs.preferences.IntPreferenceType
import com.kickstarter.libs.utils.PlayServicesCapability
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.WebClientType
import com.stripe.android.Stripe
import rx.Scheduler
import java.net.CookieManager

class Environment private constructor(
    private val activitySamplePreference: IntPreferenceType?,
    private val apiClient: ApiClientType?,
    private val apolloClient: ApolloClientType?,
    private val apolloClientV2: ApolloClientTypeV2?,
    private val build: Build?,
    private val buildCheck: BuildCheck?,
    private val cookieManager: CookieManager?,
    private val currentConfig: CurrentConfigType?,
    private val currentConfigV2: CurrentConfigTypeV2?,
    private val currentUser: CurrentUserType?,
    private val currentUserV2: CurrentUserTypeV2?,
    private val firstSessionPreference: BooleanPreferenceType?,
    private val gson: Gson?,
    private val hasSeenAppRatingPreference: BooleanPreferenceType?,
    private val hasSeenGamesNewsletterPreference: BooleanPreferenceType?,
    private val internalTools: InternalToolsType?,
    private val ksCurrency: KSCurrency?,
    private val ksString: KSString?,
    private val analytics: AnalyticEvents?,
    private val logout: Logout?,
    private val playServicesCapability: PlayServicesCapability?,
    private val scheduler: Scheduler?,
    private val schedulerV2: io.reactivex.Scheduler?,
    private val sharedPreferences: SharedPreferences?,
    private val stripe: Stripe?,
    private val webClient: WebClientType?,
    private val webEndpoint: String,
    private val firebaseAnalyticsClient: FirebaseAnalyticsClientType?,
    private val featureFlagClient: FeatureFlagClientType?
) {
    fun activitySamplePreference() = this.activitySamplePreference
    fun apiClient() = this.apiClient
    fun apolloClient() = this.apolloClient
    fun apolloClientV2() = this.apolloClientV2
    fun build() = this.build
    fun buildCheck() = this.buildCheck
    fun cookieManager() = this.cookieManager
    fun currentConfig() = this.currentConfig
    fun currentConfigV2() = this.currentConfigV2
    fun currentUser() = this.currentUser
    fun currentUserV2() = this.currentUserV2
    fun firstSessionPreference() = this.firstSessionPreference
    fun gson() = this.gson
    fun hasSeenAppRatingPreference() = this.hasSeenAppRatingPreference
    fun hasSeenGamesNewsletterPreference() = this.hasSeenGamesNewsletterPreference
    fun internalTools() = this.internalTools
    fun ksCurrency() = this.ksCurrency
    fun ksString() = this.ksString
    fun analytics() = this.analytics
    fun logout() = this.logout
    fun playServicesCapability() = this.playServicesCapability
    fun scheduler() = this.scheduler
    fun schedulerV2() = this.schedulerV2
    fun sharedPreferences() = this.sharedPreferences
    fun stripe() = this.stripe
    fun webClient() = this.webClient
    fun webEndpoint() = this.webEndpoint
    fun firebaseAnalyticsClient() = this.firebaseAnalyticsClient
    fun featureFlagClient() = this.featureFlagClient

    data class Builder(
        private var activitySamplePreference: IntPreferenceType? = null,
        private var apiClient: ApiClientType? = null,
        private var apolloClient: ApolloClientType? = null,
        private var apolloClientV2: ApolloClientTypeV2? = null,
        private var build: Build? = null,
        private var buildCheck: BuildCheck? = null,
        private var cookieManager: CookieManager? = null,
        private var currentConfig: CurrentConfigType? = null,
        private var currentConfigV2: CurrentConfigTypeV2? = null,
        private var currentUser: CurrentUserType? = null,
        private var currentUserV2: CurrentUserTypeV2? = null,
        private var firstSessionPreference: BooleanPreferenceType? = null,
        private var gson: Gson? = null,
        private var hasSeenAppRatingPreference: BooleanPreferenceType? = null,
        private var hasSeenGamesNewsletterPreference: BooleanPreferenceType? = null,
        private var internalTools: InternalToolsType? = null,
        private var ksCurrency: KSCurrency? = null,
        private var ksString: KSString? = null,
        private var analytics: AnalyticEvents? = null,
        private var logout: Logout? = null,
        private var playServicesCapability: PlayServicesCapability? = null,
        private var scheduler: Scheduler? = null,
        private var schedulerV2: io.reactivex.Scheduler? = null,
        private var sharedPreferences: SharedPreferences? = null,
        private var stripe: Stripe? = null,
        private var webClient: WebClientType? = null,
        private var webEndpoint: String = "",
        private var firebaseAnalyticsClient: FirebaseAnalyticsClientType? = null,
        private var featureFlagClient: FeatureFlagClientType? = null
    ) {
        fun activitySamplePreference(activitySamplePreference: IntPreferenceType) = apply { this.activitySamplePreference = activitySamplePreference }
        fun apiClient(apiClient: ApiClientType) = apply { this.apiClient = apiClient }
        fun apolloClient(apolloClient: ApolloClientType) = apply { this.apolloClient = apolloClient }
        fun apolloClientV2(apolloClientV2: ApolloClientTypeV2) = apply { this.apolloClientV2 = apolloClientV2 }
        fun build(build: Build) = apply { this.build = build }
        fun buildCheck(buildCheck: BuildCheck) = apply { this.buildCheck = buildCheck }
        fun cookieManager(cookieManager: CookieManager) = apply { this.cookieManager = cookieManager }
        fun currentConfig(currentConfig: CurrentConfigType) = apply { this.currentConfig = currentConfig }
        fun currentConfig2(currentConfig2: CurrentConfigTypeV2) = apply { this.currentConfigV2 = currentConfig2 }
        fun currentUser(currentUser: CurrentUserType) = apply { this.currentUser = currentUser }
        fun currentUserV2(currentUserV2: CurrentUserTypeV2) = apply { this.currentUserV2 = currentUserV2 }
        fun firstSessionPreference(firstSessionPreference: BooleanPreferenceType) = apply { this.firstSessionPreference = firstSessionPreference }
        fun gson(gson: Gson) = apply { this.gson = gson }
        fun hasSeenAppRatingPreference(hasSeenAppRatingPreference: BooleanPreferenceType) = apply { this.hasSeenAppRatingPreference = hasSeenAppRatingPreference }
        fun hasSeenGamesNewsletterPreference(hasSeenGamesNewsletterPreference: BooleanPreferenceType) = apply { this.hasSeenGamesNewsletterPreference = hasSeenGamesNewsletterPreference }
        fun internalTools(internalTools: InternalToolsType) = apply { this.internalTools = internalTools }
        fun ksCurrency(ksCurrency: KSCurrency) = apply { this.ksCurrency = ksCurrency }
        fun ksString(ksString: KSString) = apply { this.ksString = ksString }
        fun analytics(analytics: AnalyticEvents) = apply { this.analytics = analytics }
        fun logout(logout: Logout) = apply { this.logout = logout }
        fun playServicesCapability(playServicesCapability: PlayServicesCapability) = apply { this.playServicesCapability = playServicesCapability }
        fun scheduler(scheduler: Scheduler) = apply { this.scheduler = scheduler }
        fun schedulerV2(schedulerV2: io.reactivex.Scheduler) = apply { this.schedulerV2 = schedulerV2 }
        fun sharedPreferences(sharedPreferences: SharedPreferences) = apply { this.sharedPreferences = sharedPreferences }
        fun stripe(stripe: Stripe) = apply { this.stripe = stripe }
        fun webClient(webClient: WebClientType) = apply { this.webClient = webClient }
        fun webEndpoint(webEndpoint: String) = apply { this.webEndpoint = webEndpoint }
        fun firebaseAnalyticsClient(firebaseAnalyticsClient: FirebaseAnalyticsClientType) = apply { this.firebaseAnalyticsClient = firebaseAnalyticsClient }

        fun featureFlagClient(featureFlag: FeatureFlagClientType) = apply { this.featureFlagClient = featureFlag }

        fun build() = Environment(
            activitySamplePreference = activitySamplePreference,
            apiClient = apiClient,
            apolloClient = apolloClient,
            apolloClientV2 = apolloClientV2,
            build = build,
            buildCheck = buildCheck,
            cookieManager = cookieManager,
            currentConfig = currentConfig,
            currentConfigV2 = currentConfigV2,
            currentUser = currentUser,
            currentUserV2 = currentUserV2,
            firstSessionPreference = firstSessionPreference,
            gson = gson,
            hasSeenAppRatingPreference = hasSeenAppRatingPreference,
            hasSeenGamesNewsletterPreference = hasSeenGamesNewsletterPreference,
            internalTools = internalTools,
            ksCurrency = ksCurrency,
            ksString = ksString,
            analytics = analytics,
            logout = logout,
            playServicesCapability = playServicesCapability,
            scheduler = scheduler,
            schedulerV2 = schedulerV2,
            sharedPreferences = sharedPreferences,
            stripe = stripe,
            webClient = webClient,
            webEndpoint = webEndpoint,
            firebaseAnalyticsClient = firebaseAnalyticsClient,
            featureFlagClient = featureFlagClient
        )
    }

    fun toBuilder() = Builder(
        activitySamplePreference = activitySamplePreference,
        apiClient = apiClient,
        apolloClient = apolloClient,
        apolloClientV2 = apolloClientV2,
        build = build,
        buildCheck = buildCheck,
        cookieManager = cookieManager,
        currentConfig = currentConfig,
        currentConfigV2 = currentConfigV2,
        currentUser = currentUser,
        currentUserV2 = currentUserV2,
        firstSessionPreference = firstSessionPreference,
        gson = gson,
        hasSeenAppRatingPreference = hasSeenAppRatingPreference,
        hasSeenGamesNewsletterPreference = hasSeenGamesNewsletterPreference,
        internalTools = internalTools,
        ksCurrency = ksCurrency,
        ksString = ksString,
        analytics = analytics,
        logout = logout,
        playServicesCapability = playServicesCapability,
        scheduler = scheduler,
        schedulerV2 = schedulerV2,
        sharedPreferences = sharedPreferences,
        stripe = stripe,
        webClient = webClient,
        webEndpoint = webEndpoint,
        firebaseAnalyticsClient = firebaseAnalyticsClient,
        featureFlagClient = featureFlagClient
    )

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
