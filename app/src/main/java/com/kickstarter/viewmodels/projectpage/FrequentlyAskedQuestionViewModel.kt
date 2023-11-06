package com.kickstarter.viewmodels.projectpage

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Project
import com.kickstarter.models.ProjectFaq
import com.kickstarter.models.User
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface FrequentlyAskedQuestionViewModel {

    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)

        /** Call when message button has been clicked. */
        fun askQuestionButtonClicked()
    }

    interface Outputs {
        /** Emits the current list [ProjectFaq]. */
        fun projectFaqList(): Observable<List<ProjectFaq>>
        fun bindEmptyState(): Observable<Unit>
        /** Emits a boolean that determines if the message icon should be shown. */
        fun askQuestionButtonIsGone(): Observable<Boolean>

        /** Emits when we should start the creator bio [com.kickstarter.ui.activities.MessageCreatorActivity]. */
        fun startComposeMessageActivity(): Observable<Project>

        /** Emits when we should start the creator bio [com.kickstarter.ui.activities.MessagesActivity]. */
        fun startMessagesActivity(): Observable<Project>
    }

    class FrequentlyAskedQuestionViewModel(val environment: Environment) :
        ViewModel(), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectDataInput = PublishSubject.create<ProjectData>()
        private val askQuestionButtonClicked = PublishSubject.create<Unit>()

        private val askQuestionButtonIsGone = BehaviorSubject.create<Boolean>()
        private val startComposeMessageActivity = PublishSubject.create<Project>()
        private val startMessageActivity = PublishSubject.create<Project>()

        private val projectFaqList = BehaviorSubject.create<List<ProjectFaq>>()
        private val bindEmptyState = BehaviorSubject.create<Unit>()

        private val currentUser = requireNotNull(environment.currentUserV2())

        private val disposables = CompositeDisposable()

        init {
            val projectFaqList = projectDataInput
                .filter { it.project().projectFaqs().isNotNull() }
                .map { requireNotNull(it.project().projectFaqs()) }

            projectFaqList
                .filter { it.isNotEmpty() }
                .subscribe { this.projectFaqList.onNext(it) }
                .addToDisposable(disposables)

            projectDataInput
                .filter { it.project().projectFaqs().isNullOrEmpty() }
                .subscribe {
                    this.bindEmptyState.onNext(Unit)
                }
                .addToDisposable(disposables)

            val project = projectDataInput
                .map { it.project() }

            this.currentUser.observable()
                .compose(Transformers.combineLatestPair(project))
                .map { userIsLoggedOutOrProjectCreator(Pair(it.first.getValue(), it.second)) }
                .subscribe { this.askQuestionButtonIsGone.onNext(it) }
                .addToDisposable(disposables)

            project
                .compose(Transformers.takeWhenV2(this.askQuestionButtonClicked))
                .filter { !it.isBacking() }
                .subscribe { this.startComposeMessageActivity.onNext(it) }
                .addToDisposable(disposables)

            project
                .compose(Transformers.takeWhenV2(this.askQuestionButtonClicked))
                .filter { it.isBacking() }
                .subscribe { this.startMessageActivity.onNext(it) }
                .addToDisposable(disposables)
        }

        private fun userIsLoggedOutOrProjectCreator(userAndProject: Pair<User, Project>) =
            userAndProject.first == null || userAndProject.first?.id() == userAndProject.second?.creator()?.id()

        override fun configureWith(projectData: ProjectData) = this.projectDataInput
            .onNext(projectData)

        override fun askQuestionButtonClicked() {
            this.askQuestionButtonClicked.onNext(Unit)
        }

        override fun askQuestionButtonIsGone(): Observable<Boolean> = this.askQuestionButtonIsGone

        override fun startComposeMessageActivity(): Observable<Project> = this.startComposeMessageActivity

        override fun startMessagesActivity(): Observable<Project> = this.startMessageActivity

        override fun projectFaqList(): Observable<List<ProjectFaq>> = this.projectFaqList

        override fun bindEmptyState(): Observable<Unit> = this.bindEmptyState

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FrequentlyAskedQuestionViewModel(environment) as T
        }
    }
}
