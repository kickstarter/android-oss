package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.CreatorBioActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface CreatorBioViewModel {
    interface Inputs {
        fun messageButtonClicked()
    }
    interface Outputs {
        fun messageIconIsGone(): Observable<Boolean>

        fun startComposeMessageActivity(): Observable<Project>

        fun startMessageActivity(): Observable<Project>

        /** Emits a URL to load in the web view.  */
        fun url(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<CreatorBioActivity>(environment), Inputs, Outputs {

        private val messageButtonClicked = PublishSubject.create<Void>()

        private val messageIconIsGone = PublishSubject.create<Boolean>()
        private val startComposeMessageActivity = PublishSubject.create<Project>()
        private val startMessageActivity = PublishSubject.create<Project>()
        private val url = BehaviorSubject.create<String>()

        val inputs : Inputs = this
        val outputs : Outputs = this

        init {
            intent()
                    .map { it.getStringExtra(IntentKey.URL) }
                    .ofType(String::class.java)
                    .compose(bindToLifecycle())
                    .subscribe(this.url)

            val project = intent()
                    .map { it.getParcelableExtra(IntentKey.PROJECT) as Project }

            project
                    .compose<Project>(takeWhen(this.messageButtonClicked))
                    .filter { !it.isBacking }
                    .compose(bindToLifecycle())
                    .subscribe(this.startComposeMessageActivity)
            project
                    .compose<Project>(takeWhen(this.messageButtonClicked))
                    .filter { it.isBacking }
                    .compose(bindToLifecycle())
                    .subscribe(this.startMessageActivity)

        }

        override fun messageButtonClicked() {
            this.messageButtonClicked.onNext(null)
        }

        @NonNull
        override fun messageIconIsGone(): Observable<Boolean> = this.messageIconIsGone

        @NonNull
        override fun startComposeMessageActivity(): Observable<Project> = this.startComposeMessageActivity

        @NonNull
        override fun startMessageActivity(): Observable<Project> = this.startMessageActivity

        @NonNull
        override fun url(): Observable<String> = this.url

    }
}
