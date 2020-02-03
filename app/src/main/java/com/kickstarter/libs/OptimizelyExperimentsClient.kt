package com.kickstarter.libs

import com.google.firebase.iid.FirebaseInstanceId
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.models.User
import com.optimizely.ab.android.sdk.OptimizelyClient
import com.optimizely.ab.android.sdk.OptimizelyManager

class OptimizelyExperimentsClient(private val optimizelyManager: OptimizelyManager) : ExperimentsClientType {
    override fun androidBuildVersion(): String {
        return android.os.Build.VERSION.RELEASE
    }

    override fun track(eventKey: String, user: User?, refTag: RefTag?) {
        optimizelyClient().track(eventKey, userId(), attributes(user, refTag))
    }

    override fun variant(experiment: OptimizelyExperiment.Key, user: User?, refTag: RefTag?): OptimizelyExperiment.Variant {
        val variationString: String? = if (user?.isAdmin == true) {
            optimizelyClient().getVariation(experiment.key, user.id().toString(), attributes(user, refTag))
        } else {
            optimizelyClient().activate(experiment.key, userId(), attributes(user, refTag))
        }?.key

        return OptimizelyExperiment.Variant.safeValueOf(variationString)
    }

    override fun userId(): String = FirebaseInstanceId.getInstance().id

    private fun optimizelyClient(): OptimizelyClient = this.optimizelyManager.optimizely
}
