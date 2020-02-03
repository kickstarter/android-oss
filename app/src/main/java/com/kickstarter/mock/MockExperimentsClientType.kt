package com.kickstarter.mock

import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.models.User
import rx.Observable
import rx.subjects.PublishSubject

open class MockExperimentsClientType : ExperimentsClientType {
    class ExperimentsEvent internal constructor(internal val eventKey: String, internal val attributes: MutableMap<String, *>)

    private val experimentEvents : PublishSubject<ExperimentsEvent> = PublishSubject.create()
    val eventKeys: Observable<String> = this.experimentEvents.map { e -> e.eventKey }

    override fun track(eventKey: String, user: User?, refTag: RefTag?) {
        this.experimentEvents.onNext(ExperimentsEvent(eventKey, attributes(user, refTag)))
    }

    override fun userId(user: User?): String = user?.id()?.toString()?: "0"

    override fun variant(experiment: OptimizelyExperiment.Key, user: User?, refTag: RefTag?): OptimizelyExperiment.Variant = OptimizelyExperiment.Variant.CONTROL

    override fun androidBuildVersion(): String = "9"
}
