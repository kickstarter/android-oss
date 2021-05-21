package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.models.Comment
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

        /** Call when the user clicks flag comment */
        fun onFlagButtonClicked()

        /** Configure the view model with the [Comment]. */
        fun configureWith(comment: Comment)
    }

    interface Outputs {
        /** Emits the commentCardStatus */
        fun commentCardStatus(): Observable<CommentCardStatus>

        /** Emits the comment Author Name. */
        fun commentAuthorName(): Observable<String>

        /** Emits the comment Author avatar string url. */
        fun commentAuthorAvatarUrl(): Observable<String>

        /** Emits the comment Author avatar string url. */
        fun commentMessageBody(): Observable<String>

        /** Emits the comment post time */
        fun commentPostTime(): Observable<DateTime>

        /** Emits the current [Comment] when Comment GuideLines clicked.. */
        fun openCommentGuideLines(): Observable<Comment>

        /** Emits the current [Comment] when Retry clicked.. */
        fun retrySendComment(): Observable<Comment>

        /** Emits the current [Comment] when Reply clicked.. */
        fun replyToComment(): Observable<Comment>

        /** Emits the current [Comment] when flag clicked.. */
        fun flagComment(): Observable<Comment>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<CommentCardViewHolder>(environment), Inputs, Outputs {
        private val commentInput = PublishSubject.create<Comment>()
        private val onCommentGuideLinesClicked = PublishSubject.create<Void>()
        private val onRetryViewClicked = PublishSubject.create<Void>()
        private val onReplyButtonClicked = PublishSubject.create<Void>()
        private val onFlagButtonClicked = PublishSubject.create<Void>()

        private val commentCardStatus = BehaviorSubject.create<CommentCardStatus>()
        private val commentAuthorName = BehaviorSubject.create<String>()
        private val commentAuthorAvatarUrl = BehaviorSubject.create<String>()
        private val commentMessageBody = BehaviorSubject.create<String>()
        private val commentPostTime = BehaviorSubject.create<DateTime>()
        private val openCommentGuideLines = PublishSubject.create<Comment>()
        private val retrySendComment = PublishSubject.create<Comment>()
        private val replyToComment = PublishSubject.create<Comment>()
        private val flagComment = PublishSubject.create<Comment>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            this.commentInput
                .compose(bindToLifecycle())
                .subscribe {
                    val cardStatus = when {
                        it.deleted() -> CommentCardStatus.DELETED_COMMENT
                        (it.repliesCount() != 0) -> CommentCardStatus.COMMENT_WITH_REPLAY
                        else -> CommentCardStatus.COMMENT_WITHOUT_REPLAY
                    }
                    this.commentCardStatus.onNext(cardStatus)
                }

            this.commentInput
                .map { it.author().name() }
                .compose(bindToLifecycle())
                .subscribe(this.commentAuthorName)

            this.commentInput
                .map { it.author().avatar().medium() }
                .compose(bindToLifecycle())
                .subscribe(this.commentAuthorAvatarUrl)

            this.commentInput
                .map { it.body() }
                .compose(bindToLifecycle())
                .subscribe(this.commentMessageBody)

            this.commentInput
                .map { it.createdAt() }
                .compose(bindToLifecycle())
                .subscribe(this.commentPostTime)

            this.commentInput
                .compose(takeWhen(this.onCommentGuideLinesClicked))
                .compose(bindToLifecycle())
                .subscribe(this.openCommentGuideLines)

            this.commentInput
                .compose(takeWhen(this.onReplyButtonClicked))
                .compose(bindToLifecycle())
                .subscribe(this.replyToComment)

            this.commentInput
                .compose(takeWhen(this.onRetryViewClicked))
                .compose(bindToLifecycle())
                .subscribe(this.retrySendComment)

            this.commentInput
                .compose(takeWhen(this.onFlagButtonClicked))
                .compose(bindToLifecycle())
                .subscribe(this.flagComment)
        }

        override fun configureWith(comment: Comment) = this.commentInput.onNext(comment)

        override fun onCommentGuideLinesClicked() = this.onCommentGuideLinesClicked.onNext(null)

        override fun onRetryViewClicked() = this.onRetryViewClicked.onNext(null)

        override fun onReplyButtonClicked() = this.onReplyButtonClicked.onNext(null)

        override fun onFlagButtonClicked() = this.onFlagButtonClicked.onNext(null)

        override fun commentCardStatus(): Observable<CommentCardStatus> = this.commentCardStatus

        override fun commentAuthorName(): Observable<String> = this.commentAuthorName

        override fun commentAuthorAvatarUrl(): Observable<String> = this.commentAuthorAvatarUrl

        override fun commentMessageBody(): Observable<String> = this.commentMessageBody

        override fun commentPostTime(): Observable<DateTime> = this.commentPostTime

        override fun openCommentGuideLines(): Observable<Comment> = openCommentGuideLines

        override fun retrySendComment(): Observable<Comment> = retrySendComment

        override fun replyToComment(): Observable<Comment> = replyToComment

        override fun flagComment(): Observable<Comment> = flagComment
    }
}
