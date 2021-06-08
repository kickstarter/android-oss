package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.loadmore.LoadingType
import com.kickstarter.libs.loadmore.PaginatedViewModelOutput
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.CommentsActivity
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.ui.views.CommentComposerStatus
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import timber.log.Timber

interface CommentsViewModel {

    interface Inputs {
        fun refresh()
        fun nextPage()
        fun insertNewCommentToList(comment: String, createdAt: DateTime)
        fun onShowGuideLinesLinkClicked()
    }

    interface Outputs : PaginatedViewModelOutput<CommentCardData> {
        fun currentUserAvatar(): Observable<String?>
        fun commentComposerStatus(): Observable<CommentComposerStatus>
        fun enableReplyButton(): Observable<Boolean>
        fun showCommentComposer(): Observable<Boolean>
        fun commentsList(): Observable<Pair<List<CommentCardData>, Boolean>>
        fun setEmptyState(): Observable<Boolean>
        fun showCommentGuideLinesLink(): Observable<Void>
        fun initialLoadCommentsError(): Observable<Throwable>
        fun paginateCommentsError(): Observable<Throwable>
        fun pullToRefreshError(): Observable<Throwable>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<CommentsActivity>(environment), Inputs, Outputs {

        private val currentUser: CurrentUserType = environment.currentUser()
        private val client: ApiClientType = environment.apiClient()
        private val apolloClient: ApolloClientType = environment.apolloClient()
        val inputs: Inputs = this
        val outputs: Outputs = this
        private val refresh = PublishSubject.create<Void>()
        private val nextPage = PublishSubject.create<Void>()
        private val onShowGuideLinesLinkClicked = PublishSubject.create<Void>()

        private val currentUserAvatar = BehaviorSubject.create<String?>()
        private val commentComposerStatus = BehaviorSubject.create<CommentComposerStatus>()
        private val showCommentComposer = BehaviorSubject.create<Boolean>()
        private val commentsList = BehaviorSubject.create<Pair<List<CommentCardData>, Boolean>>()
        private val showGuideLinesLink = BehaviorSubject.create<Void>()
        private val disableReplyButton = BehaviorSubject.create<Boolean>()

        private val insertNewCommentToList = PublishSubject.create<Pair<String, DateTime>>()
        private val isLoadingMoreItems = BehaviorSubject.create<Boolean>()
        private val isRefreshing = BehaviorSubject.create<Boolean>()
        private val enablePagination = BehaviorSubject.create<Boolean>()
        private val setEmptyState = BehaviorSubject.create<Boolean>()
        private val insertComment = BehaviorSubject.create<CommentCardData>()

        // - Error observables to handle the 3 different use cases
        private val internalError = BehaviorSubject.create<Throwable>()
        private val initialError = BehaviorSubject.create<Throwable>()
        private val paginationError = BehaviorSubject.create<Throwable>()
        private val pullToRefreshError = BehaviorSubject.create<Throwable>()

        private var lastCommentCursor: String? = null
        override var loadMoreListData = mutableListOf<CommentCardData>()

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
                    showCommentComposer.onNext(true)
                }

            val projectOrUpdate = intent()
                .map<Any?> {
                    val project = it.getParcelableExtra(IntentKey.PROJECT) as? Project
                    val update = it.getParcelableExtra(IntentKey.UPDATE)as? Update
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

            initialProject
                .compose(combineLatestPair(currentUser.observable()))
                .compose(bindToLifecycle())
                .subscribe {
                    val composerStatus = getCommentComposerStatus(Pair(it.first, it.second))
                    showCommentComposer.onNext(composerStatus != CommentComposerStatus.GONE)
                    commentComposerStatus.onNext(composerStatus)
                }

            loadCommentList(initialProject)

            commentsList.map { it.first }
                .compose<Pair<List<CommentCardData>, User >>(combineLatestPair(this.currentUser.loggedInUser()))
                .compose(Transformers.takePairWhen(this.insertNewCommentToList))
                .map {
                    Pair(it.first.first, buildCommentBody(Pair(it.first.second, it.second)))
                }
                .compose(combineLatestPair(initialProject))
                .map {
                    Pair(
                        it.first.first,
                        CommentCardData.builder()
                            .comment(it.first.second)
                            .project(it.second)
                            .commentCardState(CommentCardStatus.TRYING_TO_POST.commentCardStatus)
                            .build()
                    )
                }
                .compose(bindToLifecycle())
                .subscribe {
                    it.first.toMutableList().apply {
                        add(0, it.second)
                        commentsList.onNext(Pair(this, true))
                    }
                }

            this.onShowGuideLinesLinkClicked
                .compose(bindToLifecycle())
                .subscribe {
                    showGuideLinesLink.onNext(null)
                }


            this.commentsList
                .map { it.size }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    this.setEmptyState.onNext(it == 0)
                }

            // TODO showcasing subscription to initialization to be completed on : https://kickstarter.atlassian.net/browse/NT-1951
            this.internalError
                .compose(combineLatestPair(commentsList))
                .filter { it.second.isEmpty() }
                .map { it.first }
                .subscribe {
                    it.localizedMessage
                    Timber.d("************ On initializing error")
                    this.initialError.onNext(it)
                }

