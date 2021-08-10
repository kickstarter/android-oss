package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.loadmore.ApolloPaginate
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takePairWhen
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.models.User
import com.kickstarter.models.extensions.cardStatus
import com.kickstarter.models.extensions.updateCanceledPledgeComment
import com.kickstarter.models.extensions.updateCommentAfterSuccessfulPost
import com.kickstarter.models.extensions.updateCommentFailedToPost
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.CommentsActivity
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.ui.views.CommentComposerStatus
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface CommentsViewModel {

    interface Inputs {
        fun refresh()
        fun nextPage()
        fun backPressed()
        fun insertNewCommentToList(comment: String, createdAt: DateTime)
        fun onReplyClicked(comment: Comment, openKeyboard: Boolean)
        fun onShowGuideLinesLinkClicked()
        fun checkIfThereAnyPendingComments(isBackAction: Boolean)

        /** Will be called with the successful response when calling the `postComment` Mutation **/
        fun refreshComment(comment: Comment, position: Int)
        fun refreshCommentCardInCaseFailedPosted(comment: Comment, position: Int)
        fun onShowCanceledPledgeComment(comment: Comment)
    }

    interface Outputs {
        fun closeCommentsPage(): Observable<Void>
        fun currentUserAvatar(): Observable<String?>
        fun commentComposerStatus(): Observable<CommentComposerStatus>
        fun enableReplyButton(): Observable<Boolean>
        fun showCommentComposer(): Observable<Boolean>
        fun commentsList(): Observable<List<CommentCardData>>
        fun scrollToTop(): Observable<Boolean>
        fun setEmptyState(): Observable<Boolean>
        fun showCommentGuideLinesLink(): Observable<Void>
        fun initialLoadCommentsError(): Observable<Throwable>
        fun paginateCommentsError(): Observable<Throwable>
        fun pullToRefreshError(): Observable<Throwable>
        fun startThreadActivity(): Observable<Pair<CommentCardData, Boolean>>
        fun hasPendingComments(): Observable<Pair<Boolean, Boolean>>

        /** Emits a boolean indicating whether comments are being fetched from the API.  */
        fun isFetchingComments(): Observable<Boolean>

        /** Display the bottom pagination Error Cell **/
        fun shouldShowPaginationErrorUI(): Observable<Boolean>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<CommentsActivity>(environment), Inputs, Outputs {

        private val currentUser: CurrentUserType = environment.currentUser()
        private val client: ApiClientType = environment.apiClient()
        private val apolloClient: ApolloClientType = environment.apolloClient()
        val inputs: Inputs = this
        val outputs: Outputs = this
        private val backPressed = PublishSubject.create<Void>()
        private val refresh = PublishSubject.create<Void>()
        private val nextPage = PublishSubject.create<Void>()
        private val onShowGuideLinesLinkClicked = PublishSubject.create<Void>()
        private val onReplyClicked = PublishSubject.create<Pair<Comment, Boolean>>()
        private val checkIfThereAnyPendingComments = PublishSubject.create<Boolean>()
        private val failedCommentCardToRefresh = PublishSubject.create<Pair<Comment, Int>>()
        private val showCanceledPledgeComment = PublishSubject.create<Comment>()

        private val closeCommentsPage = BehaviorSubject.create<Void>()
        private val currentUserAvatar = BehaviorSubject.create<String?>()
        private val commentComposerStatus = BehaviorSubject.create<CommentComposerStatus>()
        private val showCommentComposer = BehaviorSubject.create<Boolean>()
        private val commentsList = BehaviorSubject.create<List<CommentCardData>>()
        private val outputCommentList = BehaviorSubject.create<List<CommentCardData>>()
        private val showGuideLinesLink = BehaviorSubject.create<Void>()
        private val disableReplyButton = BehaviorSubject.create<Boolean>()
        private val scrollToTop = BehaviorSubject.create<Boolean>()

        private val insertNewCommentToList = PublishSubject.create<Pair<String, DateTime>>()
        private val isRefreshing = BehaviorSubject.create<Boolean>()
        private val setEmptyState = BehaviorSubject.create<Boolean>()
        private val displayPaginationError = BehaviorSubject.create<Boolean>()
        private val commentToRefresh = PublishSubject.create<Pair<Comment, Int>>()
        private val startThreadActivity = BehaviorSubject.create<Pair<CommentCardData, Boolean>>()
        private val hasPendingComments = BehaviorSubject.create<Pair<Boolean, Boolean>>()

        // - Error observables to handle the 3 different use cases
        private val internalError = BehaviorSubject.create<Throwable>()
        private val initialError = BehaviorSubject.create<Throwable>()
        private val paginationError = BehaviorSubject.create<Throwable>()
        private val pullToRefreshError = BehaviorSubject.create<Throwable>()
        private var commentableId: String? = null

        private val isFetchingComments = BehaviorSubject.create<Boolean>()
        private lateinit var project: Project

        init {

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
                    showCommentComposer.onNext(true)
                }

            val projectOrUpdate = intent()
                .map<Any?> {
                    val project = it.getParcelableExtra(IntentKey.PROJECT) as? Project
                    val update = it.getParcelableExtra(IntentKey.UPDATE)as? Update
                    project?.let {
                        Either.Left<Project?, Update?>(it)
                    }
                        ?: Either.Right<Project?, Update?>(update)
                }
                .ofType(Either::class.java)
                .take(1)

            val initialProject = projectOrUpdate.map {
                it as? Either<Project?, Update?>
            }.flatMap {
                it?.either<Observable<Project?>>(
                    { value: Project? -> Observable.just(value) },
                    { u: Update? -> client.fetchProject(u?.projectId().toString()).compose(Transformers.neverError()) }
                )
            }.map {
                requireNotNull(it)
            }.doOnNext {
                this.project = it
            }
                .share()

            initialProject
                .compose(combineLatestPair(currentUser.observable()))
                .compose(bindToLifecycle())
                .subscribe {
                    val composerStatus = getCommentComposerStatus(Pair(it.first, it.second))
                    showCommentComposer.onNext(composerStatus != CommentComposerStatus.GONE)
                    commentComposerStatus.onNext(composerStatus)
                }

            val projectOrUpdateComment = projectOrUpdate.map {
                it as? Either<Project?, Update?>
            }.compose(combineLatestPair(initialProject))
                .map {
                    Pair(it.second, it.first?.right())
                }

            loadCommentListFromProjectOrUpdate(projectOrUpdateComment)

            this.insertNewCommentToList
                .distinctUntilChanged()
                .withLatestFrom(this.currentUser.loggedInUser()) {
                    comment, user ->
                    Pair(comment, user)
                }
                .map {
                    Pair(it.first, buildCommentBody(Pair(it.second, it.first)))
                }
                .withLatestFrom(initialProject) {
                    commentData, project ->
                    Pair(commentData, project)
                }
                .map {
                    Pair(
                        it.first.first,
                        CommentCardData.builder()
                            .comment(it.first.second)
                            .project(it.second)
                            .commentableId(commentableId)
                            .commentCardState(CommentCardStatus.TRYING_TO_POST.commentCardStatus)
                            .build()
                    )
                }
                .doOnNext { scrollToTop.onNext(true) }
                .withLatestFrom(this.commentsList) { it, list ->
                    list.toMutableList().apply {
                        add(0, it.second)
                    }.toList()
                }.compose(bindToLifecycle())
                .subscribe {
                    commentsList.onNext(it)
                }

            this.onShowGuideLinesLinkClicked
                .compose(bindToLifecycle())
                .subscribe {
                    showGuideLinesLink.onNext(null)
                }

            this.commentsList
                .map { it.size }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    this.setEmptyState.onNext(it == 0)
                }

            this.paginationError
                .compose(bindToLifecycle())
                .subscribe {
                    this.displayPaginationError.onNext(true)
                }

            this.commentsList
                .compose(takePairWhen(checkIfThereAnyPendingComments))
                .compose(bindToLifecycle())
                .subscribe { pair ->
                    this.hasPendingComments.onNext(
                        Pair(
                            pair.first.any {
                                it.commentCardState == CommentCardStatus.TRYING_TO_POST.commentCardStatus ||
                                    it.commentCardState == CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus
                            },
                            pair.second
                        )
                    )
                }

            this.backPressed
                .compose(bindToLifecycle())
                .subscribe { this.closeCommentsPage.onNext(it) }

            commentsList
                .compose(takePairWhen(onReplyClicked))
                .compose(bindToLifecycle())
                .subscribe { pair ->

                    val cardData = pair.first.first { it.comment?.id() == pair.second.first.id() }
                    this.startThreadActivity.onNext(
                        Pair(
                            cardData,
                            pair.second.second
                        )
                    )
                }

            // - Update internal mutable list with the latest state after successful response
            this.commentsList
                .compose(takePairWhen(this.commentToRefresh))
                .map {
                    it.second.first.updateCommentAfterSuccessfulPost(it.first, it.second.second)
                }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    this.commentsList.onNext(it)
                }

            // - Reunite in only one place where the output list gets new updates
            this.commentsList
                .filter { it.isNotEmpty() }
                .compose(bindToLifecycle())
                .subscribe {
                    this.outputCommentList.onNext(it)
                }
            // - Update internal mutable list with the latest state after failed response
            this.commentsList
                .compose(takePairWhen(this.failedCommentCardToRefresh))
                .map {
                    it.second.first.updateCommentFailedToPost(it.first, it.second.second)
                }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    this.commentsList.onNext(it)
                }

            this.commentsList
                .compose(takePairWhen(this.showCanceledPledgeComment))
                .map {
                    it.second.updateCanceledPledgeComment(it.first)
                }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    this.commentsList.onNext(it)
                }
        }

        private fun loadCommentListFromProjectOrUpdate(projectOrUpdate: Observable<Pair<Project, Update?>>) {
            val startOverWith =
                Observable.merge(
                    projectOrUpdate,
                    projectOrUpdate.compose(
                        Transformers.takeWhen(
                            refresh
                        )
                    )
                )

            val apolloPaginate =
                ApolloPaginate.builder<CommentCardData, CommentEnvelope, Pair<Project, Update?>>()
                    .nextPage(nextPage)
                    .distinctUntilChanged(true)
                    .startOverWith(startOverWith)
                    .envelopeToListOfData {
                        mapToCommentCardDataList(Pair(it, this.project))
                    }
                    .loadWithParams {
                        loadWithProjectOrUpdateComments(Observable.just(it.first), it.second)
                    }
                    .clearWhenStartingOver(true)
                    .build()

            apolloPaginate.isFetching()
                .compose(bindToLifecycle<Boolean>())
                .subscribe(this.isFetchingComments)

            apolloPaginate.paginatedData()?.share()
                ?.compose(Transformers.combineLatestPair(this.isFetchingComments))
                ?.filter { !it.second }
                ?.subscribe {
                    this.commentsList.onNext(it.first)
                }

            this.internalError
                .compose(combineLatestPair(commentsList))
                .filter {
                    // it.first.second &&
                    it.second.isNullOrEmpty()
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.initialError.onNext(it.first)
                }

            this.internalError
                .compose(combineLatestPair(commentsList))
                .filter {
                    it.second.isNotEmpty()
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.paginationError.onNext(it.first)
                }

            this.refresh
                .doOnNext {
                    this.isRefreshing.onNext(true)
                }

            this.internalError
                .compose(combineLatestPair(isRefreshing))
                .compose(combineLatestPair(commentsList))
                .filter {
                    it.second.isNullOrEmpty()
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.isRefreshing.onNext(false)
                }
        }

        private fun loadWithProjectOrUpdateComments(
            projectOrUpdate: Observable<Pair<Project, Update?>>,
            cursor: String?
        ): Observable<CommentEnvelope> {
            return projectOrUpdate.switchMap {
                return@switchMap if (it.second?.id() != null) {
                    apolloClient.getProjectUpdateComments(it.second?.id().toString(), cursor)
                } else {
                    apolloClient.getProjectComments(it.first?.slug() ?: "", cursor)
                }
            }.doOnNext {
                commentableId = it.commentableId
            }
                .doOnError {
                    this.internalError.onNext(it)
                }
                .onErrorResumeNext(Observable.empty())
        }

        private fun mapToCommentCardDataList(it: Pair<CommentEnvelope, Project>) =
            it.first.comments?.map { comment: Comment ->
                CommentCardData.builder()
                    .comment(comment)
                    .project(it.second)
                    .commentCardState(comment.cardStatus())
                    .commentableId(it.first.commentableId)
                    .build()
            }

        private fun buildCommentBody(it: Pair<User, Pair<String, DateTime>>): Comment {
            return Comment.builder()
                .body(it.second.first)
                .parentId(-1)
                .authorBadges(listOf())
                .createdAt(it.second.second)
                .cursor("")
                .deleted(false)
                .id(-1)
                .authorCanceledPledge(false)
                .repliesCount(0)
                .author(it.first)
                .build()
        }

        private fun getCommentComposerStatus(projectAndUser: Pair<Project, User?>) =
            when {
                projectAndUser.second == null -> CommentComposerStatus.GONE
                projectAndUser.first.isBacking || ProjectUtils.userIsCreator(projectAndUser.first, projectAndUser.second) -> CommentComposerStatus.ENABLED
                else -> CommentComposerStatus.DISABLED
            }

        // - Inputs
        override fun backPressed() = backPressed.onNext(null)
        override fun refresh() = refresh.onNext(null)
        override fun nextPage() = nextPage.onNext(null)
        override fun insertNewCommentToList(comment: String, createdAt: DateTime) = insertNewCommentToList.onNext(Pair(comment, createdAt))
        override fun onShowGuideLinesLinkClicked() = onShowGuideLinesLinkClicked.onNext(null)
        override fun refreshComment(comment: Comment, position: Int) = this.commentToRefresh.onNext(Pair(comment, position))
        override fun onReplyClicked(comment: Comment, openKeyboard: Boolean) = onReplyClicked.onNext(Pair(comment, openKeyboard))
        override fun checkIfThereAnyPendingComments(isBackAction: Boolean) = checkIfThereAnyPendingComments.onNext(isBackAction)
        override fun refreshCommentCardInCaseFailedPosted(comment: Comment, position: Int) =
            this.failedCommentCardToRefresh.onNext(Pair(comment, position))
        override fun onShowCanceledPledgeComment(comment: Comment) =
            this.showCanceledPledgeComment.onNext(comment)
        // - Outputs
        override fun closeCommentsPage(): Observable<Void> = closeCommentsPage
        override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
        override fun commentComposerStatus(): Observable<CommentComposerStatus> = commentComposerStatus
        override fun showCommentComposer(): Observable<Boolean> = showCommentComposer
        override fun commentsList(): Observable<List<CommentCardData>> = this.outputCommentList
        override fun enableReplyButton(): Observable<Boolean> = disableReplyButton
        override fun showCommentGuideLinesLink(): Observable<Void> = showGuideLinesLink
        override fun initialLoadCommentsError(): Observable<Throwable> = this.initialError
        override fun paginateCommentsError(): Observable<Throwable> = this.paginationError
        override fun pullToRefreshError(): Observable<Throwable> = this.pullToRefreshError
        override fun scrollToTop(): Observable<Boolean> = this.scrollToTop
        override fun shouldShowPaginationErrorUI(): Observable<Boolean> = this.displayPaginationError

        override fun setEmptyState(): Observable<Boolean> = setEmptyState

        override fun startThreadActivity(): Observable<Pair<CommentCardData, Boolean>> = this.startThreadActivity
        override fun isFetchingComments(): Observable<Boolean> = this.isFetchingComments

        override fun hasPendingComments(): Observable<Pair<Boolean, Boolean>> = this.hasPendingComments
    }
}
