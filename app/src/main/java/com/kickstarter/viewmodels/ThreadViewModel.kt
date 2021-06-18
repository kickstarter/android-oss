package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Comment
import com.kickstarter.services.ApolloClientType
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ThreadActivity
import com.kickstarter.ui.data.CommentCardData
import rx.Observable
import rx.subjects.BehaviorSubject

interface ThreadViewModel {

    interface Inputs
    interface Outputs {
        /** The anchored root comment */
        fun getRootComment(): Observable<Comment>

        /** get comment replies **/
        fun onCommentReplies(): Observable<List<CommentCardData>>

        /** Will tell to the compose view if should open the keyboard */
        fun shouldFocusOnCompose(): Observable<Boolean>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ThreadActivity>(environment), Inputs, Outputs {
        private val apolloClient: ApolloClientType = environment.apolloClient()
        private val currentUser: CurrentUserType = environment.currentUser()

        private val rootComment = BehaviorSubject.create<Comment>()
        private val focusOnCompose = BehaviorSubject.create<Boolean>()

        private val onCommentReplies = BehaviorSubject.create<List<CommentCardData>>()

        val inputs = this
        val outputs = this

        init {
            getCommentFromIntent()

            val comment = getCommentFromIntent()
                .distinctUntilChanged()
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            intent()
                .map { it.getBooleanExtra(IntentKey.REPLY_EXPAND, false) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.focusOnCompose)

            val commentEnvelope = getCommentFromIntent()
                .switchMap {
                    this.apolloClient.getRepliesForComment(it)
                }
                .share()

            commentEnvelope
                .compose(Transformers.combineLatestPair(comment))
                .compose(bindToLifecycle())
                .subscribe {
                    val comments = it.first.comments?.map { comment: Comment ->
                        CommentCardData.builder().comment(
                            comment.toBuilder()
                                .parentId(it.second.id())
                                .build()
                        )
                            .build()
                    }

                    this.onCommentReplies.onNext(comments)
                }

            comment
                .compose(bindToLifecycle())
                .subscribe(this.rootComment)
        }

        private fun getCommentFromIntent() = intent()
            .map { it.getParcelableExtra(IntentKey.COMMENT) as Comment? }
            .ofType(Comment::class.java)

        override fun getRootComment(): Observable<Comment> = this.rootComment
        override fun onCommentReplies(): Observable<List<CommentCardData>> = this.onCommentReplies

        override fun shouldFocusOnCompose(): Observable<Boolean> = this.focusOnCompose
    }
}
