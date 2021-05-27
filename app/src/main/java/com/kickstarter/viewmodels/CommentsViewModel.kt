package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.CommentsActivity
import com.kickstarter.ui.data.ProjectData
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface CommentsViewModel {

    interface Inputs {
        fun refresh()
        fun nextPage()
    }

    interface Outputs {
        fun currentUserAvatar(): Observable<String?>
        fun enableCommentComposer(): Observable<Boolean>
        fun showCommentComposer(): Observable<Void>
        fun commentsList(): Observable<List<Comment>>
        fun isLoadingMoreItems(): Observable<Boolean>
        fun isRefreshing(): Observable<Boolean>
        fun enableLoadMore(): Observable<Boolean>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<CommentsActivity>(environment), Inputs, Outputs {

        private val currentUser: CurrentUserType = environment.currentUser()
        private val client: ApiClientType = environment.apiClient()
        private val apolloClient: ApolloClientType = environment.apolloClient()
        val inputs: Inputs = this
        val outputs: Outputs = this
        private val refresh = PublishSubject.create<Void>()
        private val nextPage = PublishSubject.create<Void>()

        private val currentUserAvatar = BehaviorSubject.create<String?>()
        private val enableCommentComposer = BehaviorSubject.create<Boolean>()
        private val showCommentComposer = BehaviorSubject.create<Void>()
        private val commentsList = BehaviorSubject.create<List<Comment>?>()
        private val isLoadingMoreItems = BehaviorSubject.create<Boolean>()
        private val isRefreshing = BehaviorSubject.create<Boolean>()
        private val enableLoadMore = BehaviorSubject.create<Boolean>()

        var lastCommentCursour: String? = null
        protected var loadMoreListData = mutableListOf<Comment>()

        init {

            val loggedInUser = this.currentUser.loggedInUser()
                    .filter { u -> u != null }
                    .map { requireNotNull(it) }

            loggedInUser
                    .compose(bindToLifecycle())
                    .subscribe {
                        currentUserAvatar.onNext(it.avatar().small())
                    }

            loggedInUser
                    .compose(bindToLifecycle())
                    .subscribe {
                        showCommentComposer.onNext(null)
                    }

            intent()
                    .map { it.getParcelableExtra(IntentKey.PROJECT_DATA) as ProjectData? }
                    .ofType(ProjectData::class.java)
                    .compose(bindToLifecycle())
                    .subscribe {
                        enableCommentComposer.onNext(isProjectBackedOrUserIsCreator(Pair(it.project(), it.user())))
                    }

            val projectOrUpdate = intent()
                    .map<Any?> {
                        val project = it.getParcelableExtra(IntentKey.PROJECT) as? Project
                        val update = it.getParcelableExtra(IntentKey.UPDATE) as? Update
                        project?.let {
                            Either.Left<Project?, Update?>(it)
                        }
                                ?: Either.Right<Project?, Update?>(update)
                    }
                    .ofType(Either::class.java)
                    .take(1)

            val initialProject = projectOrUpdate.map {
                it as? Either<Project?, Update?>
            }.flatMap {
                it?.either<Observable<Project?>>(
                        { value: Project? -> Observable.just(value) },
                        { u: Update? -> client.fetchProject(u?.projectId().toString()).compose(Transformers.neverError()) }
                )
            }.map { requireNotNull(it) }
                    .share()

            Observable.combineLatest(
                    loggedInUser,
                    initialProject
            ) { a: User?, b: Project ->
                Pair.create(a, b)
            }.compose(bindToLifecycle())
                    .subscribe {
                        it.second?.let { project ->
                            enableCommentComposer.onNext(isProjectBackedOrUserIsCreator(Pair(project, it.first)))
                        }
                    }

            val projectSlug = initialProject
                    .map { requireNotNull(it?.slug()) }

            projectSlug.switchMap {
                        this.apolloClient.getProjectComments(it, null)
                    }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map {
                        lastCommentCursour = it.pageInfoEnvelope?.endCursor
                        it.comments
                    }
                    .filter { ObjectUtils.isNotNull(it) }

                    .compose(bindToLifecycle())
                    .subscribe {

                        updateLoadMoreListData(
                                LoadingType.NORMAL,
                                it
                        )
                    }

            projectSlug
                    .compose(Transformers.takeWhen(this.nextPage))
                    .switchMap {
                        this.isLoadingMoreItems.onNext(true)
                        it.let { slug ->
                            this.apolloClient.getProjectComments(slug, lastCommentCursour)
                        }
                    }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map {
                        lastCommentCursour = it.pageInfoEnvelope?.endCursor
                        it.comments
                    }
                    .compose(bindToLifecycle())
                    .subscribe {
                        updateLoadMoreListData(
                                LoadingType.LOAD_MORE,
                                it
                        )
                        this.isLoadingMoreItems.onNext(false)
                    }

            projectSlug
                    .compose(Transformers.takeWhen(this.refresh))
                    .switchMap {
                        this.isRefreshing.onNext(true)
                        it.let { slug ->
                            this.apolloClient.getProjectComments(slug, null)
                        }
                    }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map {
                        lastCommentCursour = it.pageInfoEnvelope?.endCursor
                        it.comments
                    }
                    .compose(bindToLifecycle())
                    .subscribe {
                        updateLoadMoreListData(
                                LoadingType.PULL_REFRESH,
                                it
                        )
                        this.isRefreshing.onNext(false)
                    }
        }

        private fun isProjectBackedOrUserIsCreator(pair: Pair<Project, User>) =
                pair.first.isBacking || ProjectUtils.userIsCreator(pair.first, pair.second)

        override fun refresh() = refresh.onNext(null)
        override fun nextPage() = nextPage.onNext(null)

        override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
        override fun enableCommentComposer(): Observable<Boolean> = enableCommentComposer
        override fun showCommentComposer(): Observable<Void> = showCommentComposer
        override fun commentsList(): Observable<List<Comment>> = commentsList
        override fun isLoadingMoreItems(): Observable<Boolean> = isLoadingMoreItems
        override fun enableLoadMore(): Observable<Boolean> = enableLoadMore
        override fun isRefreshing(): Observable<Boolean> = isRefreshing

        protected fun updateLoadMoreListData(loadingType: LoadingType, data: List<Comment>?) {
            if (loadingType != LoadingType.LOAD_MORE) {
                loadMoreListData = mutableListOf()
                updateLoadMoreState(true)
            }
            if (data?.isEmpty() == true) {
                if (loadingType == LoadingType.LOAD_MORE) {
                    updateLoadMoreState(false)
                }
            }
                data?.let { loadMoreListData.addAll(it) }

            commentsList.onNext(loadMoreListData)
        }

        private fun updateLoadMoreState(enabled: Boolean) {
            enableLoadMore.onNext(enabled)
        }
    }
}
enum class LoadingType {
    NORMAL,
    LOAD_MORE,
    PULL_REFRESH
}
