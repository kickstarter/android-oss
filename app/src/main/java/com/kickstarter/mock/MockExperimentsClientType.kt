package com.kickstarter.mock

import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.models.OptimizelyEnvironment
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.libs.utils.ExperimentRevenueData
import org.json.JSONArray
import org.json.JSONObject
import rx.Observable
import rx.subjects.PublishSubject

open class MockExperimentsClientType(private val variant: OptimizelyExperiment.Variant, private val optimizelyEnvironment: OptimizelyEnvironment) : ExperimentsClientType {
    constructor(variant: OptimizelyExperiment.Variant) : this(variant, OptimizelyEnvironment.STAGING)
    constructor() : this(OptimizelyExperiment.Variant.CONTROL, OptimizelyEnvironment.STAGING)

    class ExperimentsEvent internal constructor(internal val eventKey: String, internal val attributes: Map<String, *>, internal val tags: Map<String, *>?)

    private val experimentEvents : PublishSubject<ExperimentsEvent> = PublishSubject.create()
    val eventKeys: Observable<String> = this.experimentEvents.map { e -> e.eventKey }

    override fun appVersion(): String = "9.9.9"

    override fun optimizelyEnvironment(): OptimizelyEnvironment = this.optimizelyEnvironment

    override fun optimizelyProperties(experimentData: ExperimentData): Map<String, Any> {
        val experiments = JSONArray()
        val variant = this.variant.rawValue ?: "unknown"
        experiments.put(JSONObject(mutableMapOf<Any?, Any?>("optimizely_experiment_slug" to "test_experiment",
                "optimizely_variant_id" to variant)))

        return mapOf("optimizely_api_key" to optimizelyEnvironment.sdkKey,
                "optimizely_environment_key" to optimizelyEnvironment.environmentKey,
                "optimizely_experiments" to experiments)
    }

    override fun trackingVariation(experimentKey: String, experimentData: ExperimentData): String? = this.variant.rawValue

    override fun OSVersion(): String = "9"

    override fun track(eventKey: String, experimentData: ExperimentData) {
        this.experimentEvents.onNext(ExperimentsEvent(eventKey, attributes(experimentData, this.optimizelyEnvironment), null))
    }

    override fun trackRevenue(eventKey: String, experimentRevenueData: ExperimentRevenueData) {
        this.experimentEvents.onNext(ExperimentsEvent(eventKey, attributes(experimentRevenueData.experimentData, this.optimizelyEnvironment), checkoutTags(experimentRevenueData)))
    }

    override fun userId(): String = "device-id"

    override fun variant(experiment: OptimizelyExperiment.Key, experimentData: ExperimentData): OptimizelyExperiment.Variant = this.variant
}
