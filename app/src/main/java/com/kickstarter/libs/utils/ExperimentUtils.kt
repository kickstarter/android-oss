package com.kickstarter.libs.utils

import com.google.firebase.iid.FirebaseInstanceId
import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.RefTag
import com.kickstarter.models.User
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import java.util.*
import kotlin.math.roundToInt

object ExperimentUtils {

    fun attributes(experimentData: ExperimentData, appVersion: String, OSVersion: String, apiEndpoint: ApiEndpoint): MutableMap<String, Any?> {
        return mutableMapOf(
                Pair("distinct_id", if (apiEndpoint != ApiEndpoint.PRODUCTION) FirebaseInstanceId.getInstance().id else null),
                Pair("session_app_release_version", appVersion),
                Pair("session_os_version", String.format("Android %s", OSVersion)),
                Pair("session_ref_tag", experimentData.intentRefTag?.tag()),
                Pair("session_referrer_credit", experimentData.cookieRefTag?.tag()),
                Pair("session_user_is_logged_in", experimentData.user != null),
                Pair("user_backed_projects_count", experimentData.user?.backedProjectsCount() ?: 0),
                Pair("user_country", experimentData.user?.location()?.country() ?: Locale.getDefault().country)
        )
    }

    fun checkoutTags(experimentRevenueData: ExperimentRevenueData): MutableMap<String, Any?> {
        val amount = experimentRevenueData.checkoutData.amount()
        val project = experimentRevenueData.pledgeData.projectData().project()
        val fxRate = project.fxRate()
        val paymentType = experimentRevenueData.checkoutData.paymentType()
        return mutableMapOf(
                Pair("checkout_amount", amount),
                Pair("checkout_payment_type", paymentType.rawValue()),
                Pair("checkout_revenue_in_usd_cents", (amount * fxRate * 100).roundToInt()),
                Pair("currency", project.currency())
        )
    }
}

data class ExperimentData(val user: User?, val intentRefTag: RefTag?, val cookieRefTag: RefTag?)
data class ExperimentRevenueData(val experimentData: ExperimentData, val checkoutData: CheckoutData, val pledgeData: PledgeData)
