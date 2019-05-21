package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.CreatorBioActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface CreatorBioViewModel {

    interface Inputs {
        /** Call when message button has been clicked. */
        fun messageButtonClicked()
    }

    interface Outputs {
        /** Emits a boolean that determines if the message icon should be shown. */
        fun messageIconIsGone(): Observable<Boolean>

        /** Emits when we should start the creator bio [com.kickstarter.ui.activities.MessageCreatorActivity]. */
        fun startComposeMessageActivity(): Observable<Project>

        /** Emits when we should start the creator bio [com.kickstarter.ui.activities.MessagesActivity]. */
        fun startMessagesActivity(): Observable<Project>

        /** Emits the URL of the creator's bio to load in the web view. */
        fun url(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<CreatorBioActivity>(environment), Inputs, Outputs {

        private val messageButtonClicked = PublishSubject.create<Void>()

        private val messageIconIsGone = BehaviorSubject.create<Boolean>()
        private val startComposeMessageActivity = PublishSubject.create<Project>()
        private val startMessageActivity = PublishSubject.create<Project>()
        private val url = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val currentUser = environment.currentUser()

        init {
            intent()
                    .map { it.getStringExtra(IntentKey.URL) }
                    .ofType(String::class.java)
                    .compose(bindToLifecycle())
                    .subscribe(this.url)

            val project = intent()
                    .map { it.getParcelableExtra(IntentKey.PROJECT) as Project }

            this.currentUser.observable()
                    .compose(combineLatestPair<User, Project>(project))
                    .map { userIsLoggedOutOrProjectCreator(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.messageIconIsGone)

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

            project
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackViewedCreatorBioModal(it) }

        }

        private fun userIsLoggedOutOrProjectCreator(userAndProject: Pair<User, Project>) =
                userAndProject.first == null || userAndProject.first?.id() == userAndProject.second?.creator()?.id()

        override fun messageButtonClicked() {
            this.messageButtonClicked.onNext(null)
        }

        @NonNull
        override fun messageIconIsGone(): Observable<Boolean> = this.messageIconIsGone

        @NonNull
        override fun startComposeMessageActivity(): Observable<Project> = this.startComposeMessageActivity

        @NonNull
        override fun startMessagesActivity(): Observable<Project> = this.startMessageActivity

        @NonNull
        override fun url(): Observable<String> = this.url

    }
}
