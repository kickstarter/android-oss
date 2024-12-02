package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.loadmore.ApolloPaginateV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takePairWhenV2
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.models.User
import com.kickstarter.models.extensions.cardStatus
import com.kickstarter.models.extensions.updateCanceledPledgeComment
import com.kickstarter.models.extensions.updateCommentAfterSuccessfulPost
import com.kickstarter.models.extensions.updateCommentFailedToPost
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.ui.views.CommentComposerStatus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

interface CommentsViewModel {

    interface Inputs {
        fun refresh()
        fun nextPage()
        fun backPressed()
        fun insertNewCommentToList(comment: String, createdAt: DateTime)
        fun onReplyClicked(comment: Comment, openKeyboard: Boolean)
        fun onShowGuideLinesLinkClicked()
        fun checkIfThereAnyPendingComments(isBackAction: Boolean)

        /** Will be called with the successful response when calling the `postComment` Mutation **/
        fun refreshComment(comment: Comment, position: Int)
        fun refreshCommentCardInCaseFailedPosted(comment: Comment, position: Int)
        fun onShowCanceledPledgeComment(comment: Comment)
        fun onResumeActivity()
    }

    interface Outputs {
        fun closeCommentsPage(): Observable<Unit>
        fun currentUserAvatar(): Observable<String?>
        fun commentComposerStatus(): Observable<CommentComposerStatus>
        fun enableReplyButton(): Observable<Boolean>
        fun showCommentComposer(): Observable<Boolean>
        fun commentsList(): Observable<List<CommentCardData>>
        fun scrollToTop(): Observable<Boolean>
        fun setEmptyState(): Observable<Boolean>
        fun showCommentGuideLinesLink(): Observable<Unit>
        fun initialLoadCommentsError(): Observable<Throwable>
        fun paginateCommentsError(): Observable<Throwable>
        fun pullToRefreshError(): Observable<Throwable>
        fun startThreadActivity(): Observable<Pair<Pair<CommentCardData, Boolean>, String?>>
        fun startThreadActivityFromDeepLink(): Observable<Pair<CommentCardData, String?>>
        fun hasPendingComments(): Observable<Pair<Boolean, Boolean>>

        /** Emits a boolean indicating whether comments are being fetched from the API.  */
        fun isFetchingComments(): Observable<Boolean>

        /** Display the bottom pagination Error Cell **/
        fun shouldShowPaginationErrorUI(): Observable<Boolean>
        /** Display the initial Load Error Cell **/
        fun shouldShowInitialLoadErrorUI(): Observable<Boolean>
    }

    class CommentsViewModel(val environment: Environment, private val intent: Intent? = null) : ViewModel(), Inputs, Outputs {

        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val currentUserStream = requireNotNull(environment.currentUserV2())
        private val analyticEvents = requireNotNull(environment.analytics())

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val backPressed = PublishSubject.create<Unit>()
        private val refresh = PublishSubject.create<Unit>()
        private val nextPage = PublishSubject.create<Unit>()
        private val onShowGuideLinesLinkClicked = PublishSubject.create<Unit>()
        private val onReplyClicked = PublishSubject.create<Pair<Comment, Boolean>>()
        private val checkIfThereAnyPendingComments = PublishSubject.create<Boolean>()
        private val failedCommentCardToRefresh = PublishSubject.create<Pair<Comment, Int>>()
        private val showCanceledPledgeComment = PublishSubject.create<Comment>()
        private val onResumeActivity = PublishSubject.create<Unit>()

        private val closeCommentsPage = BehaviorSubject.create<Unit>()
        private val currentUserAvatar = BehaviorSubject.create<String?>()
        private val commentComposerStatus = BehaviorSubject.create<CommentComposerStatus>()
        private val showCommentComposer = BehaviorSubject.create<Boolean>()
        private val commentsList = BehaviorSubject.create<List<CommentCardData>>()
        private val outputCommentList = BehaviorSubject.create<List<CommentCardData>>()
        private val showGuideLinesLink = BehaviorSubject.create<Unit>()
        private val disableReplyButton = BehaviorSubject.create<Boolean>()
        private val scrollToTop = BehaviorSubject.create<Boolean>()

