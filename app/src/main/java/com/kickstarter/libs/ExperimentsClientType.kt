package com.kickstarter.libs

import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.libs.utils.ExperimentRevenueData
import com.kickstarter.libs.utils.ExperimentUtils

interface ExperimentsClientType {

    fun ExperimentsClientType.attributes(experimentData: ExperimentData, apiEndpoint: ApiEndpoint): MutableMap<String, *> {
        return ExperimentUtils.attributes(experimentData, appVersion(), OSVersion(), apiEndpoint)
    }

    fun appVersion(): String
    fun OSVersion(): String
    fun track(eventKey: String, experimentData: ExperimentData)
    fun trackRevenue(eventKey: String, experimentData: ExperimentRevenueData)
    fun userId() : String
    fun variant(experiment: OptimizelyExperiment.Key, experimentData: ExperimentData): OptimizelyExperiment.Variant?
}
