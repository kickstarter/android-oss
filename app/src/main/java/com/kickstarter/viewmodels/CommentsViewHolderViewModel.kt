package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.ExperimentsClientType
import com.kickstarter.libs.models.OptimizelyFeature
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.models.Comment
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

        fun postNewComment(commentCardData: CommentCardData)
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

       /** Emits the new bind comment to ui */
        fun newCommentBind(): Observable<CommentCardData>
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
        private val postNewComment = BehaviorSubject.create<CommentCardData>()
        private val newCommentBind = BehaviorSubject.create<CommentCardData>()
        private val isCommentEnableThreads = PublishSubject.create<Boolean>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val optimizely: ExperimentsClientType = environment.optimizely()
        private val apolloClient: ApolloClientType = environment.apolloClient()
        init {
            this.commentInput
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    this.commentCardStatus.onNext(cardStatus(it))
                }

            this.commentInput
                .compose(Transformers.combineLatestPair(environment.currentUser().observable()))
                .compose(bindToLifecycle())
                .subscribe {
                    this.isReplyButtonVisible.onNext(
                        shouldReplyButtonBeVisible(it.first, it.second, optimizely.isFeatureEnabled(OptimizelyFeature.Key.COMMENT_ENABLE_THREADS))
                    )
                }

            val comment = this.commentInput
                .map { it.comment }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

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
                .compose(bindToLifecycle())
                .subscribe {
                    this.retrySendComment.onNext(it)
                    this.commentCardStatus.onNext(CommentCardStatus.RE_TRYING_TO_POST)
                }

            val commentData = this.commentInput.map {
                Pair(requireNotNull(it.comment?.body()), requireNotNull(it.project))
            }

            Observable
                .combineLatest(commentData, postNewComment) { commentData, _ ->
                    return@combineLatest this.apolloClient.createComment(
                        PostCommentData(
                            project = commentData.second,
                            body = commentData.first,
                            clientMutationId = null,
                            parentId = null
                        )
                    ).doOnError {
                        this.commentCardStatus.onNext(CommentCardStatus.FAILED_TO_SEND_COMMENT)
                    }
                        .onErrorResumeNext(Observable.empty())
                }
                .switchMap {
                    it
                }.subscribe {
                    this.commentCardStatus.onNext(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
                }

            Observable
                .combineLatest(commentData, onRetryViewClicked) { commentData, _ ->
                    return@combineLatest this.apolloClient.createComment(
                        PostCommentData(
                            project = commentData.second,
                            body = commentData.first,
                            clientMutationId = null,
                            parentId = null
                        )
                    ).doOnError {
                        this.commentCardStatus.onNext(CommentCardStatus.FAILED_TO_SEND_COMMENT)
                    }
                        .onErrorResumeNext(Observable.empty())
                }
                .switchMap {
                    it
                }.doOnNext {

                    this.commentCardStatus.onNext(CommentCardStatus.POSTING_COMMENT_COMPLETED_SUCCESSFULLY)
                }
                .delay(3000, TimeUnit.MILLISECONDS)
                .subscribe {
                    this.commentCardStatus.onNext(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS)
                }

            comment
                .compose(takeWhen(this.onFlagButtonClicked))
                .compose(bindToLifecycle())
                .subscribe(this.flagComment)

            this.commentInput
                .filter { ObjectUtils.isNotNull(it) }
                .filter { it.commentCardState == CommentCardStatus.TRYING_TO_POST.commentCardStatus }
                .compose(bindToLifecycle())
                .subscribe {
                    this.newCommentBind.onNext(it)
                }
        }

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
            commentCardData.project?.let { (it.isBacking || ProjectUtils.userIsCreator(it, user)) && featureFlagActive &&
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

        override fun newCommentBind(): Observable<CommentCardData> = this.newCommentBind

        override fun postNewComment(commentCardData: CommentCardData) = postNewComment.onNext(commentCardData)
    }
}