        private val insertNewCommentToList = PublishSubject.create<Pair<String, DateTime>>()
        private val isRefreshing = BehaviorSubject.create<Boolean>()
        private val setEmptyState = BehaviorSubject.create<Boolean>()
        private val displayInitialError = BehaviorSubject.create<Boolean>()
        private val displayPaginationError = BehaviorSubject.create<Boolean>()
        private val commentToRefresh = PublishSubject.create<Pair<Comment, Int>>()
        private val startThreadActivity = BehaviorSubject.create<Pair<Pair<CommentCardData, Boolean>, String?>>()
        private val startThreadActivityFromDeepLink = BehaviorSubject.create<Pair<CommentCardData, String?>>()
        private val hasPendingComments = BehaviorSubject.create<Pair<Boolean, Boolean>>()

        // - Error observables to handle the 3 different use cases
        private val internalError = BehaviorSubject.create<Throwable>()
        private val initialError = BehaviorSubject.create<Throwable>()
        private val paginationError = BehaviorSubject.create<Throwable>()
        private val pullToRefreshError = BehaviorSubject.create<Throwable>()
        private var commentableId = BehaviorSubject.create<String>()

        private val isFetchingComments = BehaviorSubject.create<Boolean>()
        private lateinit var project: Project

        private var openedThreadActivityFromDeepLink = false

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val newlyPostedCommentsList = mutableListOf<CommentCardData>()

        private val disposables = CompositeDisposable()

