package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.services.apiresponses.InternalBuildEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.DownloadBetaActivity
import com.kickstarter.viewmodels.inputs.DownloadBetaViewModelInputs
import com.kickstarter.viewmodels.outputs.DownloadBetaViewModelOutputs
import rx.Observable
import rx.subjects.BehaviorSubject

class DownloadBetaViewModel(environment: Environment) :
    ActivityViewModel<DownloadBetaActivity>(environment),
    DownloadBetaViewModelInputs,
    DownloadBetaViewModelOutputs {

    val outputs: DownloadBetaViewModelOutputs = this
    private val internalBuildEnvelope = BehaviorSubject.create<InternalBuildEnvelope>()

    init {
        intent()
            .map<InternalBuildEnvelope> { it.getParcelableExtra(IntentKey.INTERNAL_BUILD_ENVELOPE) }
            .ofType(InternalBuildEnvelope::class.java)
            .compose(bindToLifecycle())
            .subscribe(internalBuildEnvelope)
    }
    override fun internalBuildEnvelope(): Observable<InternalBuildEnvelope> = internalBuildEnvelope
}
