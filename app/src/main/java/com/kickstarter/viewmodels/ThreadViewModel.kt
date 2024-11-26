package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.loadmore.ApolloPaginateV2
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.toCommentCardList
import com.kickstarter.libs.utils.extensions.userIsCreator
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.models.extensions.updateCanceledPledgeComment
import com.kickstarter.models.extensions.updateCommentAfterSuccessfulPost
import com.kickstarter.models.extensions.updateCommentFailedToPost
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.ui.views.CommentComposerStatus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime

interface ThreadViewModel {

    interface Inputs {
        fun nextPage()
        fun reloadRepliesPage()
        fun insertNewReplyToList(comment: String, createdAt: DateTime)
        fun onShowGuideLinesLinkClicked()
        fun refreshCommentCardInCaseFailedPosted(comment: Comment, position: Int)
        fun refreshCommentCardInCaseSuccessPosted(comment: Comment, position: Int)
        fun checkIfThereAnyPendingComments()
        fun backPressed()
        fun onShowCanceledPledgeComment(comment: Comment)
    }

    interface Outputs {
        /** The anchored root comment */
        fun getRootComment(): Observable<CommentCardData>

        /** get comment replies **/
        fun onCommentReplies(): Observable<Pair<List<CommentCardData>, Boolean>>

        /** Will tell to the compose view if should open the keyboard */
        fun shouldFocusOnCompose(): Observable<Boolean>
        fun scrollToBottom(): Observable<Unit>

        fun currentUserAvatar(): Observable<String?>
        fun replyComposerStatus(): Observable<CommentComposerStatus>
        fun showReplyComposer(): Observable<Boolean>

        fun isFetchingReplies(): Observable<Boolean>
        fun loadMoreReplies(): Observable<Unit>

        /** Display the pagination Error Cell **/
        fun shouldShowPaginationErrorUI(): Observable<Boolean>

        /** Display the Initial Error Cell **/
        fun initialLoadCommentsError(): Observable<Boolean>

        fun refresh(): Observable<Unit>

        fun showCommentGuideLinesLink(): Observable<Unit>
        fun hasPendingComments(): Observable<Boolean>
        fun closeThreadActivity(): Observable<Unit>
    }

    class ThreadViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {
        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val currentUserStream = requireNotNull(environment.currentUserV2())
        private val analyticEvents = requireNotNull(environment.analytics())
        private val nextPage = PublishSubject.create<Unit>()
        private val onLoadingReplies = PublishSubject.create<Unit>()
        private val insertNewReplyToList = PublishSubject.create<Pair<String, DateTime>>()
        private val onShowGuideLinesLinkClicked = PublishSubject.create<Unit>()
        private val failedCommentCardToRefresh = PublishSubject.create<Pair<Comment, Int>>()
        private val successfullyPostedCommentCardToRefresh =
            PublishSubject.create<Pair<Comment, Int>>()
        private val checkIfThereAnyPendingComments = PublishSubject.create<Unit>()
        private val backPressed = PublishSubject.create<Unit>()
        private val showCanceledPledgeComment = PublishSubject.create<Comment>()

        private val rootComment = BehaviorSubject.create<CommentCardData>()
        private val focusOnCompose = BehaviorSubject.create<Boolean>()
        private val currentUserAvatar = BehaviorSubject.create<String?>()
        private val replyComposerStatus = BehaviorSubject.create<CommentComposerStatus>()
        private val showReplyComposer = BehaviorSubject.create<Boolean>()
        private val scrollToBottom = BehaviorSubject.create<Unit>()
        private val isFetchingReplies = BehaviorSubject.create<Boolean>()
        private val hasPreviousElements = BehaviorSubject.create<Boolean>()
        private val refresh = PublishSubject.create<Unit>()
        private val loadMoreReplies = PublishSubject.create<Unit>()
        private val displayPaginationError = BehaviorSubject.create<Boolean>()
        private val initialLoadCommentsError = BehaviorSubject.create<Boolean>()
        private val showGuideLinesLink = BehaviorSubject.create<Unit>()
        private val hasPendingComments = BehaviorSubject.create<Boolean>()
        private val closeThreadActivity = BehaviorSubject.create<Unit>()

        private val intent = PublishSubject.create<Intent>()

        private val disposables = CompositeDisposable()

        private val onCommentReplies =
            BehaviorSubject.create<Pair<List<CommentCardData>, Boolean>>()
        private var project: Project? = null
        private var currentUser: User? = null

        val inputs = this
        val outputs = this

        // - Error observables to handle the 3 different use cases
        private val internalError = BehaviorSubject.create<Throwable>()
        private val initialError = BehaviorSubject.create<Throwable>()
        private val paginationError = BehaviorSubject.create<Throwable>()

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val newlyPostedRepliesList = mutableListOf<CommentCardData>()

