package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.reduceProjectPayload
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.CommentCardDataFactory
import com.kickstarter.mock.factories.CommentEnvelopeFactory
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Comment
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.ui.views.CommentComposerStatus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subscribers.TestSubscriber
import org.joda.time.DateTime
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

class ThreadViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ThreadViewModel.ThreadViewModel
    private val getComment = TestSubscriber<CommentCardData>()
    private val focusCompose = TestSubscriber<Boolean>()
    private val onReplies = TestSubscriber<Pair<List<CommentCardData>, Boolean>>()

    private val replyComposerStatus = TestSubscriber<CommentComposerStatus>()
    private val showReplyComposer = TestSubscriber<Boolean>()
    private val loadMoreReplies = TestSubscriber<Unit>()
    private val openCommentGuideLines = TestSubscriber<Unit>()
    private val refresh = TestSubscriber<Unit>()
    private val hasPendingComments = TestSubscriber<Boolean>()
    private val closeThreadActivity = TestSubscriber<Unit>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment() {
        setUpEnvironment(environment())
    }

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ThreadViewModel.ThreadViewModel(environment)
        this.vm.getRootComment().subscribe { getComment.onNext(it) }.addToDisposable(disposables)
        this.vm.shouldFocusOnCompose().subscribe { focusCompose.onNext(it) }
            .addToDisposable(disposables)
        this.vm.onCommentReplies().subscribe { onReplies.onNext(it) }.addToDisposable(disposables)
        this.vm.loadMoreReplies().subscribe { loadMoreReplies.onNext(it) }
            .addToDisposable(disposables)
        this.vm.refresh().subscribe { refresh.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testGetRootComment() {
        setUpEnvironment()

        val project = ProjectFactory.project().reduceProjectPayload()
        val commentCardData = CommentCardDataFactory.commentCardData().toBuilder().project(project).build()

        this.vm.intent(Intent().putExtra(IntentKey.COMMENT_CARD_DATA, commentCardData))
        getComment.assertValue(commentCardData)

        this.vm.intent(Intent().putExtra("Some other Key", commentCardData))
        getComment.assertValue(commentCardData)
    }

    @Test
    fun testShouldFocusCompose() {
        setUpEnvironment()

        this.vm.intent(Intent().putExtra(IntentKey.REPLY_EXPAND, false))
        focusCompose.assertValue(false)

        this.vm.intent(Intent().putExtra("Some other Key", false))
        focusCompose.assertValues(false)

        this.vm.intent(Intent().putExtra(IntentKey.REPLY_EXPAND, true))
        focusCompose.assertValues(false, true)
    }

    @Test
    fun testThreadViewModel_setCurrentUserAvatar() {
        val userAvatar = AvatarFactory.avatar()
        val currentUser = UserFactory.user().toBuilder().id(111).avatar(
            userAvatar
        ).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .isBacking(false)
            .build()

        val vm = ThreadViewModel.ThreadViewModel(
            environment().toBuilder().currentUserV2(MockCurrentUserV2(currentUser)).build()
        )
        val currentUserAvatar = TestSubscriber<String?>()
        vm.outputs.currentUserAvatar().subscribe { currentUserAvatar.onNext(it) }
            .addToDisposable(disposables)
        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // set user avatar with small url
        currentUserAvatar.assertValue(userAvatar.small())
    }

    @Test
    fun testThreadViewModel_whenUserLoggedInAndBacking_shouldShowEnabledComposer() {
        val vm = ThreadViewModel.ThreadViewModel(
            environment().toBuilder().currentUserV2(MockCurrentUserV2(UserFactory.user())).build()
        )
        vm.outputs.replyComposerStatus().subscribe { replyComposerStatus.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.showReplyComposer().subscribe { showReplyComposer.onNext(it) }
            .addToDisposable(disposables)

        val project = ProjectFactory.backedProject().reduceProjectPayload()
        // Start the view model with a backed project and comment.
        vm.intent(
            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardDataBacked().toBuilder().project(project).build()
            )
        )

        // The comment composer should be shown to backer and enabled to write comments
        replyComposerStatus.assertValue(CommentComposerStatus.ENABLED)
        showReplyComposer.assertValues(true, true)
    }

    @Test
    fun testThreadViewModel_whenUserIsLoggedOut_composerShouldBeGone() {
        val vm = ThreadViewModel.ThreadViewModel(environment().toBuilder().build())

        vm.outputs.replyComposerStatus().subscribe { replyComposerStatus.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.showReplyComposer().subscribe { showReplyComposer.onNext(it) }
            .addToDisposable(disposables)
        // Start the view model with a backed project and comment.
        val project = ProjectFactory.backedProject().reduceProjectPayload()
        vm.intent(

            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardData().toBuilder().project(project).build()
            )
        )

        // The comment composer should be hidden and disabled to write comments as no user logged-in
        showReplyComposer.assertValue(false)
        replyComposerStatus.assertValue(CommentComposerStatus.GONE)
    }

    @Test
    fun testThreadViewModel_whenUserIsLoggedInNotBacking_shouldShowDisabledComposer() {
        val vm = ThreadViewModel.ThreadViewModel(
            environment().toBuilder().currentUserV2(MockCurrentUserV2(UserFactory.user())).build()
        )
        vm.outputs.replyComposerStatus().subscribe { replyComposerStatus.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.showReplyComposer().subscribe { showReplyComposer.onNext(it) }
            .addToDisposable(disposables)
        // Start the view model with a project and comment.
        val project = ProjectFactory.project().reduceProjectPayload()
        vm.intent(
            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardData().toBuilder().project(project).build()
            )
        )

        // The comment composer should show but in disabled state
        replyComposerStatus.assertValue(CommentComposerStatus.DISABLED)
        showReplyComposer.assertValues(true, true)
    }

    @Test
    fun testThreadViewModel_whenUserIsCreator_shouldShowEnabledComposer() {
        val currentUser = UserFactory.user().toBuilder().id(1234).build()
        val vm = ThreadViewModel.ThreadViewModel(
            environment().toBuilder().currentUserV2(MockCurrentUserV2(currentUser)).build()
        )

        vm.outputs.replyComposerStatus().subscribe { replyComposerStatus.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.showReplyComposer().subscribe { showReplyComposer.onNext(it) }
            .addToDisposable(disposables)

        // Start the view model with a backed project and comment.

        val project = ProjectFactory.backedProject().reduceProjectPayload()
        vm.intent(
            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardDataBacked().toBuilder().project(project).build()
            )
        )

        // The comment composer enabled to write comments for creator
        showReplyComposer.assertValues(true, true)
        replyComposerStatus.assertValues(CommentComposerStatus.ENABLED)
    }

    @Test
    fun testThreadViewModel_enableCommentComposer_notBackingNotCreator() {
        val currentUser = UserFactory.user().toBuilder().id(111).build()
        val vm = ThreadViewModel.ThreadViewModel(
            environment().toBuilder().currentUserV2(MockCurrentUserV2(currentUser)).build()
        )

        vm.outputs.replyComposerStatus().subscribe { replyComposerStatus.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.showReplyComposer().subscribe { showReplyComposer.onNext(it) }
            .addToDisposable(disposables)

        // Start the view model with a backed project and comment.
        val project = ProjectFactory.project().reduceProjectPayload()
        vm.intent(
            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardData().toBuilder().project(project).build()
            )
        )

        // Comment composer should be disabled and shown the disabled msg if not backing and not creator.
        showReplyComposer.assertValues(true, true)
        replyComposerStatus.assertValue(CommentComposerStatus.DISABLED)
    }

    @Test
    fun testLoadCommentReplies_Successful() {
        val createdAt = DateTime.now()
        val env = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getRepliesForComment(
                    comment: Comment,
                    cursor: String?,
                    pageSize: Int
                ): Observable<CommentEnvelope> {
                    return Observable.just(CommentEnvelopeFactory.repliesCommentsEnvelope(createdAt = createdAt))
                }
            })
            .build()

        setUpEnvironment(env)

        // Start the view model with a backed project and comment.
        val project = ProjectFactory.project().reduceProjectPayload()
        vm.intent(
            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardData().toBuilder().project(project).build()
            )
        )

        this.onReplies.assertValueCount(2)
    }

    @Test
    fun testLoadCommentReplies_Error() {
        val env = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getRepliesForComment(
                    comment: Comment,
                    cursor: String?,
                    pageSize: Int
                ): Observable<CommentEnvelope> {
                    return Observable.error(IOException())
                }
            })
            .build()

        setUpEnvironment(env)

        // Start the view model with a backed project and comment.

        val project = ProjectFactory.project().reduceProjectPayload()
        vm.intent(
            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardData().toBuilder().project(project).build()
            )
        )

        this.onReplies.assertValueCount(0)

        vm.reloadRepliesPage()
        this.refresh.assertValueCount(1)
    }

    @Test
    fun testLoadCommentReplies_pagination() {
        val createdAt = DateTime.now()
        val replies =
            CommentEnvelopeFactory.repliesCommentsEnvelopeHasPrevious(createdAt = createdAt)
        val env = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getRepliesForComment(
                    comment: Comment,
                    cursor: String?,
                    pageSize: Int
                ): Observable<CommentEnvelope> {
                    return Observable.just(replies)
                }
            })
            .build()

        setUpEnvironment(env)

        val onReplies = BehaviorSubject.create<Pair<List<CommentCardData>, Boolean>>()
        this.vm.onCommentReplies().subscribe { onReplies.onNext(it) }.addToDisposable(disposables)

        // Start the view model with a backed project and comment.
        val project = ProjectFactory.project().reduceProjectPayload()
        vm.intent(
            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardData().toBuilder().project(project).build()
            )
        )

        val onRepliesResult = onReplies.value

        assertEquals(true, onRepliesResult?.second)
        assertEquals(replies.comments?.size, onRepliesResult?.first?.size)
        assertEquals(true, onRepliesResult?.second)

        vm.inputs.reloadRepliesPage()

        this.loadMoreReplies.assertValueCount(1)
    }

    @Test
    fun testThreadsViewModel_openCommentGuidelinesLink() {
        setUpEnvironment()
        val project = ProjectFactory.project().reduceProjectPayload()
        vm.intent(
            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardData().toBuilder().project(project).build()
            )
        )

        // Start the view model with an update.
        vm.outputs.showCommentGuideLinesLink().subscribe { openCommentGuideLines.onNext(it) }
            .addToDisposable(disposables)

        // post a comment
        vm.inputs.onShowGuideLinesLinkClicked()
        openCommentGuideLines.assertValueCount(1)
    }

    /*
    * test Pagination
    */
    @Test
    fun testThreadViewModel_ProjectLoadingMore_AndInsertNewReplay() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .avatar(AvatarFactory.avatar())
            .build()

        var firstCall = true

        val testScheduler = TestScheduler()

        val comment1 =
            CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(1).body("comment1")
                .build()
        val comment2 =
            CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(2).body("comment2")
                .build()
        val newPostedComment =
            CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(3).body("comment3")
                .build()

        val commentEnvelope = CommentEnvelopeFactory.commentsEnvelope().toBuilder()
            .comments(listOf(comment1, comment2))
            .build()

        val env = environment().toBuilder().currentUserV2(MockCurrentUserV2(currentUser))
            .apolloClientV2(object : MockApolloClientV2() {

                override fun getRepliesForComment(
                    comment: Comment,
                    cursor: String?,
                    pageSize: Int
                ): Observable<CommentEnvelope> {
                    return if (firstCall)
                        Observable.just(commentEnvelope)
                    else
                        Observable.just(CommentEnvelopeFactory.emptyCommentsEnvelope())
                }
            })
            .schedulerV2(testScheduler).build()

        val onReplies = BehaviorSubject.create<Pair<List<CommentCardData>, Boolean>>()
        val vm = ThreadViewModel.ThreadViewModel(env)
        val project = ProjectFactory.project().reduceProjectPayload()
        vm.intent(
            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardData().toBuilder().project(project).build()
            )
        )
        vm.outputs.onCommentReplies().subscribe { onReplies.onNext(it) }
            .addToDisposable(disposables)

        assertEquals(2, onReplies.value?.first?.size)

        // post a comment
        vm.inputs.insertNewReplyToList(newPostedComment.body(), DateTime.now())

        assertEquals(3, onReplies.value?.first?.size)
        assertEquals(1, vm.newlyPostedRepliesList.size)

        firstCall = false
        // get the next page which is end of page
        vm.inputs.nextPage()

        vm.outputs.onCommentReplies().take(0).subscribe {
            assertEquals(2, it.first.size)
            assertEquals(CommentFactory.comment(), it.first.last().comment)
        }.addToDisposable(disposables)

        assertEquals(3, onReplies.value?.first?.size)
        assertEquals(1, vm.newlyPostedRepliesList.size)
    }

    @Test
    fun testComments_UpdateCommentStateAfterPost() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .build()

        val comment1 =
            CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(1).body("comment1")
                .build()
        val comment2 =
            CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(2).body("comment2")
                .build()
        val newPostedComment =
            CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(3).body("comment3")
                .build()

        val commentEnvelope = CommentEnvelopeFactory.commentsEnvelope().toBuilder()
            .comments(listOf(comment1, comment2))
            .build()

        val testScheduler = TestScheduler()

        val env = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getRepliesForComment(
                    comment: Comment,
                    cursor: String?,
                    pageSize: Int
                ): Observable<CommentEnvelope> {
                    return Observable.just(commentEnvelope)
                }
            })
            .currentUserV2(MockCurrentUserV2(currentUser))
            .schedulerV2(testScheduler)
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

        val vm = ThreadViewModel.ThreadViewModel(env)
        // Start the view model with a backed project and comment.
        val project = ProjectFactory.project().reduceProjectPayload()
        vm.intent(
            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardData().toBuilder().project(project).build()
            )
        )
        vm.outputs.onCommentReplies().subscribe { onReplies.onNext(it) }
            .addToDisposable(disposables)

        onReplies.assertValueCount(1)
        vm.outputs.onCommentReplies().take(0).subscribe {
            val newList = it.first
            assertTrue(newList.size == 2)
            assertTrue(newList[0].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)

        // - New posted comment with status "TRYING_TO_POST"
        vm.inputs.insertNewReplyToList(newPostedComment.body(), DateTime.now())
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        onReplies.assertValueCount(2)
        assertEquals(1, vm.newlyPostedRepliesList.size)
        assertEquals(
            CommentCardStatus.TRYING_TO_POST.commentCardStatus,
            vm.newlyPostedRepliesList[0].commentCardState
        )

        vm.outputs.onCommentReplies().take(0).subscribe {
            val newList = it.first
            assertTrue(newList.size == 3)
            assertTrue(newList[0].comment?.body() == commentCardData3.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData3.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[2].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[2].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)

        // - Check the status of the newly posted comment has been updated to "COMMENT_FOR_LOGIN_BACKED_USERS"
        vm.inputs.refreshCommentCardInCaseSuccessPosted(newPostedComment, 0)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        onReplies.assertValueCount(3)
        vm.outputs.onCommentReplies().take(0).subscribe {
            val newList = it.first
            assertTrue(newList.size == 3)
            assertTrue(newList[0].comment?.body() == commentCardData3Updated.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData3Updated.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[2].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[2].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
        assertEquals(1, vm.newlyPostedRepliesList.size)
        assertEquals(
            CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus,
            vm.newlyPostedRepliesList[0].commentCardState
        )
    }

    @Test
    fun testComments_UpdateCommentStateAfterPostFailed() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .build()

        val comment1 =
            CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(1).body("comment1")
                .build()
        val comment2 =
            CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(2).body("comment2")
                .build()
        val newPostedComment =
            CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(3).body("comment3")
                .build()

        val commentEnvelope = CommentEnvelopeFactory.commentsEnvelope().toBuilder()
            .comments(listOf(comment1, comment2))
            .build()

        val testScheduler = TestScheduler()

        val env = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getRepliesForComment(
                    comment: Comment,
                    cursor: String?,
                    pageSize: Int
                ): Observable<CommentEnvelope> {
                    return Observable.just(commentEnvelope)
                }
            })
            .currentUserV2(MockCurrentUserV2(currentUser))
            .schedulerV2(testScheduler)
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

        val commentCardData3Failed = CommentCardData.builder()
            .comment(newPostedComment)
            .commentCardState(CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus)
            .build()

        val commentCardData3Updated = CommentCardData.builder()
            .comment(newPostedComment)
            .commentCardState(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus)
            .build()

        val vm = ThreadViewModel.ThreadViewModel(env)
        // Start the view model with a backed project and comment.
        val project = ProjectFactory.project().reduceProjectPayload()
        vm.intent(
            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardData().toBuilder().project(project).build()
            )
        )
        vm.outputs.onCommentReplies().subscribe { onReplies.onNext(it) }
            .addToDisposable(disposables)

        onReplies.assertValueCount(1)
        vm.outputs.onCommentReplies().take(0).subscribe {
            val newList = it.first
            assertTrue(newList.size == 2)
            assertTrue(newList[0].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)

        // - New posted comment with status "TRYING_TO_POST"
        vm.inputs.insertNewReplyToList(newPostedComment.body(), DateTime.now())
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        onReplies.assertValueCount(2)
        assertEquals(1, vm.newlyPostedRepliesList.size)

        vm.outputs.onCommentReplies().take(0).subscribe {
            val newList = it.first
            assertTrue(newList.size == 3)
            assertTrue(newList[0].comment?.body() == commentCardData3.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData3.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[2].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[2].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)

        // - Check the status of the newly posted comment has been updated to "FAILED_TO_SEND_COMMENT"
        vm.inputs.refreshCommentCardInCaseFailedPosted(newPostedComment, 0)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        assertEquals(1, vm.newlyPostedRepliesList.size)
        assertEquals(
            CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus,
            vm.newlyPostedRepliesList[0].commentCardState
        )
        onReplies.assertValueCount(3)
        assertEquals(1, vm.newlyPostedRepliesList.size)
        vm.outputs.onCommentReplies().take(0).subscribe {
            val newList = it.first
            assertTrue(newList.size == 3)
            assertTrue(newList[0].comment?.body() == commentCardData3Failed.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData3Failed.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[2].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[2].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)

        // - Check the status of the newly posted comment has been updated to "COMMENT_FOR_LOGIN_BACKED_USERS"
        vm.inputs.refreshCommentCardInCaseSuccessPosted(newPostedComment, 0)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        vm.outputs.onCommentReplies().take(0).subscribe {
            val newList = it.first
            assertTrue(newList.size == 3)
            assertTrue(newList[0].comment?.body() == commentCardData3Updated.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData3Updated.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[2].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[2].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)

        assertEquals(1, vm.newlyPostedRepliesList.size)
        assertEquals(
            CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus,
            vm.newlyPostedRepliesList[0].commentCardState
        )
    }

    @Test
    fun backButtonPressed_whenEmits_shouldEmitToCloseActivityStream() {
        setUpEnvironment()
        vm.outputs.closeThreadActivity().subscribe { closeThreadActivity.onNext(it) }
            .addToDisposable(disposables)

        vm.inputs.backPressed()
        closeThreadActivity.assertValueCount(1)
    }

    @Test
    fun testReplies_BackWithPendingComment() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .build()

        val comment1 =
            CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(1).body("comment1")
                .build()
        val comment2 =
            CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(2).body("comment2")
                .build()
        val newPostedComment =
            CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(3).body("comment3")
                .build()

        val commentEnvelope = CommentEnvelopeFactory.commentsEnvelope().toBuilder()
            .comments(listOf(comment1, comment2))
            .build()

        val testScheduler = TestScheduler()

        val env = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getRepliesForComment(
                    comment: Comment,
                    cursor: String?,
                    pageSize: Int
                ): Observable<CommentEnvelope> {
                    return Observable.just(commentEnvelope)
                }
            })
            .currentUserV2(MockCurrentUserV2(currentUser))
            .schedulerV2(testScheduler)
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

        val commentCardData3Failed = CommentCardData.builder()
            .comment(newPostedComment)
            .commentCardState(CommentCardStatus.FAILED_TO_SEND_COMMENT.commentCardStatus)
            .build()

        val vm = ThreadViewModel.ThreadViewModel(env)
        // Start the view model with a backed project and comment.
        val project = ProjectFactory.project().reduceProjectPayload()
        vm.intent(
            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardData().toBuilder().project(project).build()
            )
        )
        vm.outputs.onCommentReplies().subscribe { onReplies.onNext(it) }
            .addToDisposable(disposables)

        vm.outputs.onCommentReplies().subscribe { }.addToDisposable(disposables)
        vm.outputs.hasPendingComments().subscribe { hasPendingComments.onNext(it) }
            .addToDisposable(disposables)

        onReplies.assertValueCount(1)
        vm.outputs.onCommentReplies().take(0).subscribe {
            val newList = it.first
            assertTrue(newList.size == 2)
            assertTrue(newList[0].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)

        vm.inputs.checkIfThereAnyPendingComments()

        this.hasPendingComments.assertValue(false)
        // - New posted comment with status "TRYING_TO_POST"
        vm.inputs.insertNewReplyToList(newPostedComment.body(), DateTime.now())
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        onReplies.assertValueCount(2)
        assertEquals(1, vm.newlyPostedRepliesList.size)
        assertEquals(
            CommentCardStatus.TRYING_TO_POST.commentCardStatus,
            vm.newlyPostedRepliesList[0].commentCardState
        )

        vm.outputs.onCommentReplies().take(0).subscribe {
            val newList = it.first
            assertTrue(newList.size == 3)
            assertTrue(newList[0].comment?.body() == commentCardData3.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData3.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[2].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[2].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)

        vm.inputs.checkIfThereAnyPendingComments()
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        this.hasPendingComments.assertValues(false, true)

        // - Check the status of the newly posted comment
        vm.inputs.refreshCommentCardInCaseFailedPosted(newPostedComment, 0)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        onReplies.assertValueCount(3)
        vm.outputs.onCommentReplies().take(0).subscribe {
            val newList = it.first
            assertTrue(newList.size == 3)
            assertTrue(newList[0].comment?.body() == commentCardData3Failed.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData3Failed.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[2].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[2].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)

        // - Check Pull to refresh
        vm.inputs.checkIfThereAnyPendingComments()
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        this.hasPendingComments.assertValues(false, true, true)
    }

    @Test
    fun testComments_onShowCanceledPledgeComment() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .build()

        val comment1 = CommentFactory.commentWithCanceledPledgeAuthor(currentUser).toBuilder().id(1)
            .body("comment1").build()

        val commentEnvelope = CommentEnvelopeFactory.commentsEnvelope().toBuilder()
            .comments(listOf(comment1))
            .build()

        val testScheduler = TestScheduler()

        val env = environment().toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getRepliesForComment(
                    comment: Comment,
                    cursor: String?,
                    pageSize: Int
                ): Observable<CommentEnvelope> {
                    return Observable.just(commentEnvelope)
                }
            })
            .currentUserV2(MockCurrentUserV2(currentUser))
            .schedulerV2(testScheduler)
            .build()

        val commentCardData1 = CommentCardData.builder()
            .comment(comment1)
            .commentCardState(CommentCardStatus.CANCELED_PLEDGE_MESSAGE.commentCardStatus)
            .build()

        val commentCardData2 = CommentCardData.builder()
            .comment(comment1)
            .commentCardState(CommentCardStatus.CANCELED_PLEDGE_COMMENT.commentCardStatus)
            .build()

        val vm = ThreadViewModel.ThreadViewModel(env)
        // Start the view model with a backed project and comment.
        val project = ProjectFactory.project().reduceProjectPayload()
        vm.intent(
            Intent().putExtra(
                IntentKey.COMMENT_CARD_DATA,
                CommentCardDataFactory.commentCardData().toBuilder().project(project).build()
            )
        )
        vm.outputs.onCommentReplies().subscribe { onReplies.onNext(it) }
            .addToDisposable(disposables)

        onReplies.assertValueCount(1)
        vm.outputs.onCommentReplies().take(0).subscribe {
            val newList = it.first
            assertTrue(newList[0].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData1.commentCardState)
        }.addToDisposable(disposables)

        vm.inputs.onShowCanceledPledgeComment(comment1)

        vm.outputs.onCommentReplies().take(1).subscribe {
            val newList = it.first
            assertTrue(newList[0].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)
    }
}
