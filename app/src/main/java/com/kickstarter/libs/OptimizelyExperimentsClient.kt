package com.kickstarter.libs

import android.os.Build
import com.google.firebase.iid.FirebaseInstanceId
import com.kickstarter.BuildConfig
import com.kickstarter.libs.models.OptimizelyEnvironment
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.libs.utils.ExperimentRevenueData
import com.optimizely.ab.android.sdk.OptimizelyClient
import com.optimizely.ab.android.sdk.OptimizelyManager

class OptimizelyExperimentsClient(private val optimizelyManager: OptimizelyManager, private val optimizelyEnvironment: OptimizelyEnvironment) : ExperimentsClientType {
    override fun appVersion(): String = BuildConfig.VERSION_NAME

    override fun OSVersion(): String = Build.VERSION.RELEASE

    override fun track(eventKey: String, experimentData: ExperimentData) {
        optimizelyClient().track(eventKey, userId(), attributes(experimentData, this.optimizelyEnvironment))
    }

    override fun trackRevenue(eventKey: String, experimentRevenueData: ExperimentRevenueData) {
        optimizelyClient().track(eventKey, userId(), attributes(experimentRevenueData.experimentData, this.optimizelyEnvironment), checkoutTags(experimentRevenueData))
    }

    override fun userId(): String = FirebaseInstanceId.getInstance().id

    override fun variant(experiment: OptimizelyExperiment.Key, experimentData: ExperimentData): OptimizelyExperiment.Variant {
        val user = experimentData.user
        val variationString: String? = if (user?.isAdmin == true) {
            optimizelyClient().getVariation(experiment.key, user.id().toString(), attributes(experimentData, this.optimizelyEnvironment))
        } else {
            optimizelyClient().activate(experiment.key, userId(), attributes(experimentData, this.optimizelyEnvironment))
        }?.key

        return OptimizelyExperiment.Variant.safeValueOf(variationString)
    }

    override fun optimizelyEnvironment(): OptimizelyEnvironment = this.optimizelyEnvironment

    private fun optimizelyClient(): OptimizelyClient = this.optimizelyManager.optimizely

    override fun trackingVariation(experimentKey: String, experimentData: ExperimentData): String? {
        return optimizelyClient().getVariation(experimentKey, userId(), attributes(experimentData, this.optimizelyEnvironment))?.key
    }
}