        init {

            val commentData = getCommentCardDataFromIntent()
                .distinctUntilChanged()
                .filter { it.isNotNull() }
                .map { it }

            intent
                .map { it.getBooleanExtra(IntentKey.REPLY_EXPAND, false) }
                .distinctUntilChanged()
                .subscribe { this.focusOnCompose.onNext(it) }
                .addToDisposable(disposables)

            val comment =
                getCommentCardDataFromIntent().map { it.comment }.map { it }

            val project = commentData
                .filter { it.project.isNotNull() }
                .map { requireNotNull(it.project) }

            project.take(1)
                .subscribe {
                    this.project = it
                }
                .addToDisposable(disposables)

            currentUserStream
                .observable()
                .take(1)
                .subscribe { this.currentUser = it.getValue() }
                .addToDisposable(disposables)

            loadCommentListFromProjectOrUpdate(comment)

            this.insertNewReplyToList
                .distinctUntilChanged()
                .withLatestFrom(this.currentUserStream.loggedInUser()) { comment, user ->
                    Pair(comment, user)
                }
                .withLatestFrom(commentData) { reply, parent ->
                    Pair(reply, parent)
                }
                .map {
                    Pair(
                        it.second,
                        buildReplyBody(Pair(Pair(it.second, it.first.second), it.first.first))
                    )
                }
                .map {
                    CommentCardData.builder()
                        .comment(it.second)
                        .project(it.first.project)
                        .commentableId(it.first.commentableId)
                        .commentCardState(CommentCardStatus.TRYING_TO_POST.commentCardStatus)
                        .build()
                }
                .withLatestFrom(this.onCommentReplies) { reply, pair ->
                    Pair(
                        pair.first.toMutableList().apply {
                            newlyPostedRepliesList.add(0, reply)
                            /** bind new reply at the top of list to as list is reversed  **/
                            add(0, reply)
                        }.toList(),
                        pair.second
                    )
                }
                .subscribe {
                    onCommentReplies.onNext(it)
                    scrollToBottom.onNext(Unit)
                }
                .addToDisposable(disposables)

            commentData
                .subscribe {
                    this.rootComment.onNext(it)
                }
                .addToDisposable(disposables)

            val loggedInUser = this.currentUserStream.loggedInUser()
                .map { it }

            loggedInUser
                .subscribe {
                    currentUserAvatar.onNext(it.avatar().small())
                }
                .addToDisposable(disposables)

            loggedInUser
                .subscribe {
                    showReplyComposer.onNext(true)
                }
                .addToDisposable(disposables)

            project
                .compose(Transformers.combineLatestPair(currentUserStream.observable()))
                .subscribe {
                    val composerStatus =
                        getCommentComposerStatus(Pair(it.first, it.second.getValue()))
                    showReplyComposer.onNext(composerStatus != CommentComposerStatus.GONE)
                    replyComposerStatus.onNext(composerStatus)
                }
                .addToDisposable(disposables)

            project
                .compose(Transformers.combineLatestPair(this.getProjectUpdateId()))
                .compose(Transformers.combineLatestPair(comment))
                .distinctUntilChanged()
                .subscribe {
                    analyticEvents.trackThreadCommentPageViewed(
                        it.first.first,
                        it.second.id().toString(),
                        it.first.second
                    )
                }
                .addToDisposable(disposables)

            this.onLoadingReplies
                .map {
                    this.onCommentReplies.value ?: Pair(listOf(), false)
                }
                .subscribe {
                    if (it?.first?.isNotEmpty() == true) {
                        this.loadMoreReplies.onNext(Unit)
                    } else {
                        this.refresh.onNext(Unit)
                    }
                }
                .addToDisposable(disposables)

            this.initialError
                .subscribe {
                    this.initialLoadCommentsError.onNext(true)
                }
                .addToDisposable(disposables)

            this.paginationError
                .subscribe {
                    this.displayPaginationError.onNext(true)
                }
                .addToDisposable(disposables)

            this.onShowGuideLinesLinkClicked
                .subscribe {
                    showGuideLinesLink.onNext(Unit)
                }
                .addToDisposable(disposables)

            // - Update internal mutable list with the latest state after failed response
            this.onCommentReplies
                .compose(Transformers.combineLatestPair(this.failedCommentCardToRefresh))
                .map {
                    val mappedList =
                        it.second.first.updateCommentFailedToPost(it.first.first, it.second.second)
                    updateNewlyPostedCommentWithNewStatus(mappedList[it.second.second])
                    Pair(mappedList, it.first.second)
                }
                .distinctUntilChanged()
                .subscribe { this.onCommentReplies.onNext(it) }
                .addToDisposable(disposables)

            // - Update internal mutable list with the latest state after successful response

            this.successfullyPostedCommentCardToRefresh
                .map { it.first }
                .compose(Transformers.combineLatestPair(this.getProjectUpdateId()))
                .compose(Transformers.combineLatestPair(project))
                .compose(Transformers.combineLatestPair(comment))
                .distinctUntilChanged()
                .subscribe {
                    analyticEvents.trackRootCommentReplyCTA(
                        it.first.second,
                        it.first.first.first.id().toString(),
                        it.first.first.first.body(), it.second.id().toString(),
                        it.first.first.second
                    )
                }
                .addToDisposable(disposables)

            this.onCommentReplies
                .compose(Transformers.combineLatestPair(this.successfullyPostedCommentCardToRefresh))
                .map {
                    val mappedList = it.second.first.updateCommentAfterSuccessfulPost(
                        it.first.first,
                        it.second.second
                    )
                    updateNewlyPostedCommentWithNewStatus(mappedList[it.second.second])
                    Pair(mappedList, it.first.second)
                }
                .distinctUntilChanged()
                .subscribe {
                    this.onCommentReplies.onNext(it)
                }
                .addToDisposable(disposables)

            this.onCommentReplies
                .compose(Transformers.takePairWhenV2(this.showCanceledPledgeComment))
                .map {
                    Pair(it.second.updateCanceledPledgeComment(it.first.first), it.first.second)
                }
                .distinctUntilChanged()
                .subscribe {
                    this.onCommentReplies.onNext(it)
                }
                .addToDisposable(disposables)

            checkIfThereAnyPendingComments
                .withLatestFrom(this.onCommentReplies) { _, list ->
                    list
                }
                .subscribe { pair ->
                    this.hasPendingComments.onNext(
                        pair.first.any {
                            it.commentCardState == CommentCardStatus.TRYING_TO_POST.commentCardStatus ||
                                it.commentCardState == CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus
                        }
                    )
                }
                .addToDisposable(disposables)

            this.backPressed
                .subscribe { this.closeThreadActivity.onNext(it) }
                .addToDisposable(disposables)

            intent
                .map { it.getBooleanExtra(IntentKey.REPLY_SCROLL_BOTTOM, false) }
                .filter { it }
                .compose(Transformers.takeWhenV2(this.onCommentReplies))
                .distinctUntilChanged()
                .subscribe {
                    scrollToBottom.onNext(Unit)
                }
                .addToDisposable(disposables)
        }

