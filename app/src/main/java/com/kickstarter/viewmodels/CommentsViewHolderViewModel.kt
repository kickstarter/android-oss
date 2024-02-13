package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.userIsCreator
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.models.extensions.assignAuthorBadge
import com.kickstarter.models.extensions.isCommentPendingReview
import com.kickstarter.models.extensions.isCurrentUserAuthor
import com.kickstarter.models.extensions.isReply
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardBadge
import com.kickstarter.ui.views.CommentCardStatus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

interface CommentsViewHolderViewModel {
    interface Inputs {
        /** Call when the user clicks learn more about comment guidelines. */
        fun onCommentGuideLinesClicked()

        /** Call when the user clicks retry view to send message */
        fun onRetryViewClicked()

        /** Call when the user clicks reply to comment */
        fun onReplyButtonClicked()

        /** Call when the user clicks view replies to comment */
        fun onViewRepliesButtonClicked()

        /** Call when the user clicks flag comment */
        fun onFlagButtonClicked()

        /** Configure the view model with the [Comment]. */
        fun configureWith(commentCardData: CommentCardData)

        /** Show cancelled comment pledge */
        fun onShowCommentClicked()
    }

    interface Outputs {
        /** Emits the commentCardStatus */
        fun commentCardStatus(): Observable<CommentCardStatus>

        /** Emits the comment replies count. */
        fun commentRepliesCount(): Observable<Int>

        /** Emits the comment Author Name. */
        fun commentAuthorName(): Observable<String>

        /** Emits the comment Author avatar string url. */
        fun commentAuthorAvatarUrl(): Observable<String>

        /** Emits the comment Author avatar string url. */
        fun commentMessageBody(): Observable<String>

        /** Emits the comment post time */
        fun commentPostTime(): Observable<DateTime>

        /** Emits the visibility of the comment card action group */
        fun isReplyButtonVisible(): Observable<Boolean>

        /** Emits the current [Comment] when Comment GuideLines clicked.. */
        fun openCommentGuideLines(): Observable<Comment>

        /** Emits the current [Comment] when Retry clicked.. */
        fun retrySendComment(): Observable<Comment>

        /** Emits the current [Comment] when Reply clicked.. */
        fun replyToComment(): Observable<Comment>

        /** Emits the current [Comment] when flag clicked.. */
        fun flagComment(): Observable<Comment>

        /** Emits the current [Comment] when view replies clicked.. */
        fun viewCommentReplies(): Observable<Comment>

        /** Emits the current status to the CommentCard UI*/
        fun isCommentEnableThreads(): Observable<Boolean>

        /** Emits if the comment is a reply to root comment */
        fun isCommentReply(): Observable<Unit>

        /** Emits when the execution of the post mutation is successful, it will be used to update the main list state for this comment**/
        fun isSuccessfullyPosted(): Observable<Comment>

        /** Emits when the execution of the post mutation is error, it will be used to update the main list state for this comment**/
        fun isFailedToPost(): Observable<Comment>

        /** Emits the current [Comment] when show comment for canceled pledge. */
        fun showCanceledComment(): Observable<Comment>

        fun authorBadge(): Observable<CommentCardBadge>
    }

    class ViewModel(environment: Environment) : Inputs, Outputs {
        private val commentInput = PublishSubject.create<CommentCardData>()
        private val onCommentGuideLinesClicked = PublishSubject.create<Unit>()
        private val onRetryViewClicked = PublishSubject.create<Unit>()
        private val onReplyButtonClicked = PublishSubject.create<Unit>()
        private val onFlagButtonClicked = PublishSubject.create<Unit>()
        private val onViewCommentRepliesButtonClicked = PublishSubject.create<Unit>()
        private val onShowCommentClicked = PublishSubject.create<Unit>()

        private val commentCardStatus = BehaviorSubject.create<CommentCardStatus>()
        private val authorBadge = BehaviorSubject.create<CommentCardBadge>()
        private val isReplyButtonVisible = BehaviorSubject.create<Boolean>()
        private val commentAuthorName = BehaviorSubject.create<String>()
        private val commentAuthorAvatarUrl = BehaviorSubject.create<String>()
        private val commentMessageBody = BehaviorSubject.create<String>()
        private val commentRepliesCount = BehaviorSubject.create<Int>()
        private val commentPostTime = BehaviorSubject.create<DateTime>()
        private val openCommentGuideLines = PublishSubject.create<Comment>()
        private val retrySendComment = PublishSubject.create<Comment>()
        private val replyToComment = PublishSubject.create<Comment>()
        private val flagComment = PublishSubject.create<Comment>()
        private val viewCommentReplies = PublishSubject.create<Comment>()
        private val isCommentEnableThreads = PublishSubject.create<Boolean>()
        private val internalError = BehaviorSubject.create<Throwable>()
        private val postedSuccessfully = BehaviorSubject.create<Comment>()
        private val failedToPosted = BehaviorSubject.create<Comment>()
        private val showCanceledComment = PublishSubject.create<Comment>()