            // TODO showcasing pagination error subscriptionto be completed on : https://kickstarter.atlassian.net/browse/NT-2019
            this.paginationError
                .subscribe {
                    it.localizedMessage
                    Timber.d("************ On pagination error")
                }
        }


        private fun loadCommentList(initialProject: Observable<Project>) {

            // - First load for comments & handle initial load errors
            getProjectComments(initialProject, this.internalError)
                .compose(bindToLifecycle())
                .subscribe {
                    bindCommentList(it.first, LoadingType.NORMAL)
                }

            // - Load comments from pagination & Handle pagination errors
            initialProject
                .compose(Transformers.takeWhen(this.nextPage))
                .doOnNext {
                    this.isLoadingMoreItems.onNext(true)
                }
                .switchMap { getProjectComments(Observable.just(it), this.paginationError) }
                .compose(bindToLifecycle())
                .subscribe {
                    updatePaginatedData(
                        LoadingType.LOAD_MORE,
                        it.first
                    )
                }

            // - Handle pull to refresh and it's errors
            initialProject
                .compose(Transformers.takeWhen(this.refresh))
                .doOnNext {
                    this.isRefreshing.onNext(true)
                    // reset cursor
                    lastCommentCursor = null
                }
                .switchMap { getProjectComments(Observable.just(it), this.pullToRefreshError) }
                .compose(bindToLifecycle())
                .subscribe {
                    bindCommentList(it.first, LoadingType.PULL_REFRESH)
                }
        }

        private fun getProjectComments(project: Observable<Project>, errorObservable: BehaviorSubject<Throwable>) = project.switchMap {
            return@switchMap apolloClient.getProjectComments(it.slug() ?: "", lastCommentCursor)
        }.doOnError {
            errorObservable.onNext(it)
        }
            .onErrorResumeNext(Observable.empty())
            .filter { ObjectUtils.isNotNull(it) }
            .compose<Pair<CommentEnvelope, Project>>(combineLatestPair(project))
            .map { Pair(requireNotNull(mapToCommentCardDataList(it)), it.first.totalCount) }

        private fun mapToCommentCardDataList(it: Pair<CommentEnvelope, Project>) =
            it.first.comments?.map { comment: Comment ->
                CommentCardData.builder().comment(comment).project(it.second).build()
            }

        private fun buildCommentBody(it: Pair<User, Pair<String, DateTime>>): Comment {
            return Comment.builder()
                .body(it.second.first)
                .parentId(-1)
                .authorBadges(listOf())
                .createdAt(it.second.second)
                .cursor("")
                .deleted(false)
                .id(-1)
                .repliesCount(0)
                .author(it.first)
                .build()
        }

        private fun bindCommentList(commentCardDataList: List<CommentCardData>, loadingType: LoadingType) {
            updatePaginatedData(
                loadingType,
                commentCardDataList
            )
        }

        private fun getCommentComposerStatus(projectAndUser: Pair<Project, User?>) =
            when {
                projectAndUser.second == null -> CommentComposerStatus.GONE
                projectAndUser.first.isBacking || ProjectUtils.userIsCreator(projectAndUser.first, projectAndUser.second) -> CommentComposerStatus.ENABLED
                else -> CommentComposerStatus.DISABLED
            }

        override fun refresh() = refresh.onNext(null)
        override fun nextPage() = nextPage.onNext(null)
        override fun onShowGuideLinesLinkClicked() = onShowGuideLinesLinkClicked.onNext(null)

        override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
        override fun commentComposerStatus(): Observable<CommentComposerStatus> = commentComposerStatus
        override fun showCommentComposer(): Observable<Boolean> = showCommentComposer
        override fun commentsList(): Observable<Pair<List<CommentCardData>, Boolean>> = commentsList
        override fun enableReplyButton(): Observable<Boolean> = disableReplyButton
        override fun showCommentGuideLinesLink(): Observable<Void> = showGuideLinesLink
        override fun initialLoadCommentsError(): Observable<Throwable> = this.initialError
        override fun paginateCommentsError(): Observable<Throwable> = this.paginationError
        override fun pullToRefreshError(): Observable<Throwable> = this.pullToRefreshError

        override fun setEmptyState(): Observable<Boolean> = setEmptyState
        override fun isLoadingMoreItems(): Observable<Boolean> = isLoadingMoreItems
        override fun enablePagination(): Observable<Boolean> = enablePagination
        override fun isRefreshing(): Observable<Boolean> = isRefreshing
        override fun insertNewCommentToList(comment: String, createdAt: DateTime) = insertNewCommentToList.onNext(Pair(comment, createdAt))

        override fun bindPaginatedData(data: List<CommentCardData>?) {
            lastCommentCursor = data?.lastOrNull()?.comment?.cursor()
            val newList = data?.let { it } ?: emptyList()

            appendMoreComments(newList)

            this.isRefreshing.onNext(false)
            this.isLoadingMoreItems.onNext(false)
        }

        private fun appendMoreComments(newList: List<CommentCardData>) {
            Observable.just(this.loadMoreListData)
                .compose(combineLatestPair(Observable.just(newList)))
                .distinctUntilChanged()
                .subscribe {
                    it.first.addAll(newList)
                    this.commentsList.onNext(it.first)
                }
        }

        override fun updatePaginatedState(enabled: Boolean) {
            enablePagination.onNext(enabled)
        }
    }
}
