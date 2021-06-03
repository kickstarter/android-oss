package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.loadmore.LoadingType
import com.kickstarter.libs.loadmore.PaginatedViewModelOutput
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.CommentsActivity
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentComposerStatus
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface CommentsViewModel {

    interface Inputs {
        fun refresh()
        fun nextPage()
        fun postComment(comment: String, createdAt: DateTime)
        fun retryPostComment(comment: Comment, position: Int)
    }

    interface Outputs : PaginatedViewModelOutput<CommentCardData> {
        fun currentUserAvatar(): Observable<String?>
        fun commentComposerStatus(): Observable<CommentComposerStatus>
        fun enableReplyButton(): Observable<Boolean>
        fun showCommentComposer(): Observable<Boolean>
        fun commentsList(): Observable<List<CommentCardData>>
        fun setEmptyState(): Observable<Boolean>
        fun insertComment(): Observable<CommentCardData>
        fun commentPosted(): Observable<CommentCardData>
        fun updateFailedComment(): Observable<CommentCardData>
        fun updateCommentStatus(): Observable<CommentCardData>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<CommentsActivity>(environment), Inputs, Outputs {

        private val currentUser: CurrentUserType = environment.currentUser()
        private val client: ApiClientType = environment.apiClient()
        private val apolloClient: ApolloClientType = environment.apolloClient()
        val inputs: Inputs = this
        val outputs: Outputs = this
        private val refresh = PublishSubject.create<Void>()
        private val nextPage = PublishSubject.create<Void>()

        private val currentUserAvatar = BehaviorSubject.create<String?>()
        private val commentComposerStatus = BehaviorSubject.create<CommentComposerStatus>()
        private val showCommentComposer = BehaviorSubject.create<Boolean>()
        private val commentsList = BehaviorSubject.create<List<CommentCardData>?>()
        private val disableReplyButton = BehaviorSubject.create<Boolean>()

        private val postComment = PublishSubject.create<Pair<String, DateTime>>()
        private val retryPostCommentData = PublishSubject.create<Pair<Comment, Int>>()
        private val isLoadingMoreItems = BehaviorSubject.create<Boolean>()
        private val isRefreshing = BehaviorSubject.create<Boolean>()
        private val enablePagination = BehaviorSubject.create<Boolean>()
        private val setEmptyState = BehaviorSubject.create<Boolean>()
        private val insertComment = BehaviorSubject.create<CommentCardData>()
        private val commentPosted = BehaviorSubject.create<CommentCardData>()
        private val updateFailedComment = BehaviorSubject.create<CommentCardData>()
        private val failedPostedCommentObserver = BehaviorSubject.create<Void>()
        private val updateCommentStatus = BehaviorSubject.create<CommentCardData>()

        private var lastCommentCursour: String? = null
        override var loadMoreListData = mutableListOf<CommentCardData>()
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
            }.map { requireNotNull(it) }
                .share()

            initialProject
                .compose(combineLatestPair(currentUser.observable()))
                .compose(bindToLifecycle())
                .subscribe {
                    val composerStatus = getCommentComposerStatus(Pair(it.first, it.second))
                    showCommentComposer.onNext(composerStatus != CommentComposerStatus.GONE)
                    commentComposerStatus.onNext(composerStatus)
                }

            loadCommentList(initialProject)

            val postComment = postComment
                    .compose(combineLatestPair(initialProject))
                    .switchMap {
                        it.second?.let { project ->
                            this.apolloClient.createComment(
                                    PostCommentData(
                                            project = project,
                                            body = it.first.first,
                                            clientMutationId = null,
                                            parentId = null
                                    )
                            )
                        }
                    }
                    .compose<Pair<Comment, Project?>>(combineLatestPair(initialProject))
                    .map {
                        CommentCardData.builder().comment(it.first).project(it.second).build()
                    }
                    .onErrorResumeNext {
                        this.failedPostedCommentObserver.onNext(null)
                        Observable.empty()
                    } // add this line
                    .share()

            this.currentUser.loggedInUser()
                .compose(Transformers.takePairWhen(this.postComment))
                .compose(Transformers.takePairWhen(this.failedPostedCommentObserver))
                .map {
                    buildCommentBody(it.first)
                }
                .compose<Pair<Comment, Project?>>(combineLatestPair(initialProject))
                .map {
                    CommentCardData.builder()
                        .comment(it.first)
                        .project(it.second)
                        .commentCardState(CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus)
                        .build()
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.updateFailedComment.onNext(it)
                }

            Observable
                    .combineLatest(initialProject, this.postComment) { project, pair ->
                        return@combineLatest this.apolloClient.createComment(
                                PostCommentData(
                                        project = project,
                                        body = pair.first,
                                        clientMutationId = null,
                                        parentId = null
                                )).doOnError {
                            this.failedPostedCommentObserver.onNext(null)
                        }
                                .onErrorResumeNext(Observable.empty())
                    }
                    .switchMap {
                        it
                    }
                    .compose<Pair<Comment, Project?>>(combineLatestPair(initialProject))
                    .map {
                        CommentCardData.builder().comment(it.first).project(it.second).build()
                    }
                    .subscribe {
                        this.commentPosted.onNext(it)
                    }

            this.currentUser.loggedInUser()
                .compose(Transformers.takePairWhen(this.postComment))
                .compose(Transformers.takePairWhen(this.failedPostedCommentObserver))
                .map {
                    buildCommentBody(it.first)
                }
                .compose<Pair<Comment, Project?>>(combineLatestPair(initialProject))
                .map {
                    CommentCardData.builder()
                        .comment(it.first)
                        .project(it.second)
                        .commentCardState(CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus).build()
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.updateFailedComment.onNext(it)
                }

            // postCommentToServer(postComment, false)

//            val retryPosting = initialProject
//                .compose(Transformers.takePairWhen(this.retryPostCommentData))
//                .map {
//                    Pair(it.second, it.first)
//                }
//                .delay(500, TimeUnit.MILLISECONDS)

//            this.currentUser.loggedInUser()
//                .compose(Transformers.takePairWhen(this.retryPostCommentData))
//                .compose<Pair<Pair<User, Pair<Comment, Int>>, Project?>>(combineLatestPair(initialProject))
//                .map {
//                    Pair(
//                        CommentCardData.builder()
//                            .comment(it.first.second.first)
//                            .project(it.second)
//                            .commentCardState(CommentCardStatus.TRYING_TO_POST.commentCardStatus)
//                            .build(),
//                        it.first.second.second
//                    )
//                }
//                .compose(bindToLifecycle())
//                .subscribe {
//                    this.updateCommentStatus.onNext(it)
//                }
//
//            postCommentToServer(retryPosting, true)
        }

        private fun loadCommentList(initialProject: Observable<Project>) {
            val projectSlug = initialProject
                .map { requireNotNull(it?.slug()) }

            projectSlug
                .compose(CommentEnvelopeTransformer(initialProject))
                .compose(bindToLifecycle())
                .subscribe {
                    bindCommentList(it.first, LoadingType.NORMAL, it.second)
                }

            projectSlug
                .compose(Transformers.takeWhen(this.nextPage))
                .doOnNext {
                    this.isLoadingMoreItems.onNext(true)
                }
                .compose(CommentEnvelopeTransformer(initialProject))
                .compose(bindToLifecycle())
                .subscribe {
                    updatePaginatedData(
                        LoadingType.LOAD_MORE,
                        it.first
                    )
                }

            projectSlug
                .compose(Transformers.takeWhen(this.refresh))
                .doOnNext {
                    this.isRefreshing.onNext(true)
                    // reset cursor
                    lastCommentCursour = null
                }.compose(CommentEnvelopeTransformer(initialProject))
                .compose(bindToLifecycle())
                .subscribe {
                    bindCommentList(it.first, LoadingType.PULL_REFRESH, it.second)
                }

            this.currentUser.loggedInUser()
                .compose(Transformers.takePairWhen(this.postComment))
                .map {
                    buildCommentBody(it)
                }
                .compose<Pair<Comment, Project?>>(combineLatestPair(initialProject))
                .map {
                    CommentCardData.builder().comment(it.first).project(it.second).build()
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.insertComment.onNext(it)
                }

            this.currentUser.loggedInUser()
                .compose(Transformers.takePairWhen(this.postComment))
                .compose(Transformers.takePairWhen(this.failedPostedCommentObserver))
                .map {
                    buildCommentBody(it.first)
                }
                .compose<Pair<Comment, Project?>>(combineLatestPair(initialProject))
                .map {
                    CommentCardData.builder().comment(it.first).project(it.second).build()
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.updateFailedComment.onNext(it)
                }

            initialProject
                .compose(Transformers.takePairWhen(this.postComment))
                .compose(bindToLifecycle())
                .switchMap {
                    it.first?.let { project ->
                        this.apolloClient.createComment(
                            PostCommentData(
                                project = project,
                                body = it.second.first,
                                clientMutationId = null,
                                parentId = null
                            )
                        )
                    }
                }
                .compose<Pair<Comment, Project?>>(combineLatestPair(initialProject))
                .map {
                    CommentCardData.builder().comment(it.first).project(it.second).build()
                }
                .subscribe(
                    {
                        this.commentPosted.onNext(it)
                    },
                    {
                        this.failedPostedCommentObserver.onNext(null)
                    }
                )
        }

        private fun mapToCommentCardDataList(it: Pair<CommentEnvelope, Project?>) =
            it.first.comments?.map { comment: Comment ->
                CommentCardData.builder().comment(comment).project(it.second).build()
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
                .repliesCount(0)
                .author(it.first)
                .build()
        }

        private fun bindCommentList(commentCardDataList: List<CommentCardData>, loadingType: LoadingType, totalCount: Int?) {
            totalCount?.let { count ->
                this.setEmptyState.onNext(count < 1)
                updatePaginatedData(
                    loadingType,
                    commentCardDataList

                )
            }
        }

        inner class CommentEnvelopeTransformer(private val initialProject: Observable<Project>) : Observable.Transformer<String, Pair<List<CommentCardData>, Int>> {
            override fun call(t: Observable<String>): Observable<Pair<List<CommentCardData>, Int>> {
                return t.switchMap {
                    apolloClient.getProjectComments(it, lastCommentCursour)
                }
                    .filter { ObjectUtils.isNotNull(it) }
                    .compose<Pair<CommentEnvelope, Project?>>(combineLatestPair(initialProject))
                    .map { Pair(mapToCommentCardDataList(it), it.first.totalCount) }
            }
        }

        private fun getCommentComposerStatus(projectAndUser: Pair<Project, User?>) =
            when {
                projectAndUser.second == null -> CommentComposerStatus.GONE
                projectAndUser.first.isBacking || ProjectUtils.userIsCreator(projectAndUser.first, projectAndUser.second) -> CommentComposerStatus.ENABLED
                else -> CommentComposerStatus.DISABLED
            }

        override fun refresh() = refresh.onNext(null)
        override fun nextPage() = nextPage.onNext(null)

        override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
        override fun commentComposerStatus(): Observable<CommentComposerStatus> = commentComposerStatus
        override fun showCommentComposer(): Observable<Boolean> = showCommentComposer
        override fun commentsList(): Observable<List<CommentCardData>> = commentsList
        override fun enableReplyButton(): Observable<Boolean> = disableReplyButton

        override fun setEmptyState(): Observable<Boolean> = setEmptyState
        override fun isLoadingMoreItems(): Observable<Boolean> = isLoadingMoreItems
        override fun enablePagination(): Observable<Boolean> = enablePagination
        override fun isRefreshing(): Observable<Boolean> = isRefreshing
        override fun insertComment(): Observable<CommentCardData> = this.insertComment
        override fun commentPosted(): Observable<CommentCardData> = this.commentPosted
        override fun updateFailedComment(): Observable<CommentCardData> = this.updateFailedComment
        override fun updateCommentStatus(): Observable<CommentCardData> = this.updateCommentStatus

        override fun postComment(comment: String, createdAt: DateTime) = postComment.onNext(Pair(comment, createdAt))
        override fun retryPostComment(comment: Comment, position: Int) = retryPostCommentData.onNext(Pair(comment, position))

        override fun bindPaginatedData(data: List<CommentCardData>?) {
            lastCommentCursour = data?.lastOrNull()?.comment?.cursor()
            data?.let { loadMoreListData.addAll(it) }
            commentsList.onNext(loadMoreListData)
            this.isRefreshing.onNext(false)
            this.isLoadingMoreItems.onNext(false)
        }

        override fun updatePaginatedState(enabled: Boolean) {
            enablePagination.onNext(enabled)
        }
    }
}
