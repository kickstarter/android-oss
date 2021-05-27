package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.CommentEnvelopeFactory
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UpdateFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Comment
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.ui.IntentKey
import org.joda.time.DateTime
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class CommentsViewModelTest : KSRobolectricTestCase() {
    private val commentsList = TestSubscriber<List<Comment>?>()
    private val enableCommentComposer = TestSubscriber<Boolean>()
    private val showCommentComposer = TestSubscriber<Void>()
    private val showEmptyState = TestSubscriber<Boolean>()
    private val isLoadingMoreItems = TestSubscriber<Boolean>()
    private val isRefreshing = TestSubscriber<Boolean>()
    private val enablePagination = TestSubscriber<Boolean>()
    private val commentSubscriber = TestSubscriber<Comment>()
    private val commentPostedSubscriber = TestSubscriber<Comment>()

    @Test
    fun testCommentsViewModel_showCommentComposer_isLogInUser() {
        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build()
        )
        vm.outputs.enableCommentComposer().subscribe(enableCommentComposer)
        vm.outputs.showCommentComposer().subscribe(showCommentComposer)

        // Start the view model with a backed project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        // The comment composer should be shown to backer and enabled to write comments
        enableCommentComposer.assertValue(true)
        showCommentComposer.assertValueCount(1)
    }

    @Test
    fun testCommentsViewModel_showCommentComposer_isLogoutUser() {
        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().build()
        )

        vm.outputs.enableCommentComposer().subscribe(enableCommentComposer)
        vm.outputs.showCommentComposer().subscribe(showCommentComposer)

        // Start the view model with a backed project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        // The comment composer should be hidden and disabled to write comments as no user login -ed
        showCommentComposer.assertNoValues()
        enableCommentComposer.assertNoValues()
    }

    @Test
    fun testCommentsViewModel_enableCommentComposer_isBacking() {
        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build()
        )
        val enableCommentComposer = TestSubscriber<Boolean>()
        vm.outputs.enableCommentComposer().subscribe(enableCommentComposer)

        // Start the view model with a backed project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        // The comment composer enabled to write comments
        enableCommentComposer.assertValue(true)
    }

    @Test
    fun testCommentsViewModel_enableCommentComposer_isCreator() {
        val currentUser = UserFactory.user().toBuilder().id(1234).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .creator(currentUser)
            .isBacking(false)
            .build()
        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )
        val enableCommentComposer = TestSubscriber<Boolean>()
        vm.outputs.enableCommentComposer().subscribe(enableCommentComposer)

        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // The comment composer enabled to write comments for creator
        enableCommentComposer.assertValues(true)
    }

    @Test
    fun testCommentsViewModel_enableCommentComposer_notBackingNotCreator() {
        val creator = UserFactory.creator().toBuilder().id(222).build()
        val currentUser = UserFactory.user().toBuilder().id(111).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .isBacking(false)
            .build()
        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )
        val enableCommentComposer = TestSubscriber<Boolean>()
        vm.outputs.enableCommentComposer().subscribe(enableCommentComposer)

        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Comment composer should be disabled and shown the disabled msg if not backing and not creator.
        enableCommentComposer.assertValue(false)
    }
    @Test
    fun testCommentsViewModel_setCurrentUserAvatar() {
        val userAvatar = AvatarFactory.avatar()
        val currentUser = UserFactory.user().toBuilder().id(111).avatar(
            userAvatar
        ).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .isBacking(false)
            .build()

        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )
        val currentUserAvatar = TestSubscriber<String?>()
        vm.outputs.currentUserAvatar().subscribe(currentUserAvatar)
        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // set user avatar with small url
        currentUserAvatar.assertValue(userAvatar.small())
    }

    @Test
    fun testCommentsViewModel_EmptyState() {
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.empty()
            }
        }).build()
        val vm = CommentsViewModel.ViewModel(env)
        vm.outputs.commentsList().subscribe(commentsList)

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        commentsList.assertNoValues()
    }

    @Test
    fun testCommentsViewModel_ProjectCommentsEmit() {
        val vm = CommentsViewModel.ViewModel(environment())
        vm.outputs.commentsList().subscribe(commentsList)
        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        // Comments should emit.
        commentsList.assertValueCount(1)
    }

    /*
     * test when no comment available
     */
    @Test
    fun testCommentsViewModel_EmptyCommentState() {
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.just(CommentEnvelopeFactory.emptyCommentsEnvelope())
            }
        }).build()
        val vm = CommentsViewModel.ViewModel(env)
        vm.outputs.setEmptyState().subscribe(showEmptyState)

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        showEmptyState.assertValue(true)
    }

    /*
     * test when comment(s) available
     */
    @Test
    fun testCommentsViewModel_CommentsAvailableState() {
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.just(CommentEnvelopeFactory.commentsEnvelope())
            }
        }).build()
        val vm = CommentsViewModel.ViewModel(env)
        vm.outputs.setEmptyState().subscribe(showEmptyState)

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        showEmptyState.assertValue(false)
    }

    @Test
    fun testCommentsViewModel_ProjectRefresh() {
        val vm = CommentsViewModel.ViewModel(environment())
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        vm.outputs.isRefreshing().subscribe(isRefreshing)

        // Start the view model with a project.
        vm.inputs.refresh()
        vm.outputs.commentsList().subscribe(commentsList)

        // Comments should emit.
        isRefreshing.assertValues(false, true, false)
        commentsList.assertValueCount(1)
    }

    /*
  * test Pagination
  */
    @Test
    fun testCommentsViewModel_ProjectLoadingMore() {
        var firstCall = true
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return if (firstCall)
                    Observable.just(CommentEnvelopeFactory.commentsEnvelope())
                else
                    Observable.just(CommentEnvelopeFactory.emptyCommentsEnvelope())
            }
        }).build()

        val vm = CommentsViewModel.ViewModel(env)
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        vm.outputs.enablePagination().subscribe(enablePagination)
        vm.outputs.isLoadingMoreItems().subscribe(isLoadingMoreItems)

        enablePagination.assertValues(true)

        firstCall = false
        // get the next page which is end of page
        vm.inputs.nextPage()
        vm.outputs.commentsList().subscribe(commentsList)

        isLoadingMoreItems.assertValues(false, true, false)
        enablePagination.assertValues(true, false)
        commentsList.assertValueCount(1)
    }

    /*
     * test when comment(s) available
     */
    @Test
    fun testCommentsViewModel_PostComment_CommentAddedToView() {
        val userAvatar = AvatarFactory.avatar()
        val currentUser = UserFactory.user().toBuilder().id(1).avatar(
            userAvatar
        ).build()

        val createdAt = DateTime.now()

        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun createComment(comment: PostCommentData): Observable<Comment> {
                return Observable.just(CommentFactory.liveComment(createdAt = createdAt))
            }
        }).build()

        val vm = CommentsViewModel.ViewModel(
            env.toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        vm.outputs.insertComment().subscribe(commentSubscriber)
        vm.outputs.commentPosted().subscribe(commentPostedSubscriber)

        // post a comment
        vm.inputs.postComment("Some Comment", createdAt)

        commentPostedSubscriber.assertValue(CommentFactory.liveComment(createdAt = createdAt))
        commentSubscriber.assertValue(CommentFactory.liveComment(createdAt = createdAt))
    }

    @Test
    fun testCommentsViewModel_PostComment_FailedComment() {
        val userAvatar = AvatarFactory.avatar()
        val currentUser = UserFactory.user().toBuilder().id(1).avatar(
            userAvatar
        ).build()

        val createdAt = DateTime.now()

        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun createComment(comment: PostCommentData): Observable<Comment> {
                return Observable.error(Throwable())
            }
        }).build()

        val vm = CommentsViewModel.ViewModel(
            env.toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        vm.outputs.insertComment().subscribe(commentSubscriber)
        vm.outputs.updateFailedComment().subscribe(commentPostedSubscriber)

        // post a comment
        vm.inputs.postComment("Some Comment", createdAt)

        commentPostedSubscriber.assertValue(CommentFactory.liveComment(createdAt = createdAt))
        commentSubscriber.assertValue(CommentFactory.liveComment(createdAt = createdAt))
    }
}