        private fun updateNewlyPostedCommentWithNewStatus(
            updatedComment: CommentCardData
        ) {
            this.newlyPostedRepliesList.indexOfFirst { item ->
                item.commentableId == updatedComment.commentableId
            }.also { index ->
                newlyPostedRepliesList[index] = updatedComment
            }
        }

        private fun loadCommentListFromProjectOrUpdate(comment: Observable<Comment>) {
            val startOverWith =
                Observable.merge(
                    comment,
                    comment.compose(
                        Transformers.takeWhenV2(
                            refresh
                        )
                    )
                )

            val apolloPaginate =
                ApolloPaginateV2.builder<CommentCardData, CommentEnvelope, Comment?>()
                    .nextPage(nextPage)
                    .distinctUntilChanged(true)
                    .startOverWith(startOverWith)
                    .envelopeToListOfData {
                        hasPreviousElements.onNext(it.pageInfoEnvelope()?.hasPreviousPage ?: false)
                        this.project?.let { project -> mapListToData(it, project, currentUser) }
                    }
                    .loadWithParams {
                        loadWithProjectReplies(
                            Observable.just(it.first), it.second
                        )
                    }
                    .isReversed(true)
                    .clearWhenStartingOver(true)
                    .build()

            apolloPaginate
                .isFetching
                .share()
                .subscribe { this.isFetchingReplies.onNext(it) }
                .addToDisposable(disposables)

            /** reversed replies **/
            apolloPaginate
                .paginatedData()
                ?.map { it.asReversed() }
                ?.compose(Transformers.combineLatestPair(this.hasPreviousElements))
                ?.distinctUntilChanged()
                ?.share()
                ?.map {
                    if (this.newlyPostedRepliesList.isNotEmpty()) {
                        Pair(this.newlyPostedRepliesList + it.first, it.second)
                    } else {
                        it
                    }
                }
                ?.subscribe {
                    this.onCommentReplies.onNext(it)
                }
                ?.addToDisposable(disposables)

            this.internalError
                .subscribe {
                    this.initialError.onNext(it)
                }
                .addToDisposable(disposables)

            this.onCommentReplies
                .compose(Transformers.takePairWhenV2(internalError))
                .filter {
                    it.first.first.isNotEmpty()
                }
                .subscribe {
                    this.paginationError.onNext(it.second)
                }
                .addToDisposable(disposables)
        }

