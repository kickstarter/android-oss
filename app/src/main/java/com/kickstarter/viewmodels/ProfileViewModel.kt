package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.ApiPaginator
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.neverError
import com.kickstarter.libs.utils.IntegerUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.models.Project
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.ui.activities.ProfileActivity
import com.kickstarter.ui.adapters.ProfileAdapter
import com.kickstarter.ui.viewholders.EmptyProfileViewHolder
import com.kickstarter.ui.viewholders.ProfileCardViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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
        fun resumeDiscoveryActivity(): Observable<Void>

        /** Emits when we should start the [com.kickstarter.ui.activities.MessageThreadsActivity].  */
        fun startMessageThreadsActivity(): Observable<Void>

        /** Emits when we should start the [com.kickstarter.ui.activities.ProjectActivity].  */
        fun startProjectActivity(): Observable<Project>

        /** Emits the user name to be displayed.  */
        fun userNameTextViewText(): Observable<String>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<ProfileActivity>(environment), ProfileAdapter.Delegate, Inputs, Outputs {
        private val client: ApiClientType = environment.apiClient()
        private val currentUser: CurrentUserType = environment.currentUser()

        private val exploreProjectsButtonClicked = PublishSubject.create<Void>()
        private val messagesButtonClicked = PublishSubject.create<Void>()
        private val nextPage = PublishSubject.create<Void>()
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
        private val resumeDiscoveryActivity: Observable<Void>
        private val startProjectActivity: Observable<Project>
        private val startMessageThreadsActivity: Observable<Void>
        private val userNameTextViewText: Observable<String>

        val inputs: ProfileViewModel.Inputs = this
        val outputs: ProfileViewModel.Outputs = this

        init {

            val freshUser = this.client.fetchCurrentUser()
                    .retry(2)
                    .compose(neverError())
            freshUser.subscribe { this.currentUser.refresh(it) }

            val params = DiscoveryParams.builder()
                    .backed(1)
                    .perPage(18)
                    .sort(DiscoveryParams.Sort.ENDING_SOON)
                    .build()

            val paginator = ApiPaginator.builder<Project, DiscoverEnvelope, DiscoveryParams>()
                    .nextPage(this.nextPage)
                    .envelopeToListOfData { it.projects() }
                    .envelopeToMoreUrl { env -> env.urls().api().moreProjects() }
                    .loadWithParams { this.client.fetchProjects(params) }
                    .loadWithPaginationPath { this.client.fetchProjects(it) }
                    .build()
            
            paginator.isFetching
                    .compose(bindToLifecycle())
                    .subscribe(this.isFetchingProjects)
            
            val loggedInUser = this.currentUser.loggedInUser()

            this.avatarImageViewUrl = loggedInUser.map { u -> u.avatar().medium() }

            this.backedCountTextViewHidden = loggedInUser
                    .map { u -> IntegerUtils.isZero(u.backedProjectsCount()) }
            this.backedTextViewHidden = this.backedCountTextViewHidden

            this.backedCountTextViewText = loggedInUser
                    .map<Int> { it.backedProjectsCount() }
                    .filter { IntegerUtils.isNonZero(it) }
                    .map { NumberUtils.format(it) }

            this.createdCountTextViewHidden = loggedInUser
                    .map { u -> IntegerUtils.isZero(u.createdProjectsCount()) }
            this.createdTextViewHidden = this.createdCountTextViewHidden

            this.createdCountTextViewText = loggedInUser
                    .map<Int> { it.createdProjectsCount() }
                    .filter { IntegerUtils.isNonZero(it) }
                    .map { NumberUtils.format(it) }

            this.dividerViewHidden = Observable.combineLatest<Boolean, Boolean, Pair<Boolean, Boolean>>(
                    this.backedTextViewHidden,
                    this.createdTextViewHidden) { a, b -> Pair.create(a, b) }
                    .map { p -> p.first || p.second }

            this.projectList = paginator.paginatedData()
            this.resumeDiscoveryActivity = this.exploreProjectsButtonClicked
            this.startProjectActivity = this.projectCardClicked
            this.startMessageThreadsActivity = this.messagesButtonClicked
            this.userNameTextViewText = loggedInUser.map { it.name() }
        }

        override fun emptyProfileViewHolderExploreProjectsClicked(viewHolder: EmptyProfileViewHolder) = this.exploreProjectsButtonClicked()

        override fun exploreProjectsButtonClicked() = this.exploreProjectsButtonClicked.onNext(null)

        override fun messagesButtonClicked() = this.messagesButtonClicked.onNext(null)

        override fun nextPage() = this.nextPage.onNext(null)

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

        override fun startProjectActivity() = this.startProjectActivity

        override fun startMessageThreadsActivity() = this.startMessageThreadsActivity

        override fun userNameTextViewText() = this.userNameTextViewText
    }
}
