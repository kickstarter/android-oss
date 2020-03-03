package com.kickstarter.libs

import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.utils.ExperimentUtils
import com.kickstarter.models.User

interface ExperimentsClientType {

    fun ExperimentsClientType.attributes(user: User?, refTag: RefTag?, apiEndpoint: ApiEndpoint): MutableMap<String, *> {
        return ExperimentUtils.attributes(user, refTag, androidBuildVersion(), apiEndpoint)
    }

    fun androidBuildVersion(): String
    fun track(eventKey: String, user: User?, refTag: RefTag?)
    fun userId() : String
    fun variant(experiment: OptimizelyExperiment.Key, user: User?, refTag: RefTag?): OptimizelyExperiment.Variant?
}
