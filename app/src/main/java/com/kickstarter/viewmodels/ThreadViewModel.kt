package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ThreadActivity
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentComposerStatus
import rx.Observable
import rx.subjects.BehaviorSubject
import timber.log.Timber

interface ThreadViewModel {

    interface Inputs
    interface Outputs {
        /** The anchored root comment */
        fun getRootComment(): Observable<Comment>

        /** Will tell to the compose view if should open the keyboard */
        fun shouldFocusOnCompose(): Observable<Boolean>

        fun currentUserAvatar(): Observable<String?>
        fun replyComposerStatus(): Observable<CommentComposerStatus>
        fun showReplyComposer(): Observable<Boolean>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ThreadActivity>(environment), Inputs, Outputs {
        private val apolloClient: ApolloClientType = environment.apolloClient()
        private val currentUser: CurrentUserType = environment.currentUser()

        private val rootComment = BehaviorSubject.create<Comment>()
        private val focusOnCompose = BehaviorSubject.create<Boolean>()
        private val currentUserAvatar = BehaviorSubject.create<String?>()
        private val replyComposerStatus = BehaviorSubject.create<CommentComposerStatus>()
        private val showReplyComposer = BehaviorSubject.create<Boolean>()

        val inputs = this
        val outputs = this

        init {
            getCommentCardDataFromIntent()

            val commentData = getCommentCardDataFromIntent()
                .distinctUntilChanged()
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            intent()
                .map { it.getBooleanExtra(IntentKey.REPLY_EXPAND, false) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.focusOnCompose)

            val commentEnvelope = getCommentCardDataFromIntent()
                .switchMap {
                    it.comment?.let { it1 -> this.apolloClient.getRepliesForComment(it1) }
                }
                .share()

            commentEnvelope
                .compose(bindToLifecycle())
                .subscribe {
                    Timber.i(
                        "******* Comment envelope with \n" +
                            "totalComments:${it.totalCount} \n" +
                            "commentsList: ${it.comments?.map { comment -> comment.body() + " |"}} \n" +
                            "commentsListSize: ${it.comments?.size} \n" +
                            "pageInfo: ${it.pageInfoEnvelope} ****"
                    )
                }

            commentData
                .switchMap {
                    this.apolloClient.createComment(
                        PostCommentData(
                            commentableId = it.commentableId ?: "",
                            body = "Just a comment reply",
                            clientMutationId = null,
                            parent = it.comment
                        )
                    )
                }
                .compose(bindToLifecycle())
                .subscribe(
                    {
                        val c = it
                    },
                    {
                       val e = it
                    }
                 )

            commentData
                .compose(bindToLifecycle())
                .subscribe{
                    this.rootComment.onNext(it.comment)
                }

            val project = intent()
                .map { it.getParcelableExtra(IntentKey.PROJECT) as Project? }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

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
                    showReplyComposer.onNext(true)
                }

            project
                .compose(Transformers.combineLatestPair(currentUser.observable()))
                .compose(bindToLifecycle())
                .subscribe {
                    val composerStatus = getCommentComposerStatus(Pair(it.first, it.second))
                    showReplyComposer.onNext(composerStatus != CommentComposerStatus.GONE)
                    replyComposerStatus.onNext(composerStatus)
                }
        }

        private fun getCommentComposerStatus(projectAndUser: Pair<Project, User?>) =
            when {
                projectAndUser.second == null -> CommentComposerStatus.GONE
                projectAndUser.first.isBacking || ProjectUtils.userIsCreator(projectAndUser.first, projectAndUser.second) -> CommentComposerStatus.ENABLED
                else -> CommentComposerStatus.DISABLED
            }

        private fun getCommentCardDataFromIntent() = intent()
            .map { it.getParcelableExtra(IntentKey.COMMENT_CARD_DATA) as CommentCardData? }
            .ofType(CommentCardData::class.java)

        override fun getRootComment(): Observable<Comment> = this.rootComment
        override fun shouldFocusOnCompose(): Observable<Boolean> = this.focusOnCompose
        override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
        override fun replyComposerStatus(): Observable<CommentComposerStatus> = replyComposerStatus
        override fun showReplyComposer(): Observable<Boolean> = showReplyComposer
    }
}
