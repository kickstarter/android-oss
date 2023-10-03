package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

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

    class CreatorBioViewModel(environment: Environment, private val intent: Intent? = null) : ViewModel(), Inputs, Outputs {

        private val messageButtonClicked = PublishSubject.create<Unit>()

        private val messageIconIsGone = BehaviorSubject.createDefault(true)
        private val startComposeMessageActivity = PublishSubject.create<Project>()
        private val startMessageActivity = PublishSubject.create<Project>()
        private val url = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val currentUser = requireNotNull(environment.currentUserV2())
        private val disposables = CompositeDisposable()

        private fun intent() = this.intent?.let { Observable.just(it) } ?: Observable.empty()

        init {
            intent()
                .map { it.getStringExtra(IntentKey.URL) }
                .ofType(String::class.java)
                .subscribe { this.url.onNext(it) }
                .addToDisposable(disposables)

            val project = intent()
                .map { it.getParcelableExtra(IntentKey.PROJECT) as Project? }
                .filter { it.isNotNull() }
                .map { it }

            this.currentUser.observable()
                .compose(combineLatestPair<KsOptional<User>, Project>(project))
                .filter { it.first.isPresent() && it.first.getValue().isNotNull() }
                .map { Pair(requireNotNull(it.first.getValue()), it.second) }
                .map {
                    userIsLoggedOutOrProjectCreator(it)
                }
                .subscribe {
                    this.messageIconIsGone.onNext(it)
                }
                .addToDisposable(disposables)

            project
                .compose<Project>(takeWhenV2(this.messageButtonClicked))
                .filter { !it.isBacking() }
                .subscribe {
                    this.startComposeMessageActivity.onNext(it)
                }
                .addToDisposable(disposables)

            project
                .compose<Project>(takeWhenV2(this.messageButtonClicked))
                .filter { it.isBacking() }
                .subscribe {
                    this.startMessageActivity.onNext(it)
                }
                .addToDisposable(disposables)
        }

        private fun userIsLoggedOutOrProjectCreator(userAndProject: Pair<User, Project>) =
            userAndProject.first == null || userAndProject.first?.id() == userAndProject.second?.creator()?.id()

        override fun messageButtonClicked() {
            this.messageButtonClicked.onNext(Unit)
        }
        override fun messageIconIsGone(): Observable<Boolean> = this.messageIconIsGone

        override fun startComposeMessageActivity(): Observable<Project> = this.startComposeMessageActivity

        override fun startMessagesActivity(): Observable<Project> = this.startMessageActivity

        override fun url(): Observable<String> = this.url

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        class Factory(private val environment: Environment, private val intent: Intent? = null) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreatorBioViewModel(environment, intent) as T
            }
        }
    }
}
