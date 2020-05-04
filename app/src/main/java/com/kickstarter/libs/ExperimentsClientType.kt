package com.kickstarter.libs

import com.kickstarter.libs.models.OptimizelyEnvironment
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.libs.utils.ExperimentUtils
import com.kickstarter.models.User
import org.json.JSONArray
import org.json.JSONObject

interface ExperimentsClientType {

    fun ExperimentsClientType.attributes(experimentData: ExperimentData, optimizelyEnvironment: OptimizelyEnvironment): Map<String, *> {
        return ExperimentUtils.attributes(experimentData, appVersion(), OSVersion(), optimizelyEnvironment)
    }

    fun optimizelyProperties(experimentData: ExperimentData): Map<String, Any> {
        val experiments = JSONArray()
        val properties = mapOf("optimizely_api_key" to optimizelyEnvironment().sdkKey,
                "optimizely_environment_key" to optimizelyEnvironment().environmentKey,
                "optimizely_experiments" to experiments)

        for (experiment in OptimizelyExperiment.Key.values()) {
            val variation = trackingVariation(experiment.key, experimentData) ?: "unknown"
            experiments.put(JSONObject(mutableMapOf<Any?, Any?>("optimizely_experiment_slug" to experiment.key,
                    "optimizely_variant_id" to variation)))
        }

        return properties
    }

    fun appVersion(): String
    fun enabledFeatures(user: User?): List<String>
    fun isFeatureEnabled(feature: OptimizelyFeature.Key, experimentData: ExperimentData): Boolean
    fun optimizelyEnvironment(): OptimizelyEnvironment
    fun OSVersion(): String
    fun trackingVariation(experimentKey: String, experimentData: ExperimentData): String?
    fun userId() : String
    fun variant(experiment: OptimizelyExperiment.Key, experimentData: ExperimentData): OptimizelyExperiment.Variant?
}