        private fun mapListToData(it: CommentEnvelope, project: Project, currentUser: User?) =
            it.comments?.toCommentCardList(
                project, currentUser
            )

        private fun loadWithProjectReplies(
            comment: Observable<Comment>,
            cursor: String?
        ): Observable<CommentEnvelope> {
            return comment.switchMap {
                return@switchMap this.apolloClient.getRepliesForComment(
                    it,
                    if (cursor.isNullOrEmpty()) null else cursor
                )
            }.doOnError {
                this.internalError.onNext(it)
            }.onErrorResumeNext(Observable.empty())
        }

        private fun buildReplyBody(it: Pair<Pair<CommentCardData, User>, Pair<String, DateTime>>): Comment {
            return Comment.builder()
                .body(it.second.first)
                .parentId(it.first.first.comment?.id() ?: -1)
                .authorBadges(listOf())
                .createdAt(it.second.second)
                .cursor("")
                .deleted(false)
                .id(-1)
                .repliesCount(0)
                .authorCanceledPledge(false)
                .author(it.first.second)
                .build()
        }

        private fun getCommentComposerStatus(projectAndUser: Pair<Project, User?>) =
            when {
                projectAndUser.second == null -> CommentComposerStatus.GONE
                projectAndUser.first.isBacking() || projectAndUser.first.userIsCreator(
                    projectAndUser.second
                ) -> CommentComposerStatus.ENABLED

                else -> CommentComposerStatus.DISABLED
            }

        private fun getCommentCardDataFromIntent() = intent
            .filter {
                it.getParcelableExtra<CommentCardData?>(IntentKey.COMMENT_CARD_DATA).isNotNull()
            }
            .map { it.getParcelableExtra<CommentCardData>(IntentKey.COMMENT_CARD_DATA) as CommentCardData }
            .ofType(CommentCardData::class.java)

        private fun getProjectUpdateId() = intent
            .map { it.getStringExtra(IntentKey.UPDATE_POST_ID) ?: "" }

        override fun nextPage() = nextPage.onNext(Unit)
        override fun reloadRepliesPage() = onLoadingReplies.onNext(Unit)
        override fun refreshCommentCardInCaseFailedPosted(comment: Comment, position: Int) =
            this.failedCommentCardToRefresh.onNext(Pair(comment, position))

        override fun refreshCommentCardInCaseSuccessPosted(comment: Comment, position: Int) =
            this.successfullyPostedCommentCardToRefresh.onNext(Pair(comment, position))

        override fun getRootComment(): Observable<CommentCardData> = this.rootComment
        override fun onCommentReplies(): Observable<Pair<List<CommentCardData>, Boolean>> =
            this.onCommentReplies

        override fun insertNewReplyToList(comment: String, createdAt: DateTime) =
            this.insertNewReplyToList.onNext(
                Pair(comment, createdAt)
            )

        override fun onShowGuideLinesLinkClicked() = onShowGuideLinesLinkClicked.onNext(Unit)

        override fun shouldFocusOnCompose(): Observable<Boolean> = this.focusOnCompose
        override fun scrollToBottom(): Observable<Unit> = this.scrollToBottom
        override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
        override fun replyComposerStatus(): Observable<CommentComposerStatus> = replyComposerStatus
        override fun showReplyComposer(): Observable<Boolean> = showReplyComposer
        override fun isFetchingReplies(): Observable<Boolean> = this.isFetchingReplies
        override fun loadMoreReplies(): Observable<Unit> = this.loadMoreReplies
        override fun showCommentGuideLinesLink(): Observable<Unit> = showGuideLinesLink
        override fun checkIfThereAnyPendingComments() = checkIfThereAnyPendingComments.onNext(Unit)
        override fun backPressed() = backPressed.onNext(Unit)
        override fun onShowCanceledPledgeComment(comment: Comment) =
            this.showCanceledPledgeComment.onNext(comment)

        override fun shouldShowPaginationErrorUI(): Observable<Boolean> =
            this.displayPaginationError

        override fun initialLoadCommentsError(): Observable<Boolean> = this.initialLoadCommentsError
        override fun refresh(): Observable<Unit> = this.refresh
        override fun hasPendingComments(): Observable<Boolean> = this.hasPendingComments
        override fun closeThreadActivity(): Observable<Unit> = this.closeThreadActivity

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }

        fun intent(intent: Intent) {
            this.intent.onNext(intent)
        }
    }

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ThreadViewModel(environment) as T
        }
    }
}
