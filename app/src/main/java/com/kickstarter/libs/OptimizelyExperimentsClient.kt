package com.kickstarter.libs

import android.os.Build
import com.google.firebase.iid.FirebaseInstanceId
import com.kickstarter.BuildConfig
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.libs.utils.ExperimentRevenueData
import com.optimizely.ab.android.sdk.OptimizelyClient
import com.optimizely.ab.android.sdk.OptimizelyManager

class OptimizelyExperimentsClient(private val optimizelyManager: OptimizelyManager, private val apiEndpoint: ApiEndpoint) : ExperimentsClientType {
    override fun appVersion(): String = BuildConfig.VERSION_NAME

    override fun OSVersion(): String = Build.VERSION.RELEASE

    override fun track(eventKey: String, experimentData: ExperimentData) {
        optimizelyClient().track(eventKey, userId(), attributes(experimentData, this.apiEndpoint))
    }

    override fun trackRevenue(eventKey: String, experimentRevenueData: ExperimentRevenueData) {
        optimizelyClient().track(eventKey, userId(), attributes(experimentRevenueData.experimentData, this.apiEndpoint), checkoutTags(experimentRevenueData))
    }

    override fun userId(): String = FirebaseInstanceId.getInstance().id

    override fun variant(experiment: OptimizelyExperiment.Key, experimentData: ExperimentData): OptimizelyExperiment.Variant {
        val user = experimentData.user
        val variationString: String? = if (user?.isAdmin == true) {
            optimizelyClient().getVariation(experiment.key, user.id().toString(), attributes(experimentData, this.apiEndpoint))
        } else {
            optimizelyClient().activate(experiment.key, userId(), attributes(experimentData, this.apiEndpoint))
        }?.key

        return OptimizelyExperiment.Variant.safeValueOf(variationString)
    }

    private fun optimizelyClient(): OptimizelyClient = this.optimizelyManager.optimizely
}
