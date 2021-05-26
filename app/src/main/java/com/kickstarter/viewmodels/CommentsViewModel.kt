package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProjectUtils
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApolloClientType
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.CommentsActivity
import com.kickstarter.ui.data.ProjectData
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface CommentsViewModel {

    interface Inputs {
        fun postComment(comment: String, createdAt: DateTime)
    }

    interface Outputs {
        fun currentUserAvatar(): Observable<String?>
        fun enableCommentComposer(): Observable<Boolean>
        fun showCommentComposer(): Observable<Void>
        fun commentsList(): Observable<List<Comment>>
        fun setEmptyState(): Observable<Boolean>
        fun insertComment(): Observable<Comment>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<CommentsActivity>(environment), Inputs, Outputs {

        private val currentUser: CurrentUserType = environment.currentUser()
        private val client: ApiClientType = environment.apiClient()
        private val apolloClient: ApolloClientType = environment.apolloClient()
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val currentUserAvatar = BehaviorSubject.create<String?>()
        private val enableCommentComposer = BehaviorSubject.create<Boolean>()
        private val showCommentComposer = BehaviorSubject.create<Void>()
        private val commentsList = BehaviorSubject.create<List<Comment>?>()

        private val postComment = PublishSubject.create<Pair<String, DateTime>>()
        private val setEmptyState = BehaviorSubject.create<Boolean>()
        private val insertComment = BehaviorSubject.create<Comment>()

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
            }.share()

            Observable.combineLatest(
                loggedInUser,
                initialProject
            ) { a: User?, b: Project? ->
                Pair.create(a, b)
            }.compose(bindToLifecycle())
                .subscribe {
                    it.second?.let { project ->
                        enableCommentComposer.onNext(isProjectBackedOrUserIsCreator(Pair(project, it.first)))
                    }
                }

            val commentEnvelope = initialProject
                .map { requireNotNull(it?.slug()) }
                .switchMap {
                    this.apolloClient.getProjectComments(it, null)
                }
                .filter { ObjectUtils.isNotNull(it) }
                .share()

            commentEnvelope
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    it.totalCount?.let { count ->
                        this.setEmptyState.onNext(count < 1)
                        commentsList.onNext(it.comments)
                    }
                }

            this.currentUser.loggedInUser()
                .compose(Transformers.takePairWhen(this.postComment))
                .compose(bindToLifecycle())
                .subscribe {
                    val comment = Comment.builder()
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

                    this.insertComment.onNext(comment)
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
                .subscribe(
                    {
                    },
                    {
                    }
                )
        }

        private fun isProjectBackedOrUserIsCreator(pair: Pair<Project, User>) =
            pair.first.isBacking || ProjectUtils.userIsCreator(pair.first, pair.second)

        override fun currentUserAvatar(): Observable<String?> = currentUserAvatar
        override fun enableCommentComposer(): Observable<Boolean> = enableCommentComposer
        override fun showCommentComposer(): Observable<Void> = showCommentComposer
        override fun commentsList(): Observable<List<Comment>> = commentsList

        override fun setEmptyState(): Observable<Boolean> = setEmptyState
        override fun insertComment(): Observable<Comment> = this.insertComment
        override fun postComment(comment: String, createdAt: DateTime) = postComment.onNext(Pair(comment, createdAt))
    }
}
