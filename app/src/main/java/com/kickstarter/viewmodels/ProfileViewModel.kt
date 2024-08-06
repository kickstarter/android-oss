package com.kickstarter.viewmodels

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.ApiPaginatorV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.neverErrorV2
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isZero
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.ui.adapters.ProfileAdapter
import com.kickstarter.ui.viewholders.EmptyProfileViewHolder
import com.kickstarter.ui.viewholders.ProfileCardViewHolder
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface ProfileViewModel {

    interface Inputs {
        /** Call when the Explore Projects button in the empty state has been clicked.  */
        fun exploreProjectsButtonClicked()

        /** Call when the messages button has been clicked.  */
        fun messagesButtonClicked()

        /** Call when the next page has been invoked.  */
        fun nextPage()

        /** Call when a project card has been clicked.  */
        fun projectCardClicked(project: Project)
    }

    interface Outputs {
        /** Emits the user avatar image to be displayed.  */
        fun avatarImageViewUrl(): Observable<String>

        /** Emits when the backed projects count should be hidden.  */
        fun backedCountTextViewHidden(): Observable<Boolean>

        /** Emits the backed projects count to be displayed.  */
        fun backedCountTextViewText(): Observable<String>

        /** Emits when the backed projects text view should be hidden.  */
        fun backedTextViewHidden(): Observable<Boolean>

        /** Emits when the created projects count should be hidden.  */
        fun createdCountTextViewHidden(): Observable<Boolean>

        /** Emits the created projects count to be displayed.  */
        fun createdCountTextViewText(): Observable<String>

        /** Emits when the created projects text view should be hidden.  */
        fun createdTextViewHidden(): Observable<Boolean>

        /** Emits when the divider view should be hidden.  */
        fun dividerViewHidden(): Observable<Boolean>

        /** Emits a boolean indicating whether projects are being fetched from the API.  */
        fun isFetchingProjects(): Observable<Boolean>

        /** Emits a list of projects to display in the profile.  */
        fun projectList(): Observable<List<Project>>

        /** Emits when we should resume the [com.kickstarter.ui.activities.DiscoveryActivity].  */
        fun resumeDiscoveryActivity(): Observable<Unit>

        /** Emits when we should start the [com.kickstarter.ui.activities.MessageThreadsActivity].  */
        fun startMessageThreadsActivity(): Observable<Unit>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectActivity].  */
        fun startProjectActivity(): Observable<Project>

        /** Emits the user name to be displayed.  */
        fun userNameTextViewText(): Observable<String>
    }

    class ProfileViewModel(environment: Environment) : ViewModel(), ProfileAdapter.Delegate, Inputs, Outputs {
        private val client = requireNotNull(environment.apiClientV2())
        private val currentUser = requireNotNull(environment.currentUserV2())
        private val analytics = requireNotNull(environment.analytics())

        private val exploreProjectsButtonClicked = PublishSubject.create<Unit>()
        private val messagesButtonClicked = PublishSubject.create<Unit>()
        private val nextPage = PublishSubject.create<Unit>()
        private val refresh = PublishSubject.create<DiscoveryParams>()
        private val projectCardClicked = PublishSubject.create<Project>()

        private val avatarImageViewUrl: Observable<String>
        private val backedCountTextViewHidden: Observable<Boolean>
        private val backedCountTextViewText: Observable<String>
        private val backedTextViewHidden: Observable<Boolean>
        private val createdCountTextViewHidden: Observable<Boolean>
        private val createdCountTextViewText: Observable<String>
        private val createdTextViewHidden: Observable<Boolean>
        private val dividerViewHidden: Observable<Boolean>
        private val isFetchingProjects = BehaviorSubject.create<Boolean>()
        private val projectList: Observable<List<Project>>
        private val resumeDiscoveryActivity: Observable<Unit>
        private val startMessageThreadsActivity: Observable<Unit>
        private val userNameTextViewText: Observable<String>

        val inputs: Inputs = this
        val outputs: Outputs = this

        val disposables = CompositeDisposable()

        init {
            val freshUser = this.client.fetchCurrentUser()
                .retry(2)
                .compose(neverErrorV2())

            freshUser.subscribe { this.currentUser.refresh(it) }
                .addToDisposable(disposables)

            val params = DiscoveryParams.builder()
                .backed(1)
                .perPage(18)
                .sort(DiscoveryParams.Sort.ENDING_SOON)
                .build()

            val paginator = ApiPaginatorV2.builder<Project, DiscoverEnvelope, DiscoveryParams>()
                .nextPage(nextPage)
                .startOverWith(Observable.just(params))
                .envelopeToListOfData { it.projects() }
                .envelopeToMoreUrl { env -> env.urls()?.api()?.moreProjects() }
                .loadWithParams { this.client.fetchProjects(params).compose(neverErrorV2()) }
                .loadWithPaginationPath { this.client.fetchProjects(it).compose(neverErrorV2()) }
                .build()

            paginator.isFetching
                .subscribe { this.isFetchingProjects.onNext(it) }
                .addToDisposable(disposables)

            val loggedInUser = this.currentUser.loggedInUser()

            this.avatarImageViewUrl = loggedInUser.map { u -> u.avatar().medium() }

            this.backedCountTextViewHidden = loggedInUser
                .map { u -> u.backedProjectsCount().isZero() }
            this.backedTextViewHidden = this.backedCountTextViewHidden

            this.backedCountTextViewText = loggedInUser
                .map<Int> { it.backedProjectsCount() }
                .filter { it.isNonZero() }
                .map { NumberUtils.format(it) }

            this.createdCountTextViewHidden = loggedInUser
                .map { u -> u.createdProjectsCount().isZero() }
            this.createdTextViewHidden = this.createdCountTextViewHidden

            this.createdCountTextViewText = loggedInUser
                .map<Int> { it.createdProjectsCount() }
                .filter { it.isNonZero() }
                .map { NumberUtils.format(it) }

            this.dividerViewHidden = Observable.combineLatest<Boolean, Boolean, Pair<Boolean, Boolean>>(
                this.backedTextViewHidden,
                this.createdTextViewHidden
            ) { a, b -> Pair.create(a, b) }
                .map { p -> p.first || p.second }

            this.projectList = paginator.paginatedData().filter { it.isNotNull() }
            this.resumeDiscoveryActivity = this.exploreProjectsButtonClicked

            this.startMessageThreadsActivity = this.messagesButtonClicked
            this.userNameTextViewText = loggedInUser.map { it.name() }

            projectCardClicked
                .subscribe { analytics.trackProjectCardClicked(it, EventContextValues.ContextPageName.PROFILE.contextName) }
                .addToDisposable(disposables)
        }

        override fun emptyProfileViewHolderExploreProjectsClicked(viewHolder: EmptyProfileViewHolder) = this.exploreProjectsButtonClicked()

        override fun exploreProjectsButtonClicked() = this.exploreProjectsButtonClicked.onNext(Unit)

        override fun messagesButtonClicked() = this.messagesButtonClicked.onNext(Unit)

        override fun nextPage() = this.nextPage.onNext(Unit)

        override fun profileCardViewHolderClicked(viewHolder: ProfileCardViewHolder, project: Project) = this.projectCardClicked(project)

        override fun projectCardClicked(project: Project) = this.projectCardClicked.onNext(project)

        override fun avatarImageViewUrl() = this.avatarImageViewUrl

        override fun backedCountTextViewText() = this.backedCountTextViewText

        override fun backedCountTextViewHidden() = this.backedCountTextViewHidden

        override fun backedTextViewHidden() = this.backedTextViewHidden

        override fun createdCountTextViewHidden() = this.createdCountTextViewHidden

        override fun createdCountTextViewText() = this.createdCountTextViewText

        override fun createdTextViewHidden() = this.createdTextViewHidden

        override fun dividerViewHidden() = this.dividerViewHidden

        override fun isFetchingProjects(): BehaviorSubject<Boolean> = this.isFetchingProjects

        override fun projectList() = this.projectList

        override fun resumeDiscoveryActivity() = this.resumeDiscoveryActivity

        override fun startProjectActivity() = this.projectCardClicked

        override fun startMessageThreadsActivity() = this.startMessageThreadsActivity

        override fun userNameTextViewText() = this.userNameTextViewText

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(environment) as T
        }
    }
}
