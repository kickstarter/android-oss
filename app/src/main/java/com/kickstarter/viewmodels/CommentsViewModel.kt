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
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.views.CommentCardStatus
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

interface CommentsViewModel {

    interface Inputs {
        fun refresh()
        fun nextPage()
        fun postComment(comment: String, createdAt: DateTime)
        fun retryPostComment(comment: Comment)
    }

    interface Outputs : PaginatedViewModelOutput<Comment> {
        fun currentUserAvatar(): Observable<String?>
        fun enableCommentComposer(): Observable<Boolean>
        fun showCommentComposer(): Observable<Void>
        fun commentsList(): Observable<List<Comment>>
        fun setEmptyState(): Observable<Boolean>
        fun insertComment(): Observable<Comment>
        fun updateCommentStatus(): Observable<Pair<Comment, CommentCardStatus>>
        fun commentPosted(): Observable<Comment>
        fun updateFailedComment(): Observable<Comment>
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
        private val enableCommentComposer = BehaviorSubject.create<Boolean>()
        private val showCommentComposer = BehaviorSubject.create<Void>()
        private val commentsList = BehaviorSubject.create<List<Comment>?>()

        private val postComment = PublishSubject.create<Pair<String, DateTime>>()
        private val retryPostCommentData = PublishSubject.create<Comment>()
        private val isLoadingMoreItems = BehaviorSubject.create<Boolean>()
        private val isRefreshing = BehaviorSubject.create<Boolean>()
        private val enablePagination = BehaviorSubject.create<Boolean>()
        private val setEmptyState = BehaviorSubject.create<Boolean>()
        private val insertComment = BehaviorSubject.create<Comment>()
        private val updateCommentStatus = BehaviorSubject.create<Pair<Comment, CommentCardStatus>>()
        private val commentPosted = BehaviorSubject.create<Comment>()
        private val updateFailedComment = BehaviorSubject.create<Comment>()
        private val failedPostedCommentObserver = BehaviorSubject.create<Void>()

        private var lastCommentCursour: String? = null
        override var loadMoreListData = mutableListOf<Comment>()
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
                    showCommentComposer.onNext(null)
                }

            intent()
                .map { it.getParcelableExtra(IntentKey.PROJECT_DATA) as ProjectData? }
                .ofType(ProjectData::class.java)
                .compose(bindToLifecycle())
                .subscribe {
                    enableCommentComposer.onNext(isProjectBackedOrUserIsCreator(Pair(it.project(), it.user())))
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

            Observable.combineLatest(
                loggedInUser,
                initialProject
            ) { a: User?, b: Project ->
                Pair.create(a, b)
            }.compose(bindToLifecycle())
                .subscribe {
                    it.second?.let { project ->
                        enableCommentComposer.onNext(isProjectBackedOrUserIsCreator(Pair(project, it.first)))
                    }
                }

            val projectSlug = initialProject
                .map { requireNotNull(it?.slug()) }

            val commentEnvelope = projectSlug
                .switchMap {
                    this.apolloClient.getProjectComments(it, lastCommentCursour)
                }
                .filter { ObjectUtils.isNotNull(it) }

            commentEnvelope
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    bindCommentList(it, LoadingType.NORMAL)
                }

