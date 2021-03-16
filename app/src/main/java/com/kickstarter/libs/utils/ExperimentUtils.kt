package com.kickstarter.libs.utils

import com.google.firebase.iid.FirebaseInstanceId
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.models.OptimizelyEnvironment
import com.kickstarter.models.User
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import java.util.*

object ExperimentUtils {

    fun attributes(experimentData: ExperimentData, appVersion: String, OSVersion: String, optimizelyEnvironment: OptimizelyEnvironment): Map<String, Any?> {
        return mapOf(
            Pair("distinct_id", getInstanceId(optimizelyEnvironment)),
            Pair("session_app_release_version", appVersion),
            Pair("session_os_version", String.format("Android %s", OSVersion)),
            Pair("session_ref_tag", experimentData.intentRefTag?.tag()),
            Pair("session_referrer_credit", experimentData.cookieRefTag?.tag()),
            Pair("session_user_is_logged_in", experimentData.user != null),
            Pair("user_backed_projects_count", experimentData.user?.backedProjectsCount() ?: 0),
            Pair("user_country", experimentData.user?.location()?.country() ?: Locale.getDefault().country)
        )
    }

    private fun getInstanceId(environment: OptimizelyEnvironment) = when (environment) {
        OptimizelyEnvironment.DEVELOPMENT -> ""
        OptimizelyEnvironment.PRODUCTION -> null
        OptimizelyEnvironment.STAGING -> FirebaseInstanceId.getInstance().id
    }
}

data class ExperimentData(val user: User?, val intentRefTag: RefTag?, val cookieRefTag: RefTag?)
data class ExperimentRevenueData(val experimentData: ExperimentData, val checkoutData: CheckoutData, val pledgeData: PledgeData)
