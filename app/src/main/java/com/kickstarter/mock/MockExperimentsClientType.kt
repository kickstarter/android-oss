package com.kickstarter.mock

import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.libs.utils.ExperimentRevenueData
import rx.Observable
import rx.subjects.PublishSubject

open class MockExperimentsClientType(private val variant: OptimizelyExperiment.Variant, private val apiEndpoint: ApiEndpoint) : ExperimentsClientType {
    constructor(variant: OptimizelyExperiment.Variant) : this(variant, ApiEndpoint.STAGING)
    constructor() : this(OptimizelyExperiment.Variant.CONTROL, ApiEndpoint.STAGING)

    class ExperimentsEvent internal constructor(internal val eventKey: String, internal val attributes: Map<String, *>, internal val tags: Map<String, *>?)

    private val experimentEvents : PublishSubject<ExperimentsEvent> = PublishSubject.create()
    val eventKeys: Observable<String> = this.experimentEvents.map { e -> e.eventKey }

    override fun appVersion(): String = "9.9.9"

    override fun OSVersion(): String = "9"

    override fun track(eventKey: String, experimentData: ExperimentData) {
        this.experimentEvents.onNext(ExperimentsEvent(eventKey, attributes(experimentData, this.apiEndpoint), null))
    }

    override fun trackRevenue(eventKey: String, experimentRevenueData: ExperimentRevenueData) {
        this.experimentEvents.onNext(ExperimentsEvent(eventKey, attributes(experimentRevenueData.experimentData, this.apiEndpoint), checkoutTags(experimentRevenueData)))
    }

    override fun userId(): String = "device-id"

    override fun variant(experiment: OptimizelyExperiment.Key, experimentData: ExperimentData): OptimizelyExperiment.Variant = this.variant
}