        private val isCommentReply = BehaviorSubject.create<Unit>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val currentUser = requireNotNull(environment.currentUserV2())

        private val disposables = CompositeDisposable()

        init {

            val comment = Observable.merge(this.commentInput.distinctUntilChanged().map { it.comment }, postedSuccessfully)
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }

            configureCommentCardWithComment(comment)

            val commentCardStatus = this.commentInput
                .compose(combineLatestPair(currentUser.observable()))
                .distinctUntilChanged()
                .filter { it.isNotNull() }
                .map {
                    val commentCardState = cardStatus(it.first, it.second.getValue())
                    it.first.toBuilder().commentCardState(commentCardState?.commentCardStatus ?: 0)
                        .build()
                }
            handleStatus(commentCardStatus, environment)

            // - CommentData will hold the information for posting a new comment if needed
            val commentData = this.commentInput
                .distinctUntilChanged()
                .withLatestFrom(currentUser.loggedInUser()) { input, user -> Pair(input, user) }
                .filter { shouldCommentBePosted(it) }
                .map {
                    Pair(requireNotNull(it.first), requireNotNull(it.first.project))
                }

            postComment(commentData, internalError, environment)

            this.internalError
                .compose(combineLatestPair(commentData))
                .distinctUntilChanged()
                .delay(1, TimeUnit.SECONDS, environment.schedulerV2())
                .subscribe {
                    this.commentCardStatus.onNext(CommentCardStatus.FAILED_TO_SEND_COMMENT)
                    it.second.first.comment?.let { it1 -> this.failedToPosted.onNext(it1) }
                }
                .addToDisposable(disposables)

            comment
                .withLatestFrom(currentUser.observable()) { comment, user -> Pair(comment, user) }
                .map { it.first.assignAuthorBadge(it.second.getValue()) }
                .subscribe { this.authorBadge.onNext(it) }
                .addToDisposable(disposables)
        }

        /**
         * Handles the configuration and behaviour for the comment card
         * @param comment the comment observable
         */
        private fun handleStatus(commentCardStatus: Observable<CommentCardData>, environment: Environment) {
            commentCardStatus
                .compose(combineLatestPair(currentUser.observable()))
                .subscribe {
                    cardStatus(it.first, it.second.getValue())?.let { status ->
                        this.commentCardStatus.onNext(status)
                    }
                }
                .addToDisposable(disposables)

            commentCardStatus
                .compose(combineLatestPair(currentUser.observable()))
                .subscribe {
                    this.isReplyButtonVisible.onNext(
                        shouldReplyButtonBeVisible(
                            it.first,
                            it.second.getValue()
                        )
                    )
                }
                .addToDisposable(disposables)
        }

