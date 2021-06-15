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
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ThreadActivity
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
        fun replayComposerStatus(): Observable<CommentComposerStatus>
        fun showReplayComposer(): Observable<Boolean>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ThreadActivity>(environment), Inputs, Outputs {
        private val apolloClient: ApolloClientType = environment.apolloClient()
        private val currentUser: CurrentUserType = environment.currentUser()

        private val rootComment = BehaviorSubject.create<Comment>()
        private val focusOnCompose = BehaviorSubject.create<Boolean>()
        private val currentUserAvatar = BehaviorSubject.create<String?>()
        private val replayComposerStatus = BehaviorSubject.create<CommentComposerStatus>()
        private val showReplayComposer = BehaviorSubject.create<Boolean>()

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

            comment
                .compose(bindToLifecycle())
                .subscribe(this.rootComment)

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
                    showReplayComposer.onNext(true)
                }

            project
                .compose(Transformers.combineLatestPair(currentUser.observable()))
                .compose(bindToLifecycle())
                .subscribe {
                    val composerStatus = getCommentComposerStatus(Pair(it.first, it.second))
                    showReplayComposer.onNext(composerStatus != CommentComposerStatus.GONE)
                    replayComposerStatus.onNext(composerStatus)
                }
        }

        private fun getCommentComposerStatus(projectAndUser: Pair<Project, User?>) =
            when {
                projectAndUser.second == null -> CommentComposerStatus.GONE
                projectAndUser.first.isBacking || ProjectUtils.userIsCreator(projectAndUser.first, projectAndUser.second) -> CommentComposerStatus.ENABLED
                else -> CommentComposerStatus.DISABLED
            }

        private fun getCommentFromIntent() = intent()
            .map { it.getParcelableExtra(IntentKey.COMMENT) as Comment? }
            .ofType(Comment::class.java)

        override fun getRootComment(): Observable<Comment> = this.rootComment
        override fun shouldFocusOnCompose(): Observable<Boolean> = this.focusOnCompose
        override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
        override fun replayComposerStatus(): Observable<CommentComposerStatus> = replayComposerStatus
        override fun showReplayComposer(): Observable<Boolean> = showReplayComposer
    }
}
