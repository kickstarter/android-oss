package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.CommentEnvelopeFactory
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UpdateFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.ui.views.CommentComposerStatus
import org.joda.time.DateTime
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import rx.schedulers.TestScheduler
import rx.subjects.BehaviorSubject
import java.lang.Exception
import java.util.concurrent.TimeUnit

class CommentsViewModelTest : KSRobolectricTestCase() {
    private val closeCommentPage = TestSubscriber<Void>()
    private val commentsList = TestSubscriber<List<CommentCardData>?>()
    private val commentComposerStatus = TestSubscriber<CommentComposerStatus>()
    private val showCommentComposer = TestSubscriber<Boolean>()
    private val showEmptyState = TestSubscriber<Boolean>()
    private val scrollToTop = BehaviorSubject.create<Boolean>()
    private val initialLoadError = TestSubscriber.create<Throwable>()
    private val paginationError = TestSubscriber.create<Throwable>()
    private val shouldShowPaginatedCell = TestSubscriber.create<Boolean>()
    private val openCommentGuideLines = TestSubscriber<Void>()
    private val startThreadActivity = BehaviorSubject.create<Pair<Pair<Comment, Boolean>, Project>>()

    @Test
    fun testCommentsViewModel_whenUserLoggedInAndBacking_shouldShowEnabledComposer() {
        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build()
        )
        vm.outputs.commentComposerStatus().subscribe(commentComposerStatus)
        vm.outputs.showCommentComposer().subscribe(showCommentComposer)