        /**
         * Handles the configuration and behaviour for the comment card
         * @param comment the comment observable
         */
        private fun configureCommentCardWithComment(comment: Observable<Comment>) {

            comment
                .filter { it.parentId() > 0 }
                .subscribe {
                    this.isCommentReply.onNext(Unit)
                }
                .addToDisposable(disposables)

            comment
                .map { it.repliesCount() }
                .compose(combineLatestPair(this.isCommentEnableThreads))
                .subscribe {
                    this.commentRepliesCount.onNext(it.first)
                }
                .addToDisposable(disposables)

            comment
                .filter { it.author()?.name().isNotNull() }
                .map { it.author()?.name() ?: "" }
                .subscribe { this.commentAuthorName.onNext(it) }
                .addToDisposable(disposables)

            comment // TODO: extract all logic around selecting image to an Avatar extension function, and refactor entire app
                .filter { it.author().avatar().medium().isNotNull() }
                .map { aComment ->
                    return@map aComment.author().avatar().medium().ifBlank {
                        return@ifBlank if (aComment.author().avatar().small().isNullOrBlank()) ""
                        else aComment.author().avatar().small()
                    }
                }
                .subscribe { this.commentAuthorAvatarUrl.onNext(it) }
                .addToDisposable(disposables)

            comment
                .map { it.body() }
                .filter { it.isNotNull() }
                .subscribe { this.commentMessageBody.onNext(it) }
                .addToDisposable(disposables)

            comment
                .map { it.createdAt() }
                .filter { it.isNotNull() }
                .subscribe { it?.let { date -> this.commentPostTime.onNext(date) } }
                .addToDisposable(disposables)

            comment
                .compose(takeWhenV2(this.onViewCommentRepliesButtonClicked))
                .subscribe { this.viewCommentReplies.onNext(it) }
                .addToDisposable(disposables)

            comment
                .compose(takeWhenV2(this.onCommentGuideLinesClicked))
                .subscribe { this.openCommentGuideLines.onNext(it) }
                .addToDisposable(disposables)

            comment
                .compose(takeWhenV2(this.onReplyButtonClicked))
                .subscribe { this.replyToComment.onNext(it) }
                .addToDisposable(disposables)

            comment
                .compose(takeWhenV2(this.onRetryViewClicked))
                .doOnNext {
                    this.commentCardStatus.onNext(CommentCardStatus.RE_TRYING_TO_POST)
                }
                .subscribe {
                    this.retrySendComment.onNext(it)
                }
                .addToDisposable(disposables)

            comment
                .compose(takeWhenV2(this.onFlagButtonClicked))
                .subscribe { this.flagComment.onNext(it) }
                .addToDisposable(disposables)

            comment
                .compose(takeWhenV2(this.onShowCommentClicked))
                .subscribe { this.showCanceledComment.onNext(it) }
                .addToDisposable(disposables)
        }

