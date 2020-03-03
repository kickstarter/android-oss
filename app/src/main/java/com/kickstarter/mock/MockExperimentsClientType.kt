package com.kickstarter.mock

import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.models.User
import rx.Observable
import rx.subjects.PublishSubject

open class MockExperimentsClientType(private val variant: OptimizelyExperiment.Variant, private val apiEndpoint: ApiEndpoint) : ExperimentsClientType {
    constructor(variant: OptimizelyExperiment.Variant) : this(variant, ApiEndpoint.STAGING)
    constructor() : this(OptimizelyExperiment.Variant.CONTROL, ApiEndpoint.STAGING)

    class ExperimentsEvent internal constructor(internal val eventKey: String, internal val attributes: MutableMap<String, *>)

    private val experimentEvents : PublishSubject<ExperimentsEvent> = PublishSubject.create()
    val eventKeys: Observable<String> = this.experimentEvents.map { e -> e.eventKey }

    override fun track(eventKey: String, user: User?, refTag: RefTag?) {
        this.experimentEvents.onNext(ExperimentsEvent(eventKey, attributes(user, refTag, this.apiEndpoint)))
    }

    override fun userId(): String = "device-id"

    override fun variant(experiment: OptimizelyExperiment.Key, user: User?, refTag: RefTag?): OptimizelyExperiment.Variant = this.variant

    override fun androidBuildVersion(): String = "9"
}
