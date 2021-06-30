package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.loadmore.ApolloPaginate
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.libs.utils.extensions.toCommentCardList
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ThreadActivity
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentComposerStatus
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ThreadViewModel {

    interface Inputs {
        fun nextPage()
    }
    interface Outputs {
        /** The anchored root comment */
        fun getRootComment(): Observable<Comment>

        /** get comment replies **/
        fun onCommentReplies(): Observable<Pair<List<CommentCardData>, Boolean>>

        /** Will tell to the compose view if should open the keyboard */
        fun shouldFocusOnCompose(): Observable<Boolean>

        fun currentUserAvatar(): Observable<String?>
        fun replyComposerStatus(): Observable<CommentComposerStatus>
        fun showReplyComposer(): Observable<Boolean>

        fun isFetchingReplies(): Observable<Boolean>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ThreadActivity>(environment), Inputs, Outputs {
        private val apolloClient: ApolloClientType = environment.apolloClient()
        private val currentUser: CurrentUserType = environment.currentUser()

        private val nextPage = PublishSubject.create<Void>()

        private val rootComment = BehaviorSubject.create<Comment>()
        private val focusOnCompose = BehaviorSubject.create<Boolean>()
        private val currentUserAvatar = BehaviorSubject.create<String?>()
        private val replyComposerStatus = BehaviorSubject.create<CommentComposerStatus>()
        private val showReplyComposer = BehaviorSubject.create<Boolean>()
        private val isFetchingReplies = BehaviorSubject.create<Boolean>()
        private val hasPreviousElements = BehaviorSubject.create<Boolean>()
        private val refresh = PublishSubject.create<Void>()

        private val onCommentReplies = BehaviorSubject.create<Pair<List<CommentCardData>, Boolean>>()
        private lateinit var project: Project

        val inputs = this
        val outputs = this

        init {

            val commentData = getCommentCardDataFromIntent()
                .distinctUntilChanged()
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            intent()
                .map { it.getBooleanExtra(IntentKey.REPLY_EXPAND, false) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.focusOnCompose)

            val comment = getCommentCardDataFromIntent().map { it.comment }.map { requireNotNull(it) }

            val project = commentData
                .map { it.project }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }
                .doOnNext {
                    this.project = it
                }

            loadCommentListFromProjectOrUpdate(comment)

            commentData
                .compose(bindToLifecycle())
                .subscribe {
                    this.rootComment.onNext(it.comment)
                }

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

        private fun loadCommentListFromProjectOrUpdate(comment: Observable<Comment>) {
            val startOverWith =
                Observable.merge(
                    comment,
                    comment.compose(
                        Transformers.takeWhen(
                            refresh
                        )
                    )
                )

            val apolloPaginate =
                ApolloPaginate.builder<CommentCardData, CommentEnvelope, Comment?>()
                    .nextPage(nextPage)
                    .distinctUntilChanged(true)
                    .startOverWith(startOverWith)
                    .envelopeToListOfData {
                        hasPreviousElements.onNext(it.pageInfoEnvelope()?.hasPreviousPage ?: false)
                        mapListToData(it)
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
                .isFetching()
                .compose(bindToLifecycle<Boolean>())
                .subscribe(this.isFetchingReplies)

            apolloPaginate
                .paginatedData()
                ?.compose(Transformers.combineLatestPair(this.hasPreviousElements))
                ?.distinctUntilChanged()
                ?.share()
                ?.subscribe {
                    this.onCommentReplies.onNext(it)
                }
        }

        private fun mapListToData(it: CommentEnvelope) = it.comments?.toCommentCardList(this.project)

        private fun loadWithProjectReplies(
            comment: Observable<Comment>,
            cursor: String?
        ): Observable<CommentEnvelope> {
            return comment.switchMap {
                return@switchMap this.apolloClient.getRepliesForComment(it, cursor)
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

        override fun nextPage() = nextPage.onNext(null)

        override fun getRootComment(): Observable<Comment> = this.rootComment
        override fun onCommentReplies(): Observable<Pair<List<CommentCardData>, Boolean>> = this.onCommentReplies

        override fun shouldFocusOnCompose(): Observable<Boolean> = this.focusOnCompose
        override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
        override fun replyComposerStatus(): Observable<CommentComposerStatus> = replyComposerStatus
        override fun showReplyComposer(): Observable<Boolean> = showReplyComposer
        override fun isFetchingReplies(): Observable<Boolean> = this.isFetchingReplies
    }
}
