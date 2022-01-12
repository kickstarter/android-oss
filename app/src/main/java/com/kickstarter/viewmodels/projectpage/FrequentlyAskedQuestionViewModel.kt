package com.kickstarter.viewmodels.projectpage

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.models.ProjectFaq
import com.kickstarter.models.User
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.projectpage.FrequentlyAskedQuestionFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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
        fun bindEmptyState(): Observable<Void>
        /** Emits a boolean that determines if the message icon should be shown. */
        fun askQuestionButtonIsGone(): Observable<Boolean>

        /** Emits when we should start the creator bio [com.kickstarter.ui.activities.MessageCreatorActivity]. */
        fun startComposeMessageActivity(): Observable<Project>

        /** Emits when we should start the creator bio [com.kickstarter.ui.activities.MessagesActivity]. */
        fun startMessagesActivity(): Observable<Project>
    }

    class ViewModel(@NonNull val environment: Environment) :
        FragmentViewModel<FrequentlyAskedQuestionFragment>(environment), Inputs, Outputs {

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val projectDataInput = BehaviorSubject.create<ProjectData>()
        private val askQuestionButtonClicked = PublishSubject.create<Void>()

        private val askQuestionButtonIsGone = BehaviorSubject.create<Boolean>()
        private val startComposeMessageActivity = PublishSubject.create<Project>()
        private val startMessageActivity = PublishSubject.create<Project>()

        private val projectFaqList = BehaviorSubject.create<List<ProjectFaq>>()
        private val bindEmptyState = BehaviorSubject.create<Void>()

        private val currentUser = environment.currentUser()

        init {
            val projectFaqList = projectDataInput
                .map { it.project().projectFaqs() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            projectFaqList
                .filter { it.isNotEmpty() }
                .compose(bindToLifecycle())
                .subscribe { this.projectFaqList.onNext(it) }

            projectFaqList
                .filter { it.isNullOrEmpty() }
                .compose(bindToLifecycle())
                .subscribe { this.bindEmptyState.onNext(null) }

            val project = projectDataInput
                .map { it.project() }

            this.currentUser.observable()
                .compose(Transformers.combineLatestPair(project))
                .map { userIsLoggedOutOrProjectCreator(it) }
                .compose(bindToLifecycle())
                .subscribe(this.askQuestionButtonIsGone)

            project
                .compose(Transformers.takeWhen(this.askQuestionButtonClicked))
                .filter { !it.isBacking() }
                .compose(bindToLifecycle())
                .subscribe(this.startComposeMessageActivity)

            project
                .compose(Transformers.takeWhen(this.askQuestionButtonClicked))
                .filter { it.isBacking() }
                .compose(bindToLifecycle())
                .subscribe(this.startMessageActivity)
        }

        private fun userIsLoggedOutOrProjectCreator(userAndProject: Pair<User, Project>) =
            userAndProject.first == null || userAndProject.first?.id() == userAndProject.second?.creator()?.id()

        override fun configureWith(projectData: ProjectData) = this.projectDataInput
            .onNext(projectData)

        override fun askQuestionButtonClicked() {
            this.askQuestionButtonClicked.onNext(null)
        }

        @NonNull
        override fun askQuestionButtonIsGone(): Observable<Boolean> = this.askQuestionButtonIsGone

        @NonNull
        override fun startComposeMessageActivity(): Observable<Project> = this.startComposeMessageActivity

        @NonNull
        override fun startMessagesActivity(): Observable<Project> = this.startMessageActivity

        @NonNull
        override fun projectFaqList(): Observable<List<ProjectFaq>> = this.projectFaqList
        @NonNull
        override fun bindEmptyState(): Observable<Void> = this.bindEmptyState
    }
}
