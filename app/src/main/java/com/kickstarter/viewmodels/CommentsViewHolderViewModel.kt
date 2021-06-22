package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.views.CommentCardStatus
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
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

        /** Emits the current [OptimizelyFeature.Key.COMMENT_ENABLE_THREADS] status to the CommentCard UI*/
        fun isCommentEnableThreads(): Observable<Boolean>

        /** Emits if the comment is a reply to root comment */
        fun isCommentReply(): Observable<Void>

        /** Emits when the execution of the post mutation is successful, it will be used to update the main list state for this comment**/
        fun isSuccessfullyPosted(): Observable<Comment>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<CommentCardViewHolder>(environment), Inputs, Outputs {
        private val commentInput = PublishSubject.create<CommentCardData>()
        private val onCommentGuideLinesClicked = PublishSubject.create<Void>()
        private val onRetryViewClicked = PublishSubject.create<Void>()
        private val onReplyButtonClicked = PublishSubject.create<Void>()
        private val onFlagButtonClicked = PublishSubject.create<Void>()
        private val onViewCommentRepliesButtonClicked = PublishSubject.create<Void>()

        private val commentCardStatus = BehaviorSubject.create<CommentCardStatus>()
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

        private val isCommentReply = BehaviorSubject.create<Void>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val optimizely: ExperimentsClientType = environment.optimizely()
        private val apolloClient: ApolloClientType = environment.apolloClient()
        private val currentUser = environment.currentUser()

        init {

            val comment = this.commentInput
                .map { it.comment }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
            configureCommentCardWithComment(comment)

            val commentCardStatus = this.commentInput
                .filter { ObjectUtils.isNotNull(it) }
                .map {
                    val commentCardState = cardStatus(it)
                    it.toBuilder().commentCardState(commentCardState?.commentCardStatus ?: 0).build()
                }
            handleStatus(commentCardStatus)

            // - CommentData will hold the information for posting a new comment if needed
            val commentData = this.commentInput
                .distinctUntilChanged()
                .withLatestFrom(currentUser.loggedInUser()) { input, user -> Pair(input, user) }
                .filter { shouldCommentBePosted(it) }
                .map {
                    Pair(requireNotNull(it.first.comment?.body()), requireNotNull(it.first.project))
                }
            postComment(commentData, internalError)

            this.internalError
                .compose(bindToLifecycle())
                .delay(1, TimeUnit.SECONDS, environment.scheduler())
                .subscribe {
                    this.commentCardStatus.onNext(CommentCardStatus.FAILED_TO_SEND_COMMENT)
                }
        }

        /**
         * Handles the configuration and behaviour for the comment card
         * @param comment the comment observable
         */
        private fun handleStatus(commentCardStatus: Observable<CommentCardData>) {
            commentCardStatus
                .compose(bindToLifecycle())
                .subscribe {
                    this.commentCardStatus.onNext(cardStatus(it))
                }

            commentCardStatus
                .compose(combineLatestPair(currentUser.observable()))
                .compose(bindToLifecycle())
                .subscribe {
                    this.isReplyButtonVisible.onNext(
                        shouldReplyButtonBeVisible(
                            it.first,
                            it.second,
                            optimizely.isFeatureEnabled(OptimizelyFeature.Key.COMMENT_ENABLE_THREADS)
                        )
                    )
                }
        }

        /**
         * Handles the configuration and behaviour for the comment card
         * @param comment the comment observable
         */
        private fun configureCommentCardWithComment(comment: Observable<Comment>) {

            comment
                .filter { it.parentId() > 0 }
                .compose(bindToLifecycle())
                .subscribe {
                    this.isCommentReply.onNext(null)
                }

            comment
                .map { it.repliesCount() }
                .compose(bindToLifecycle())
                .subscribe(this.commentRepliesCount)

            comment
                .map { it.author()?.name() }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe(this.commentAuthorName)

            comment
                .map { it.author()?.avatar()?.medium() }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe(this.commentAuthorAvatarUrl)

            comment
                .map { it.body() }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe(this.commentMessageBody)

            comment
                .map { it.createdAt() }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe(this.commentPostTime)

            comment
                .compose(takeWhen(this.onViewCommentRepliesButtonClicked))
                .compose(bindToLifecycle())
                .subscribe(this.viewCommentReplies)

            comment
                .compose(takeWhen(this.onCommentGuideLinesClicked))
                .compose(bindToLifecycle())
                .subscribe(this.openCommentGuideLines)

            comment
                .compose(takeWhen(this.onReplyButtonClicked))
                .compose(bindToLifecycle())
                .subscribe(this.replyToComment)

            comment
                .compose(takeWhen(this.onRetryViewClicked))
                .doOnNext {
                    this.commentCardStatus.onNext(CommentCardStatus.RE_TRYING_TO_POST)
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.retrySendComment.onNext(it)
                }

            comment
                .compose(takeWhen(this.onFlagButtonClicked))
                .compose(bindToLifecycle())
                .subscribe(this.flagComment)
        }

        /**
         * Handles the logic for posting comments (new ones, and the retry attempts)
         * @param commentData will emmit only in case we need to post a new comment
         */
        private fun postComment(commentData: Observable<Pair<String, Project>>, errorObservable: BehaviorSubject<Throwable>) {
            commentData
                .map {
                    executePostCommentMutation(it, errorObservable)
                }
                .switchMap {
                    it
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.commentCardStatus.onNext(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
                    this.postedSuccessfully.onNext(it)
                }

            Observable
                .combineLatest(onRetryViewClicked, commentData) { _, newData ->
                    return@combineLatest executePostCommentMutation(newData, errorObservable)
                }.switchMap {
                    it
                }.doOnNext {
                    this.commentCardStatus.onNext(CommentCardStatus.POSTING_COMMENT_COMPLETED_SUCCESSFULLY)
                }
                .delay(3000, TimeUnit.MILLISECONDS)
                .compose(bindToLifecycle())
                .subscribe {
                    this.commentCardStatus.onNext(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
                }
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

            shouldPost = shouldPost && status == CommentCardStatus.TRYING_TO_POST.commentCardStatus

            return shouldPost
        }

        /**
         * Function that will execute the PostCommentMutation
         * @param commentData holds the comment body and the project to be posted
         * // TODO: for the future threads wi will need to send to the mutation not just the body,
         * // TODO: we will need the entire comment plus very important the [parentId]
         * @return Observable<Comment>
         */
        private fun executePostCommentMutation(commentData: Pair<String, Project>, errorObservable: BehaviorSubject<Throwable>) =
            this.apolloClient.createComment(
                PostCommentData(
                    project = commentData.second,
                    body = commentData.first,
                    clientMutationId = null,
                    parentId = null
                )
            ).doOnError {
                errorObservable.onNext(it)
            }
                .onErrorResumeNext(Observable.empty())

        /**
         * Checks if the current user is backing the current project,
         * or the current user is the creator of the project
         *  @param commentCardData
         *  @param featureFlagActive
         *  @param user
         *
         *  @return
         *  true -> if current user is backer and the feature flag is active
         *  false -> any of the previous conditions fails
         */
        private fun shouldReplyButtonBeVisible(
            commentCardData: CommentCardData,
            user: User?,
            featureFlagActive: Boolean
        ) =
            commentCardData.project?.let {
                (it.isBacking || ProjectUtils.userIsCreator(it, user)) && featureFlagActive &&
                    (
                        commentCardData.commentCardState == CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus ||
                            commentCardData.commentCardState == CommentCardStatus.COMMENT_WITH_REPLIES.commentCardStatus ||
                            commentCardData.commentCardState == CommentCardStatus.TRYING_TO_POST.commentCardStatus
                        )
            } ?: false

        /**
         * Updates the status of the current comment card.
         * Also updates the current state of the [OptimizelyFeature.Key.COMMENT_ENABLE_THREADS]
         * everytime the state changes.
         */
        private fun cardStatus(commentCardData: CommentCardData) = when {
            commentCardData.comment?.deleted() ?: false -> CommentCardStatus.DELETED_COMMENT
            (commentCardData.comment?.repliesCount() ?: false != 0) -> CommentCardStatus.COMMENT_WITH_REPLIES
            else -> CommentCardStatus.values().firstOrNull { it.commentCardStatus == commentCardData.commentCardState }
        }.also {
            this.isCommentEnableThreads.onNext(optimizely.isFeatureEnabled(OptimizelyFeature.Key.COMMENT_ENABLE_THREADS))
        }

        override fun configureWith(commentCardData: CommentCardData) = this.commentInput.onNext(commentCardData)

        override fun onCommentGuideLinesClicked() = this.onCommentGuideLinesClicked.onNext(null)

        override fun onRetryViewClicked() = this.onRetryViewClicked.onNext(null)

        override fun onReplyButtonClicked() = this.onReplyButtonClicked.onNext(null)

        override fun onViewRepliesButtonClicked() = this.onViewCommentRepliesButtonClicked.onNext(null)

        override fun onFlagButtonClicked() = this.onFlagButtonClicked.onNext(null)

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

        override fun isCommentReply(): Observable<Void> = this.isCommentReply

        override fun isSuccessfullyPosted(): Observable<Comment> = this.postedSuccessfully
    }
}