        private var currentUser: User? = null
        private fun intent() = intent?.let { Observable.just(it) } ?: Observable.empty()
        init {

            val loggedInUser = this.currentUserStream.loggedInUser()
                .map {
                    currentUser = it
                    it
                }

            loggedInUser
                .subscribe {
                    currentUserAvatar.onNext(it.avatar().small())
                }
                .addToDisposable(disposables)

            val projectOrUpdate = intent()
                .map<Any?> {
                    val projectData = it.getParcelableExtra(IntentKey.PROJECT_DATA) as? ProjectData
                    val update = it.getParcelableExtra(IntentKey.UPDATE)as? Update
                    projectData?.project()?.let {
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
                    { u: Update? ->
                        apolloClient.getProject(u?.projectId().toString())
                            .compose(Transformers.neverErrorV2())
                    }
                )
            }.map {
                requireNotNull(it)
            }.doOnNext {
                this.project = it
            }
                .share()

            initialProject
                .compose(combineLatestPair(currentUserStream.observable()))
                .subscribe {
                    val composerStatus = getCommentComposerStatus(Pair(it.first, it.second))
                    showCommentComposer.onNext(composerStatus != CommentComposerStatus.GONE)
                    commentComposerStatus.onNext(composerStatus)
                }
                .addToDisposable(disposables)

            val projectOrUpdateComment = projectOrUpdate.map {
                it as? Either<Project?, Update?>
            }.compose(combineLatestPair(initialProject))
                .map {
                    Pair(it.second, it.first?.right())
                }

            loadCommentListFromProjectOrUpdate(projectOrUpdateComment)

            val deepLinkCommentableId = intent()
                .filter { it.getStringExtra(IntentKey.COMMENT)?.isNotEmpty() ?: false }
                .map { requireNotNull(it.getStringExtra(IntentKey.COMMENT)) }

            projectOrUpdateComment
                .distinctUntilChanged()
                .compose(
                    // check if the activity opened by deeplink action
                    combineLatestPair(
                        intent().map {
                            it.hasExtra(IntentKey.COMMENT)
                        }
                    )
                )
                .filter {
                    !it.second
                }
                .map { it.first }
                .subscribe {
                    trackRootCommentPageViewEvent(it)
                }
                .addToDisposable(disposables)

            projectOrUpdateComment
                .compose(Transformers.takeWhenV2(onResumeActivity))
                .subscribe {
                    // send event after back action after deep link to thread activity
                    if (openedThreadActivityFromDeepLink) {
                        trackRootCommentPageViewEvent(it)
                        openedThreadActivityFromDeepLink = false
                    }
                }
                .addToDisposable(disposables)

            deepLinkCommentableId
                .compose(takePairWhenV2(projectOrUpdateComment))
                .switchMap {
                    return@switchMap apolloClient.getComment(it.first)
                }.compose(Transformers.neverErrorV2())
                .compose(combineLatestPair(deepLinkCommentableId))
                .compose(combineLatestPair(commentableId))
                .compose(combineLatestPair(currentUserStream.observable()))
                .filter { it.first.second.isNotEmpty() && it.second.isPresent() }
                .map {
                    CommentCardData.builder()
                        .comment(it.first.first.first)
                        .project(this.project)
                        .commentCardState(it.first.first.first.cardStatus(it.second.getValue()))
                        .commentableId(it.first.second)
                        .build()
                }.withLatestFrom(projectOrUpdateComment) { commentData, projectOrUpdate ->
                    Pair(commentData, projectOrUpdate)
                }
                .subscribe {
                    this.startThreadActivityFromDeepLink.onNext(Pair(it.first, it.second.second?.id()?.toString()))
                    openedThreadActivityFromDeepLink = true
                }
                .addToDisposable(disposables)

            this.insertNewCommentToList
                .withLatestFrom(this.currentUserStream.loggedInUser()) {
                        comment, user ->
                    Pair(comment, user)
                }
                .map {
                    Pair(it.first, buildCommentBody(Pair(it.second, it.first)))
                }
                .withLatestFrom(initialProject) {
                        commentData, project ->
                    Pair(commentData, project)
                }
                .compose(combineLatestPair(commentableId))
                .map {
                    Pair(
                        it.first.first,
                        CommentCardData.builder()
                            .comment(it.first.first.second)
                            .project(it.first.second)
                            .commentableId(it.second)
                            .commentCardState(CommentCardStatus.TRYING_TO_POST.commentCardStatus)
                            .build()
                    )
                }
                .distinctUntilChanged { prev, curr ->
                    prev.first.first.second.equals(curr.first.first.second)
                }
                .doOnNext {
                    this.scrollToTop.onNext(true)
                }
                .withLatestFrom(this.commentsList) { it, list ->
                    newlyPostedCommentsList.add(0, it.second)
                    list.toMutableList().apply {
                        add(0, it.second)
                    }.toList()
                }
                .subscribe {
                    commentsList.onNext(it)
                }
                .addToDisposable(disposables)

            this.commentToRefresh
                .map { it.first }
                .distinctUntilChanged()
                .withLatestFrom(projectOrUpdateComment) {
                        commentData, project ->
                    Pair(commentData, project)
                }.subscribe {
                    this.analyticEvents.trackCommentCTA(it.second.first, it.first.id().toString(), it.first.body(), it.second.second?.id()?.toString())
                }
                .addToDisposable(disposables)

            this.onShowGuideLinesLinkClicked
                .subscribe {
                    showGuideLinesLink.onNext(Unit)
                }
                .addToDisposable(disposables)

            this.commentsList
                .map { it.size }
                .distinctUntilChanged()
                .subscribe {
                    this.setEmptyState.onNext(it == 0)
                }
                .addToDisposable(disposables)

            this.initialError
                .subscribe {
                    this.displayInitialError.onNext(true)
                }
                .addToDisposable(disposables)

            this.paginationError
                .subscribe {
                    this.displayPaginationError.onNext(true)
                }
                .addToDisposable(disposables)

            this.commentsList
                .compose(takePairWhenV2(checkIfThereAnyPendingComments))
                .subscribe { pair ->
                    this.hasPendingComments.onNext(
                        Pair(
                            pair.first.any {
                                it.commentCardState == CommentCardStatus.TRYING_TO_POST.commentCardStatus ||
                                    it.commentCardState == CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus
                            },
                            pair.second
                        )
                    )
                }
                .addToDisposable(disposables)

            this.backPressed
                .subscribe { this.closeCommentsPage.onNext(it) }
                .addToDisposable(disposables)

            commentsList
                .withLatestFrom(projectOrUpdateComment) { commentData, projectOrUpdate ->
                    Pair(commentData, projectOrUpdate)
                }
                .compose(takePairWhenV2(onReplyClicked))
                .subscribe { pair ->

                    val cardData =
                        pair.first.first.first { it.comment?.id() == pair.second.first.id() }
                    val threadData = Pair(
                        cardData,
                        pair.second.second
                    )
                    this.startThreadActivity.onNext(
                        Pair(
                            threadData,
                            pair.first.second.second?.id()?.toString()
                        )
                    )
                }
                .addToDisposable(disposables)

            // - Update internal mutable list with the latest state after successful response
            this.commentsList
                .compose(takePairWhenV2(this.commentToRefresh))
                .map {
                    val mappedList = it.second.first.updateCommentAfterSuccessfulPost(it.first, it.second.second)
                    updateNewlyPostedCommentWithNewStatus(mappedList[it.second.second])
                    mappedList
                }
                .distinctUntilChanged()
                .doOnNext {
                    this.scrollToTop.onNext(false)
                }
                .subscribe {
                    this.commentsList.onNext(it)
                }
                .addToDisposable(disposables)

            // - Reunite in only one place where the output list gets new updates
            this.commentsList
                .filter { it.isNotEmpty() }
                .subscribe {
                    this.outputCommentList.onNext(it)
                }
                .addToDisposable(disposables)

            // - Update internal mutable list with the latest state after failed response
            this.commentsList
                .compose(takePairWhenV2(this.failedCommentCardToRefresh))
                .map {
                    val mappedList = it.second.first.updateCommentFailedToPost(it.first, it.second.second)
                    updateNewlyPostedCommentWithNewStatus(mappedList[it.second.second])
                    mappedList
                }
                .distinctUntilChanged()
                .subscribe {
                    this.commentsList.onNext(it)
                }
                .addToDisposable(disposables)

            this.commentsList
                .compose(takePairWhenV2(this.showCanceledPledgeComment))
                .map {
                    it.second.updateCanceledPledgeComment(it.first)
                }
                .distinctUntilChanged()
                .subscribe {
                    this.commentsList.onNext(it)
                }
                .addToDisposable(disposables)
        }

