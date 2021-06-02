package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.models.Comment
import com.kickstarter.models.User
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.viewholders.CommentCardViewHolder
import com.kickstarter.ui.views.CommentCardStatus
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

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
        fun isCommentActionGroupVisible(): Observable<Boolean>

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
    }

    class ViewModel(environment: Environment) : ActivityViewModel<CommentCardViewHolder>(environment), Inputs, Outputs {
        private val commentInput = PublishSubject.create<CommentCardData>()
        private val onCommentGuideLinesClicked = PublishSubject.create<Void>()
        private val onRetryViewClicked = PublishSubject.create<Void>()
        private val onReplyButtonClicked = PublishSubject.create<Void>()
        private val onFlagButtonClicked = PublishSubject.create<Void>()
        private val onViewCommentRepliesButtonClicked = PublishSubject.create<Void>()

        private val commentCardStatus = BehaviorSubject.create<CommentCardStatus>()
        private val isCommentActionGroupVisible = BehaviorSubject.create<Boolean>()
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

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.commentInput
                .map { it.comment }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe { this.commentCardStatus.onNext(cardStatus(it)) }

            this.commentInput
                .compose(Transformers.combineLatestPair(environment.currentUser().observable()))
                .compose(bindToLifecycle())
                .subscribe { this.isCommentActionGroupVisible.onNext(isActionGroupVisible(it.first, it.second)) }

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
                .subscribe(this.retrySendComment)

            comment
                .compose(takeWhen(this.onFlagButtonClicked))
                .compose(bindToLifecycle())
                .subscribe(this.flagComment)
        }

        private fun isActionGroupVisible(commentCardData: CommentCardData, user: User?) =
            commentCardData.project?.let {
                it.isBacking || ProjectUtils.userIsCreator(it, user)
            } ?: false

        private fun cardStatus(comment: Comment?) = when {
            comment?.deleted() ?: false -> CommentCardStatus.DELETED_COMMENT
            (comment?.repliesCount() ?: false != 0) -> CommentCardStatus.COMMENT_WITH_REPLIES
            else -> CommentCardStatus.COMMENT_WITHOUT_REPLIES
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

        override fun isCommentActionGroupVisible(): Observable<Boolean> = this.isCommentActionGroupVisible

        override fun openCommentGuideLines(): Observable<Comment> = openCommentGuideLines

        override fun retrySendComment(): Observable<Comment> = retrySendComment

        override fun replyToComment(): Observable<Comment> = replyToComment

        override fun flagComment(): Observable<Comment> = flagComment

        override fun viewCommentReplies(): Observable<Comment> = this.viewCommentReplies
    }
}