            projectSlug
                .compose(Transformers.takeWhen(this.nextPage))
                .doOnNext {
                    this.isLoadingMoreItems.onNext(true)
                }
                .switchMap {
                    this.apolloClient.getProjectComments(it, lastCommentCursour)
                }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    updatePaginatedData(
                        LoadingType.LOAD_MORE,
                        it.comments
                    )
                }

            projectSlug
                .compose(Transformers.takeWhen(this.refresh))
                .doOnNext {
                    this.isRefreshing.onNext(true)
                }
                .switchMap {
                    this.apolloClient.getProjectComments(it, null)
                }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    bindCommentList(it, LoadingType.PULL_REFRESH)
                }

            this.currentUser.loggedInUser()
                .compose(Transformers.takePairWhen(this.postComment))
                .compose(bindToLifecycle())
                .subscribe {
                    this.insertComment.onNext(buildCommentBody(it))
                }

            this.currentUser.loggedInUser()
                    .compose(Transformers.takePairWhen(this.retryPostCommentData))
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.updateCommentStatus.onNext(Pair(it.second, CommentCardStatus.TRYING_TO_POST))
                    }

            this.currentUser.loggedInUser()
                .compose(Transformers.takePairWhen(this.postComment))
                .compose(Transformers.takePairWhen(this.failedPostedCommentObserver))
                .compose(bindToLifecycle())
                .subscribe {
                    this.updateFailedComment.onNext(buildCommentBody(it.first))
                }

            this.currentUser.loggedInUser()
                    .compose(Transformers.takePairWhen(this.postComment))
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.insertComment.onNext(buildCommentBody(it))
                    }

          val postComment = initialProject
                .compose(Transformers.takePairWhen(this.postComment))

            postCommentToServer(postComment, false)

            val retryPosting = initialProject
                    .compose(Transformers.takePairWhen(this.retryPostCommentData))
                    .map {
                        Pair(it.first, Pair(it.second.body(), it.second.createdAt()))
                    }.delay(500, TimeUnit.MILLISECONDS)
            postCommentToServer(retryPosting, true)
        }

        private fun postCommentToServer(postComment: Observable<Pair<Project, Pair<String, DateTime>>>, isRetrying: Boolean) {
            postComment.compose(bindToLifecycle())
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
                    }.subscribe(
                            {
                                if (isRetrying) {
                                    this.updateCommentStatus.onNext(Pair(it, CommentCardStatus.POSTING_COMMENT_COMPLETED_SUCCESSFULLY))
                                } else {
                                    this.commentPosted.onNext(it)
                                }
                            },
                            {
                                this.failedPostedCommentObserver.onNext(null)
                            }
                    )
        }

        private fun buildCommentBody(it: Pair<User, Pair<String, DateTime>>): Comment? {
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

        private fun bindCommentList(commentEnvelope: CommentEnvelope, loadingType: LoadingType) {
            commentEnvelope.totalCount?.let { count ->
                this.setEmptyState.onNext(count < 1)
                updatePaginatedData(
                    loadingType,
                    commentEnvelope.comments

                )
            }
        }

        private fun isProjectBackedOrUserIsCreator(pair: Pair<Project, User>) =
            pair.first.isBacking || ProjectUtils.userIsCreator(pair.first, pair.second)

        override fun refresh() = refresh.onNext(null)
        override fun nextPage() = nextPage.onNext(null)

        override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
        override fun enableCommentComposer(): Observable<Boolean> = enableCommentComposer
        override fun showCommentComposer(): Observable<Void> = showCommentComposer
        override fun commentsList(): Observable<List<Comment>> = commentsList
        override fun setEmptyState(): Observable<Boolean> = setEmptyState
        override fun isLoadingMoreItems(): Observable<Boolean> = isLoadingMoreItems
        override fun enablePagination(): Observable<Boolean> = enablePagination
        override fun isRefreshing(): Observable<Boolean> = isRefreshing
        override fun insertComment(): Observable<Comment> = this.insertComment
        override fun updateCommentStatus(): Observable<Pair<Comment, CommentCardStatus>> = updateCommentStatus
        override fun commentPosted(): Observable<Comment> = this.commentPosted
        override fun updateFailedComment(): Observable<Comment> = this.updateFailedComment

        override fun postComment(comment: String, createdAt: DateTime) = postComment.onNext(Pair(comment, createdAt))
        override fun retryPostComment(comment: Comment) = retryPostCommentData.onNext(comment)

        override fun bindPaginatedData(data: List<Comment>?) {
            lastCommentCursour = data?.lastOrNull()?.cursor()
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