        private fun updateNewlyPostedCommentWithNewStatus(
            updatedComment: CommentCardData
        ) {
            this.newlyPostedCommentsList.indexOfFirst { item ->
                item.commentableId == updatedComment.commentableId
            }.also { index ->
                newlyPostedCommentsList[index] = updatedComment
            }
        }

        private fun trackRootCommentPageViewEvent(it: Pair<Project, Update?>) {
            if (it.second?.id() != null) {
                this.analyticEvents.trackRootCommentPageViewed(
                    it.first,
                    it.second?.id()?.toString()
                )
            } else {
                this.analyticEvents.trackRootCommentPageViewed(it.first)
            }
        }

        private fun loadCommentListFromProjectOrUpdate(projectOrUpdate: Observable<Pair<Project, Update?>>) {
            val startOverWith =
                Observable.merge(
                    projectOrUpdate,
                    projectOrUpdate.compose(
                        Transformers.takeWhenV2(
                            refresh
                        )
                    )
                )

            val apolloPaginate =
                ApolloPaginateV2.builder<CommentCardData, CommentEnvelope, Pair<Project, Update?>>()
                    .nextPage(nextPage)
                    .distinctUntilChanged(true)
                    .startOverWith(startOverWith)
                    .envelopeToListOfData {
                        mapToCommentCardDataList(Pair(it, this.project), currentUser)
                    }
                    .loadWithParams {
                        loadWithProjectOrUpdateComments(Observable.just(it.first), it.second)
                    }
                    .clearWhenStartingOver(false)
                    .build()

            apolloPaginate.isFetching
                .share()
                .subscribe { this.isFetchingComments.onNext(it) }
                .addToDisposable(disposables)

            apolloPaginate.paginatedData()
                ?.share()
                ?.map {
                    if (this.newlyPostedCommentsList.isNotEmpty()) {
                        this.newlyPostedCommentsList + it
                    } else {
                        it
                    }
                }
                ?.subscribe {
                    this.commentsList.onNext(it)
                }
                ?.addToDisposable(disposables)

            this.internalError
                .map { Pair(it, commentsList.value) }
                .filter {
                    it.second.isNullOrEmpty()
                }
                .subscribe {
                    this.initialError.onNext(it.first)
                }
                .addToDisposable(disposables)

            commentsList.compose(takePairWhenV2(this.internalError))
                .filter {
                    it.first.isNotEmpty()
                }
                .subscribe {
                    this.paginationError.onNext(it.second)
                }
                .addToDisposable(disposables)

            this.refresh
                .doOnNext {
                    this.isRefreshing.onNext(true)
                }
                .subscribe {
                    this.newlyPostedCommentsList.clear()
                }
                .addToDisposable(disposables)

            this.internalError
                .compose(combineLatestPair(isRefreshing))
                .compose(combineLatestPair(commentsList))
                .filter {
                    it.second.isNullOrEmpty()
                }
                .subscribe {
                    this.isRefreshing.onNext(false)
                }
                .addToDisposable(disposables)
        }

        private fun loadWithProjectOrUpdateComments(
            projectOrUpdate: Observable<Pair<Project, Update?>>,
            cursor: String
        ): Observable<CommentEnvelope> {
            return projectOrUpdate.switchMap {
                return@switchMap if (it.second?.id() != null) {
                    apolloClient.getProjectUpdateComments(it.second?.id().toString(), cursor)
                } else {
                    apolloClient.getProjectComments(it.first?.slug() ?: "", cursor)
                }
            }.doOnNext {
                it.commentableId?.let { comId ->
                    commentableId.onNext(comId)
                } ?: commentableId.onNext("")

                // Remove Pagination errorFrom View
                this.displayPaginationError.onNext(false)
                this.displayInitialError.onNext(false)
            }
                .doOnError {
                    this.internalError.onNext(it)
                }
                .onErrorResumeNext(Observable.empty())
        }

