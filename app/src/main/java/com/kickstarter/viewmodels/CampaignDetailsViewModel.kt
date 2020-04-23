package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.CampaignDetailsActivity
import com.kickstarter.ui.data.ProjectData
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface CampaignDetailsViewModel {

    interface Inputs {
        /** Call when the user clicks the pledge action button. */
        fun pledgeActionButtonClicked()
    }

    interface Outputs {
        /** Emits when we should return to the [com.kickstarter.ui.activities.ProjectActivity] with the rewards visible. */
        fun goBackToProject(): Observable<Void>

        /** Emits a boolean determining if the faux pledge container is visible. */
        fun pledgeContainerIsVisible(): Observable<Boolean>

        /** Emits the URL of the campaign to load in the web view. */
        fun url(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<CampaignDetailsActivity>(environment), Inputs, Outputs {
        private val currentUser = environment.currentUser()
        private val optimizely = environment.optimizely()

        private val pledgeButtonClicked = PublishSubject.create<Void>()

        private val goBackToProject = PublishSubject.create<Void>()
        private val pledgeContainerIsVisible = BehaviorSubject.create<Boolean>()
        private val url = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            val projectData = intent()
                    .map { it.getParcelableExtra(IntentKey.PROJECT_DATA) as ProjectData? }
                    .ofType(ProjectData::class.java)

            projectData
                    .filter { it.project().isLive && !it.project().isBacking }
                    .compose<Pair<ProjectData, User?>>(combineLatestPair(this.currentUser.observable()))
                    .map { ExperimentData(it.second, it.first.refTagFromIntent(), it.first.refTagFromCookie()) }
                    .map { this.optimizely.variant(OptimizelyExperiment.Key.CAMPAIGN_DETAILS, it) }
                    .map { it == OptimizelyExperiment.Variant.VARIANT_2 }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeContainerIsVisible)

            projectData
                    .filter { !it.project().isLive || it.project().isBacking }
                    .map { false }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeContainerIsVisible)

            projectData
                    .map { it.project() }
                    .map { it.descriptionUrl() }
                    .compose(bindToLifecycle())
                    .subscribe(this.url)

            this.pledgeButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.goBackToProject)

            projectData
                    .compose<ProjectData>(takeWhen(this.pledgeButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe { this.lake.trackCampaignDetailsPledgeButtonClicked(it) }
        }

        override fun pledgeActionButtonClicked() = this.pledgeButtonClicked.onNext(null)

        override fun goBackToProject(): Observable<Void> = this.goBackToProject

        override fun pledgeContainerIsVisible(): Observable<Boolean> = this.pledgeContainerIsVisible

        override fun url(): Observable<String> = this.url
    }
}
