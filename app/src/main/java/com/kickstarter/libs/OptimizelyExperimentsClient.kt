package com.kickstarter.libs

import android.os.Build
import com.google.firebase.iid.FirebaseInstanceId
import com.kickstarter.BuildConfig
import com.kickstarter.libs.models.OptimizelyEnvironment
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.models.User
import com.optimizely.ab.android.sdk.OptimizelyClient
import com.optimizely.ab.android.sdk.OptimizelyManager

class OptimizelyExperimentsClient(private val optimizelyManager: OptimizelyManager, private val optimizelyEnvironment: OptimizelyEnvironment) : ExperimentsClientType {
    override fun appVersion(): String = BuildConfig.VERSION_NAME

    override fun OSVersion(): String = Build.VERSION.RELEASE

    override fun track(eventKey: String, experimentData: ExperimentData) {
        optimizelyClient().track(eventKey, userId(), attributes(experimentData, this.optimizelyEnvironment))
    }

    override fun userId(): String = FirebaseInstanceId.getInstance().id

    override fun enabledFeatures(user: User?): List<String> {
        return this.optimizelyClient().getEnabledFeatures(
            userId(),
            attributes(ExperimentData(user, null, null), this.optimizelyEnvironment)
        )
            ?: emptyList()
    }

    override fun isFeatureEnabled(feature: OptimizelyFeature.Key, experimentData: ExperimentData): Boolean {
        return optimizelyClient().isFeatureEnabled(feature.key, userId(), attributes(experimentData, this.optimizelyEnvironment))
    }

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

    override fun getTrackingProperties(): Map<String, Array<Map<String, String>>> {
        val experimentsList = mutableListOf<Map<String, String>>()
        val properties = mutableMapOf<String, Array<Map<String, String>>>()

        this.optimizelyClient().optimizelyConfig?.experimentsMap?.map { entry ->
            val variant: String = this.optimizelyClient().getVariation(entry.key, userId())?.let { it.key } ?: "unknown"
            experimentsList.add(mapOf(entry.key to variant))
        }

        properties["variants_optimizely"] = experimentsList.toTypedArray()

        return properties.toMap()
    }
}
