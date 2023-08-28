package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProgressBarUtils
import com.kickstarter.libs.utils.extensions.deadlineCountdownValue
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.ui.adapters.data.ProjectDashboardData
import com.kickstarter.ui.viewholders.CreatorDashboardHeaderViewHolder
import rx.Observable
import rx.subjects.PublishSubject

interface CreatorDashboardHeaderHolderViewModel {
    interface Inputs {
        /** Call to configure the view model with ProjectDashboardData.  */
        fun configureWith(projectDashboardData: ProjectDashboardData)

        /** Call when the messages button is clicked.  */
        fun messagesButtonClicked()

        /** Call when the project button is clicked.  */
        fun projectButtonClicked()
    }

    interface Outputs {
        /** project that is currently being viewed  */
        fun currentProject(): Observable<Project>

        /** Emits when the messages button should be gone.  */
        fun messagesButtonIsGone(): Observable<Boolean>

        /** Emits when the other projects button should be gone.  */
        fun otherProjectsButtonIsGone(): Observable<Boolean>

        /** string number with the percentage of a projects funding  */
        fun percentageFunded(): Observable<String>

        /** Emits the percentage funded amount for display in the progress bar.  */
        fun percentageFundedProgress(): Observable<Int>

        /** Emits color of progress bar based on project state.  */
        fun progressBarBackground(): Observable<Int>

        /** localized count of number of backers  */
        fun projectBackersCountText(): Observable<String>

        /** current project's name  */
        fun projectNameTextViewText(): Observable<String>

        /** Emits when we should start the [com.kickstarter.ui.activities.MessageThreadsActivity].  */
        fun startMessageThreadsActivity(): Observable<Pair<Project, RefTag>>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectPageActivity].  */
        fun startProjectActivity(): Observable<Pair<Project, RefTag>>

        /** Emits the time remaining for current project with no units.  */
        fun timeRemainingText(): Observable<String>

        /** Emits a boolean determining if the view project button should be gone.  */
        fun viewProjectButtonIsGone(): Observable<Boolean>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<CreatorDashboardHeaderViewHolder>(environment), Inputs, Outputs {
        private val currentUser = requireNotNull(environment.currentUser())

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val messagesButtonClicked = PublishSubject.create<Void?>()
        private val projectButtonClicked = PublishSubject.create<Void?>()
        private val projectDashboardData = PublishSubject.create<ProjectDashboardData>()
        private val currentProject: Observable<Project>
        private val messagesButtonIsGone: Observable<Boolean>
        private val otherProjectsButtonIsGone: Observable<Boolean>
        private val percentageFunded: Observable<String>
        private val percentageFundedProgress: Observable<Int>
        private val progressBarBackground: Observable<Int>
        private val projectBackersCountText: Observable<String>
        private val projectNameTextViewText: Observable<String>
        private val startMessageThreadsActivity: Observable<Pair<Project, RefTag>>
        private val startProjectActivity: Observable<Pair<Project, RefTag>>
        private val timeRemainingText: Observable<String>
        private val viewProjectButtonIsGone: Observable<Boolean>

        override fun configureWith(projectDashboardData: ProjectDashboardData) {
            this.projectDashboardData.onNext(projectDashboardData)
        }

        override fun messagesButtonClicked() {
            messagesButtonClicked.onNext(null)
        }

        override fun projectButtonClicked() {
            projectButtonClicked.onNext(null)
        }

        override fun currentProject(): Observable<Project> {
            return currentProject
        }

        override fun messagesButtonIsGone(): Observable<Boolean> {
            return messagesButtonIsGone
        }

        override fun otherProjectsButtonIsGone(): Observable<Boolean> {
            return otherProjectsButtonIsGone
        }

        override fun percentageFunded(): Observable<String> {
            return percentageFunded
        }

        override fun percentageFundedProgress(): Observable<Int> {
            return percentageFundedProgress
        }

        override fun progressBarBackground(): Observable<Int> {
            return progressBarBackground
        }

        override fun projectBackersCountText(): Observable<String> {
            return projectBackersCountText
        }

        override fun projectNameTextViewText(): Observable<String> {
            return projectNameTextViewText
        }

        override fun startMessageThreadsActivity(): Observable<Pair<Project, RefTag>> {
            return startMessageThreadsActivity
        }

        override fun startProjectActivity(): Observable<Pair<Project, RefTag>> {
            return startProjectActivity
        }

        override fun timeRemainingText(): Observable<String> {
            return timeRemainingText
        }

        override fun viewProjectButtonIsGone(): Observable<Boolean> {
            return viewProjectButtonIsGone
        }

        init {
            val user = currentUser.observable()
            val singleProjectView = projectDashboardData
                .map(ProjectDashboardData::isViewingSingleProject)

            otherProjectsButtonIsGone = user
                .map { obj: User -> obj.memberProjectsCount() }
                .map { count: Int? -> count ?: 0 }
                .map { count: Int -> count <= 1 }
                .compose(Transformers.combineLatestPair(singleProjectView))
                .map { onlyOneProjectAndSingleProjectView: Pair<Boolean, Boolean> -> onlyOneProjectAndSingleProjectView.first || onlyOneProjectAndSingleProjectView.second }
                .compose(bindToLifecycle())

            currentProject = projectDashboardData
                .map(ProjectDashboardData::project)
                .compose(bindToLifecycle())

            messagesButtonIsGone = currentProject
                .compose(Transformers.combineLatestPair(user))
                .map { projectAndUser: Pair<Project, User> ->
                    projectAndUser.first.creator().id() != projectAndUser.second.id()
                }
                .compose(bindToLifecycle())

            percentageFunded = currentProject
                .map { p: Project -> NumberUtils.flooredPercentage(p.percentageFunded()) }
                .compose(bindToLifecycle())

            percentageFundedProgress = currentProject
                .map { p: Project -> ProgressBarUtils.progress(p.percentageFunded()) }
                .compose(bindToLifecycle())

            progressBarBackground = currentProject
                .map { p: Project -> p.isLive || p.isStarted || p.isSubmitted || p.isSuccessful }
                .map { liveStartedSubmittedSuccessful: Boolean -> if (liveStartedSubmittedSuccessful) R.drawable.progress_bar_green_horizontal else R.drawable.progress_bar_grey_horizontal }
                .compose(bindToLifecycle())

            projectBackersCountText = currentProject
                .map { obj: Project -> obj.backersCount() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { NumberUtils.format(it) }
                .compose(bindToLifecycle())

            projectNameTextViewText = currentProject
                .map { obj: Project -> obj.name() }
                .distinctUntilChanged()
                .compose(bindToLifecycle())

            startMessageThreadsActivity = currentProject
                .compose(Transformers.takeWhen(messagesButtonClicked))
                .map<Pair<Project, RefTag>> { p: Project -> Pair.create(p, RefTag.dashboard()) }
                .compose(bindToLifecycle())

            startProjectActivity = currentProject
                .compose(Transformers.takeWhen(projectButtonClicked))
                .map { p: Project -> Pair(p, RefTag.dashboard()) }
                .compose(bindToLifecycle())

            timeRemainingText = currentProject
                .map { it.deadlineCountdownValue() }
                .map { NumberUtils.format(it) }
                .compose(bindToLifecycle())

            viewProjectButtonIsGone = singleProjectView
                .compose(bindToLifecycle())
        }
    }
}