        // Start the view model with a backed project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        // The comment composer should be shown to backer and enabled to write comments
        commentComposerStatus.assertValue(CommentComposerStatus.ENABLED)
        showCommentComposer.assertValues(true, true)
    }

    @Test
    fun testCommentsViewModel_whenUserIsLoggedOut_composerShouldBeGone() {
        val vm = CommentsViewModel.ViewModel(environment().toBuilder().build())

        vm.outputs.commentComposerStatus().subscribe(commentComposerStatus)
        vm.outputs.showCommentComposer().subscribe(showCommentComposer)

        // Start the view model with a backed project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        // The comment composer should be hidden and disabled to write comments as no user logged-in
        showCommentComposer.assertValue(false)
        commentComposerStatus.assertValue(CommentComposerStatus.GONE)
    }

    @Test
    fun testCommentsViewModel_whenUserIsLoggedInNotBacking_shouldShowDisabledComposer() {
        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build()
        )
        vm.outputs.commentComposerStatus().subscribe(commentComposerStatus)
        vm.outputs.showCommentComposer().subscribe(showCommentComposer)

        // Start the view model with a backed project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        // The comment composer should show but in disabled state
        commentComposerStatus.assertValue(CommentComposerStatus.DISABLED)
        showCommentComposer.assertValues(true, true)
    }

    @Test
    fun testCommentsViewModel_whenUserIsCreator_shouldShowEnabledComposer() {
        val currentUser = UserFactory.user().toBuilder().id(1234).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .creator(currentUser)
            .isBacking(false)
            .build()
        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )

        vm.outputs.commentComposerStatus().subscribe(commentComposerStatus)
        vm.outputs.showCommentComposer().subscribe(showCommentComposer)

        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // The comment composer enabled to write comments for creator
        showCommentComposer.assertValues(true, true)
        commentComposerStatus.assertValues(CommentComposerStatus.ENABLED)
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

        vm.outputs.commentComposerStatus().subscribe(commentComposerStatus)
        vm.outputs.showCommentComposer().subscribe(showCommentComposer)

        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Comment composer should be disabled and shown the disabled msg if not backing and not creator.
        showCommentComposer.assertValues(true, true)
        commentComposerStatus.assertValue(CommentComposerStatus.DISABLED)
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
            override fun getProjectUpdateComments(
                updateId: String,
                cursor: String?,
                limit: Int
            ): Observable<CommentEnvelope> {
                return Observable.empty()
            }
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.empty()
            }
        }).build()
        val vm = CommentsViewModel.ViewModel(env)
        val commentsList = TestSubscriber<List<CommentCardData>>()
        vm.outputs.commentsList().subscribe(commentsList)

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        commentsList.assertNoValues()

        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        commentsList.assertNoValues()
    }

    @Test
    fun testCommentsViewModel_InitialLoad_Error() {
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectUpdateComments(
                updateId: String,
                cursor: String?,
                limit: Int
            ): Observable<CommentEnvelope> {
                return Observable.error(Exception())
            }

            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.error(Exception())
            }
        }).build()
        val vm = CommentsViewModel.ViewModel(env)
        val commentsList = TestSubscriber<List<CommentCardData>?>()
        vm.outputs.commentsList().subscribe(commentsList)

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        commentsList.assertNoValues()

        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        commentsList.assertNoValues()
    }

    @Test
    fun testCommentsViewModel_ProjectCommentsEmit() {
        val commentsList = BehaviorSubject.create<List<CommentCardData>?>()
        val vm = CommentsViewModel.ViewModel(environment())
        vm.outputs.commentsList().subscribe(commentsList)
        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        // Comments should emit.
        val commentCardDataList = commentsList.value
        assertEquals(1, commentCardDataList?.size)
        assertEquals(CommentFactory.comment(), commentCardDataList?.get(0)?.comment)
    }

    /*
   * test when no comment available
   */

    @Test
    fun testCommentsViewModel_EmptyCommentState() {
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {

            override fun getProjectComments(
                slug: String,
                cursor: String?,
                limit: Int
            ): Observable<CommentEnvelope> {
                return Observable.just(CommentEnvelopeFactory.emptyCommentsEnvelope())
            }

            override fun getProjectUpdateComments(
                updateId: String,
                cursor: String?,
                limit: Int
            ): Observable<CommentEnvelope> {
                return Observable.just(CommentEnvelopeFactory.emptyCommentsEnvelope())
            }
        }).build()
        val vm = CommentsViewModel.ViewModel(env)
        vm.outputs.setEmptyState().subscribe(showEmptyState)

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        showEmptyState.assertValue(true)

        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
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
        showEmptyState.assertValues(true, false)
    }

    @Test
    fun testCommentsViewModel_ProjectRefresh() {
        val vm = CommentsViewModel.ViewModel(environment())
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        // Start the view model with a project.
        vm.inputs.refresh()
        vm.outputs.commentsList().subscribe(commentsList)

        // Comments should emit.
        commentsList.assertValueCount(1)
    }

    @Test
    fun testCommentsViewModel_ProjectRefresh_AndInitialLoad_withError() {
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }).build()

        val vm = CommentsViewModel.ViewModel(env)
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        vm.outputs.initialLoadCommentsError().subscribe(initialLoadError)
        vm.outputs.paginateCommentsError().subscribe(paginationError)
        vm.outputs.shouldShowPaginationErrorUI().subscribe(shouldShowPaginatedCell)

        // Start the view model with a project.
        vm.inputs.refresh()
        vm.outputs.commentsList().subscribe(commentsList)

        // Comments should emit.
        commentsList.assertValueCount(0)
        initialLoadError.assertValueCount(3)
        shouldShowPaginatedCell.assertNoValues()
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

        firstCall = false
        // get the next page which is end of page
        vm.inputs.nextPage()
        vm.outputs.commentsList().subscribe(commentsList)

        commentsList.assertValueCount(1)
    }

    /*
     * test when comment(s) available
     */
    @Test
    fun testCommentsViewModel_PostComment_CommentAddedToView() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .avatar(AvatarFactory.avatar())
            .build()

        val createdAt = DateTime.now()

        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )

        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser)
        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        val commentsList = BehaviorSubject.create<List<CommentCardData>>()
        vm.outputs.commentsList().subscribe(commentsList)
        vm.outputs.scrollToTop().subscribe(scrollToTop)

        // post a comment
        vm.inputs.insertNewCommentToList(commentCardData.comment?.body()!!, createdAt)
        assertEquals(2, commentsList.value?.size)
        assertEquals(CommentFactory.comment(), commentsList.value?.last()?.comment)
    }

    @Test
    fun testCommentsViewModel_openCommentGuidelinesLink() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .avatar(AvatarFactory.avatar())
            .build()

        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )

        // Start the view model with an update.
        vm.intent(Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update()))
        vm.outputs.showCommentGuideLinesLink().subscribe(openCommentGuideLines)

        // post a comment
        vm.inputs.onShowGuideLinesLinkClicked()
        openCommentGuideLines.assertValueCount(1)
    }

    @Test
    fun backButtonPressed_whenEmits_shouldEmitToCloseActivityStream() {
        val vm = CommentsViewModel.ViewModel(environment())
        vm.outputs.closeCommentsPage().subscribe(closeCommentPage)

        vm.inputs.backPressed()
        closeCommentPage.assertValueCount(1)
    }

    @Test
    fun onReplyClicked_whenEmits_shouldEmitToCloseThreadActivity() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .avatar(AvatarFactory.avatar())
            .build()

        val createdAt = DateTime.now()

        val vm = CommentsViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )
        val comment = CommentFactory.liveComment(createdAt = createdAt)
        val project = ProjectFactory.project()
        vm.outputs.startThreadActivity().subscribe(startThreadActivity)
        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Start the view model with a project.

        vm.inputs.onReplyClicked(comment, true)

        assertEquals(startThreadActivity.value.second, project)
        assertEquals(startThreadActivity.value.first.first, comment)
        assertTrue(startThreadActivity.value.first.second)
    }

    @Test
    fun testComments_UpdateCommentStateAfterPost() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .build()

        val comment1 = CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(1).body("comment1").build()
        val comment2 = CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(2).body("comment2").build()
        val newPostedComment = CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(3).body("comment3").build()

        val commentEnvelope = CommentEnvelopeFactory.commentsEnvelope().toBuilder()
            .comments(listOf(comment1, comment2))
            .build()

        val testScheduler = TestScheduler()
        val env = environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.just(commentEnvelope)
            }
        })
            .currentUser(MockCurrentUser(currentUser))
            .scheduler(testScheduler)
            .build()

        val commentCardData1 = CommentCardData.builder()
            .comment(comment1)
            .commentCardState(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus)
            .build()
        val commentCardData2 = CommentCardData.builder()
            .comment(comment2)
            .commentCardState(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus)
            .build()
        val commentCardData3 = CommentCardData.builder()
            .comment(newPostedComment)
            .commentCardState(CommentCardStatus.TRYING_TO_POST.commentCardStatus)
            .build()
        val commentCardData3Updated = CommentCardData.builder()
            .comment(newPostedComment)
            .commentCardState(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus)
            .build()

        val vm = CommentsViewModel.ViewModel(env)
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))
        vm.outputs.commentsList().subscribe(commentsList)

        commentsList.assertValueCount(1)
        vm.outputs.commentsList().take(0).subscribe {
            val newList = it
            assertTrue(newList.size == 2)
            assertTrue(newList[0].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData2.commentCardState)
        }

        // - New posted comment with status "TRYING_TO_POST"
        vm.inputs.insertNewCommentToList(newPostedComment.body(), DateTime.now())
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        commentsList.assertValueCount(2)
        vm.outputs.commentsList().take(1).subscribe {
            val newList = it
            assertTrue(newList.size == 3)
            assertTrue(newList[0].comment?.body() == commentCardData3.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData3.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[2].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[2].commentCardState == commentCardData2.commentCardState)
        }

        // - Check the status of the newly posted comment has been updated to "COMMENT_FOR_LOGIN_BACKED_USERS"
        vm.inputs.refreshComment(newPostedComment)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        commentsList.assertValueCount(3)
        vm.outputs.commentsList().take(2).subscribe {
            val newList = it
            assertTrue(newList.size == 3)
            assertTrue(newList[0].comment?.body() == commentCardData3Updated.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData3Updated.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[2].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[2].commentCardState == commentCardData2.commentCardState)
        }
    }
}
