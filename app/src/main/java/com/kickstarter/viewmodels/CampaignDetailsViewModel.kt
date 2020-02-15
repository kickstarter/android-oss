package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.CampaignDetailsActivity
import rx.Observable
import rx.subjects.BehaviorSubject

interface CampaignDetailsViewModel {

    interface Inputs

    interface Outputs {
        /** Emits the URL of the campaign to load in the web view. */
        fun url(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<CampaignDetailsActivity>(environment), Inputs, Outputs {

        private val url = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            val project = intent()
                    .map { it.getParcelableExtra(IntentKey.PROJECT) as Project? }
                    .ofType(Project::class.java)

            project
                    .map { it.descriptionUrl() }
                    .compose(bindToLifecycle())
                    .subscribe(this.url)
        }

        @NonNull
        override fun url(): Observable<String> = this.url
    }
}