        /**
         * Handles the logic for posting comments (new ones, and the retry attempts)
         * @param commentData will emmit only in case we need to post a new comment
         */
        private fun postComment(
            commentData: Observable<Pair<CommentCardData, Project>>,
            errorObservable: BehaviorSubject<Throwable>,
            environment: Environment
        ) {
            val postCommentData = commentData
                .map {
                    Pair(
                        requireNotNull(it.first.commentableId),
                        requireNotNull(it.first?.comment)
                    )
                }
                .map {
                    PostCommentData(
                        commentableId = it.first,
                        body = it.second.body(),
                        clientMutationId = null,
                        parent = it.second?.parentId()
                            ?.let { id -> it.second.toBuilder().id(id).build() }
                    )
                }
            postCommentData.map {
                executePostCommentMutation(it, errorObservable)
            }
                .switchMap {
                    it
                }
                .subscribe {
                    this.commentCardStatus.onNext(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
                    this.postedSuccessfully.onNext(it)
                    if (it.isReply()) this.isReplyButtonVisible.onNext(false)
                }
                .addToDisposable(disposables)

            Observable
                .combineLatest(onRetryViewClicked, postCommentData) { _, newData ->
                    return@combineLatest executePostCommentMutation(newData, errorObservable)
                }.switchMap {
                    it
                }.doOnNext {
                    this.commentCardStatus.onNext(CommentCardStatus.POSTING_COMMENT_COMPLETED_SUCCESSFULLY)
                }
                .delay(3000, TimeUnit.MILLISECONDS, environment.schedulerV2())
                .subscribe {
                    this.commentCardStatus.onNext(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
                    this.postedSuccessfully.onNext(it)
                    if (it.isReply()) this.isReplyButtonVisible.onNext(false)
                }
                .addToDisposable(disposables)
        }

        /**
         * In order to decide if a comment needs to be posted we need to check:
         * - The comment author is the current user
         * - the comment id is negative, this happens when a comment is created on the app, and has not been posted
         * to the backed yet, otherwise it should have a id bigger than 0
         * - The state we recognize as a comment that needs to be posted is: `TRYING_TO_POST`, no other state is allowed
         */
        private fun shouldCommentBePosted(dataCommentAndUser: Pair<CommentCardData, User>): Boolean {
            var shouldPost = false
            val currentUser = dataCommentAndUser.second
            val comment = dataCommentAndUser.first?.comment?.let { return@let it }
            val status = dataCommentAndUser.first.commentCardState

            comment?.let {
                shouldPost = it.id() < 0 && it.author() == currentUser
            }

            shouldPost = shouldPost && (status == CommentCardStatus.TRYING_TO_POST.commentCardStatus || status == CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus)

            return shouldPost
        }

        /**
         * Function that will execute the PostCommentMutation
         * @param postCommentData holds the comment body and the commentableId for project or update to be posted
         * @return Observable<Comment>
         */
        private fun executePostCommentMutation(
            postCommentData: PostCommentData,
            errorObservable: BehaviorSubject<Throwable>
        ) =
            this.apolloClient.createComment(
                postCommentData
            ).doOnError {
                errorObservable.onNext(it)
            }
                .onErrorResumeNext(Observable.empty())

        /**
         * Checks if the current user is backing the current project,
         * or the current user is the creator of the project
         *  @param commentCardData
         *  @param user
         *
         *  @return
         *  true -> if current user is backer and the comment is not a reply
         *  false -> any of the previous conditions fails
         */
        private fun shouldReplyButtonBeVisible(
            commentCardData: CommentCardData,
            user: User?
        ) =
            commentCardData.project?.let {
                (it.isBacking() || it.userIsCreator(user)) &&
                    (
                        commentCardData.commentCardState == CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus ||
                            commentCardData.commentCardState == CommentCardStatus.COMMENT_WITH_REPLIES.commentCardStatus
                        ) &&
                    (commentCardData.comment?.parentId() ?: -1) < 0
            } ?: false

        /**
         * Updates the status of the current comment card.
         * everytime the state changes.
         */
        private fun cardStatus(commentCardData: CommentCardData, currentUser: User?) = when {
            commentCardData.comment?.isCommentPendingReview() ?: false &&
                commentCardData.comment?.isCurrentUserAuthor(currentUser) == false -> CommentCardStatus.FLAGGED_COMMENT
            commentCardData.comment?.deleted() ?: false -> CommentCardStatus.DELETED_COMMENT
            commentCardData.comment?.authorCanceledPledge() ?: false -> checkCanceledPledgeCommentStatus(commentCardData)
            (commentCardData.comment?.repliesCount() ?: 0 != 0) -> CommentCardStatus.COMMENT_WITH_REPLIES
            else -> CommentCardStatus.values().firstOrNull {
                it.commentCardStatus == commentCardData.commentCardState
            }
        }.also {
            this.isCommentEnableThreads.onNext(true)
        }

        private fun checkCanceledPledgeCommentStatus(commentCardData: CommentCardData): CommentCardStatus =
            if (commentCardData.commentCardState != CommentCardStatus.CANCELED_PLEDGE_COMMENT.commentCardStatus)
                CommentCardStatus.CANCELED_PLEDGE_MESSAGE
            else
                CommentCardStatus.CANCELED_PLEDGE_COMMENT

        override fun configureWith(commentCardData: CommentCardData) =
            this.commentInput.onNext(commentCardData)

        override fun onCommentGuideLinesClicked() = this.onCommentGuideLinesClicked.onNext(Unit)

        override fun onRetryViewClicked() = this.onRetryViewClicked.onNext(Unit)

        override fun onReplyButtonClicked() = this.onReplyButtonClicked.onNext(Unit)

        override fun onViewRepliesButtonClicked() =
            this.onViewCommentRepliesButtonClicked.onNext(Unit)

        override fun onFlagButtonClicked() = this.onFlagButtonClicked.onNext(Unit)

        override fun onShowCommentClicked() = this.onShowCommentClicked.onNext(Unit)

        override fun commentCardStatus(): Observable<CommentCardStatus> = this.commentCardStatus

        override fun commentRepliesCount(): Observable<Int> = this.commentRepliesCount

        override fun commentAuthorName(): Observable<String> = this.commentAuthorName

        override fun commentAuthorAvatarUrl(): Observable<String> = this.commentAuthorAvatarUrl

        override fun commentMessageBody(): Observable<String> = this.commentMessageBody

        override fun commentPostTime(): Observable<DateTime> = this.commentPostTime

        override fun isReplyButtonVisible(): Observable<Boolean> = this.isReplyButtonVisible

        override fun openCommentGuideLines(): Observable<Comment> = openCommentGuideLines

        override fun retrySendComment(): Observable<Comment> = retrySendComment

        override fun replyToComment(): Observable<Comment> = replyToComment

        override fun flagComment(): Observable<Comment> = flagComment

        override fun viewCommentReplies(): Observable<Comment> = this.viewCommentReplies

        override fun isCommentEnableThreads(): Observable<Boolean> = this.isCommentEnableThreads

        override fun isCommentReply(): Observable<Unit> = this.isCommentReply

        override fun isSuccessfullyPosted(): Observable<Comment> = this.postedSuccessfully

        override fun isFailedToPost(): Observable<Comment> = this.failedToPosted

        override fun showCanceledComment(): Observable<Comment> = this.showCanceledComment

        override fun authorBadge(): Observable<CommentCardBadge> = this.authorBadge

        fun onCleared() {
            disposables.clear()
        }
    }
}