        private fun mapToCommentCardDataList(it: Pair<CommentEnvelope, Project>, currentUser: User?) =
            it.first.comments?. filter { item ->
                filterCancelledPledgeWithoutRepliesComment(item)
            }?.map { comment: Comment ->
                CommentCardData.builder()
                    .comment(comment)
                    .project(it.second)
                    .commentCardState(comment.cardStatus(currentUser))
                    .commentableId(it.first.commentableId)
                    .build()
            } ?: emptyList()

        private fun filterCancelledPledgeWithoutRepliesComment(item: Comment) =
            !(item.authorCanceledPledge() && item.repliesCount() == 0)

        private fun buildCommentBody(it: Pair<User, Pair<String, DateTime>>): Comment {
            return Comment.builder()
                .body(it.second.first)
                .parentId(-1)
                .authorBadges(listOf())
                .createdAt(it.second.second)
                .cursor("")
                .deleted(false)
                .id(-1)
                .authorCanceledPledge(false)
                .repliesCount(0)
                .author(it.first)
                .build()
        }

        private fun getCommentComposerStatus(projectAndUser: Pair<Project, KsOptional<User>>) =
            when {
                projectAndUser.second.getValue() == null -> CommentComposerStatus.GONE
                projectAndUser.first.canComment() ?: false -> CommentComposerStatus.ENABLED
                else -> CommentComposerStatus.DISABLED
            }

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        // - Inputs
        override fun backPressed() = backPressed.onNext(Unit)
        override fun refresh() = refresh.onNext(Unit)
        override fun nextPage() = nextPage.onNext(Unit)
        override fun insertNewCommentToList(comment: String, createdAt: DateTime) = insertNewCommentToList.onNext(Pair(comment, createdAt))
        override fun onShowGuideLinesLinkClicked() = onShowGuideLinesLinkClicked.onNext(Unit)
        override fun refreshComment(comment: Comment, position: Int) = this.commentToRefresh.onNext(Pair(comment, position))
        override fun onReplyClicked(comment: Comment, openKeyboard: Boolean) = onReplyClicked.onNext(Pair(comment, openKeyboard))
        override fun checkIfThereAnyPendingComments(isBackAction: Boolean) = checkIfThereAnyPendingComments.onNext(isBackAction)
        override fun refreshCommentCardInCaseFailedPosted(comment: Comment, position: Int) =
            this.failedCommentCardToRefresh.onNext(Pair(comment, position))
        override fun onShowCanceledPledgeComment(comment: Comment) =
            this.showCanceledPledgeComment.onNext(comment)

        override fun onResumeActivity() =
            this.onResumeActivity.onNext(Unit)
        // - Outputs
        override fun closeCommentsPage(): Observable<Unit> = closeCommentsPage
        override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
        override fun commentComposerStatus(): Observable<CommentComposerStatus> = commentComposerStatus
        override fun showCommentComposer(): Observable<Boolean> = showCommentComposer
        override fun commentsList(): Observable<List<CommentCardData>> = this.outputCommentList
        override fun enableReplyButton(): Observable<Boolean> = disableReplyButton
        override fun showCommentGuideLinesLink(): Observable<Unit> = showGuideLinesLink
        override fun initialLoadCommentsError(): Observable<Throwable> = this.initialError
        override fun paginateCommentsError(): Observable<Throwable> = this.paginationError
        override fun pullToRefreshError(): Observable<Throwable> = this.pullToRefreshError
        override fun scrollToTop(): Observable<Boolean> = this.scrollToTop
        override fun shouldShowInitialLoadErrorUI(): Observable<Boolean> = this.displayInitialError
        override fun shouldShowPaginationErrorUI(): Observable<Boolean> = this.displayPaginationError

        override fun setEmptyState(): Observable<Boolean> = setEmptyState

        override fun startThreadActivity(): Observable<Pair<Pair<CommentCardData, Boolean>, String?>> = this.startThreadActivity
        override fun startThreadActivityFromDeepLink(): Observable<Pair<CommentCardData, String?>> = this.startThreadActivityFromDeepLink

        override fun isFetchingComments(): Observable<Boolean> = this.isFetchingComments

        override fun hasPendingComments(): Observable<Pair<Boolean, Boolean>> = this.hasPendingComments
    }

    class Factory(private val environment: Environment, private val intent: Intent? = null) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CommentsViewModel(environment, intent) as T
        }
    }
}
