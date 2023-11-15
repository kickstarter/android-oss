package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.CommentEnvelopeFactory
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UpdateFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Comment
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.ui.views.CommentComposerStatus
import com.kickstarter.viewmodels.CommentsViewModel.CommentsViewModel
import com.kickstarter.viewmodels.CommentsViewModel.Factory
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subscribers.TestSubscriber
import org.joda.time.DateTime
import org.junit.After
import org.junit.Test
import java.lang.Exception
import java.util.concurrent.TimeUnit

class CommentsViewModelTest : KSRobolectricTestCase() {
    private val closeCommentPage = TestSubscriber<Unit>()
    private val commentsList = TestSubscriber<List<CommentCardData>?>()
    private val commentComposerStatus = TestSubscriber<CommentComposerStatus>()
    private val showCommentComposer = TestSubscriber<Boolean>()
    private val showEmptyState = TestSubscriber<Boolean>()
    private val scrollToTop = BehaviorSubject.create<Boolean>()
    private val initialLoadError = TestSubscriber.create<Throwable>()
    private val paginationError = TestSubscriber.create<Throwable>()
    private val shouldShowPaginatedCell = TestSubscriber.create<Boolean>()
    private val shouldShowInitialLoadErrorCell = TestSubscriber.create<Boolean>()
    private val openCommentGuideLines = TestSubscriber<Unit>()
    private val hasPendingComments = TestSubscriber<Pair<Boolean, Boolean>>()

    private val disposables = CompositeDisposable()

    @After
    fun cleanUp() {
        disposables.clear()
    }

    @Test
    fun testCommentsViewModel_whenUserLoggedInAndBacking_shouldShowEnabledComposer() {

        // Start the view model with a backed project.
        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.backedProject()))
        val vm = Factory(
            environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build(),
            intent
        ).create(CommentsViewModel::class.java)

        vm.outputs.commentComposerStatus().subscribe { commentComposerStatus.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showCommentComposer().subscribe { showCommentComposer.onNext(it) }.addToDisposable(disposables)

        // The comment composer should be shown to backer and enabled to write comments
        commentComposerStatus.assertValue(CommentComposerStatus.ENABLED)
        showCommentComposer.assertValues(true, true)
    }

    @Test
    fun testCommentsViewModel_whenUserIsLoggedOut_composerShouldBeGone() {
        // Start the view model with a backed project.
        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(
            environment().toBuilder().build(),
            intent
        ).create(CommentsViewModel::class.java)

        vm.outputs.commentComposerStatus().subscribe { commentComposerStatus.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showCommentComposer().subscribe { showCommentComposer.onNext(it) }.addToDisposable(disposables)

        // The comment composer should be hidden and disabled to write comments as no user logged-in
        showCommentComposer.assertValue(false)
        commentComposerStatus.assertValue(CommentComposerStatus.GONE)
    }

    @Test
    fun testCommentsViewModel_whenUserIsLoggedInNotBacking_shouldShowDisabledComposer() {
        // Start the view model with a backed project.
        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(
            environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build(),
            intent
        ).create(CommentsViewModel::class.java)

        vm.outputs.commentComposerStatus().subscribe { commentComposerStatus.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showCommentComposer().subscribe { showCommentComposer.onNext(it) }.addToDisposable(disposables)

        // The comment composer should show but in disabled state
        commentComposerStatus.assertValue(CommentComposerStatus.DISABLED)
        showCommentComposer.assertValues(true, true)
    }

    @Test
    fun testCommentsViewModel_whenUser_can_comment_shouldShowEnabledComposer() {
        val currentUser = UserFactory.user().toBuilder().id(1234).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .canComment(true)
            .build()
        val vm = Factory(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build(),
            Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(project))
        ).create(CommentsViewModel::class.java)

        vm.outputs.commentComposerStatus().subscribe { commentComposerStatus.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showCommentComposer().subscribe { showCommentComposer.onNext(it) }.addToDisposable(disposables)

        // The comment composer enabled to write comments for creator
        showCommentComposer.assertValues(true, true)
        commentComposerStatus.assertValues(CommentComposerStatus.ENABLED)
    }

    @Test
    fun testCommentsViewModel_whenUser_cant_comment_shouldShowEnabledComposer() {
        val currentUser = UserFactory.user().toBuilder().id(1234).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .canComment(false)
            .build()
        val vm = Factory(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build(),
            Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        ).create(CommentsViewModel::class.java)

        vm.outputs.commentComposerStatus().subscribe { commentComposerStatus.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showCommentComposer().subscribe { showCommentComposer.onNext(it) }.addToDisposable(disposables)

        // The comment composer enabled to write comments for creator
        showCommentComposer.assertValues(true, true)
        commentComposerStatus.assertValues(CommentComposerStatus.DISABLED)
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

        val vm = Factory(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build(),
            Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        ).create(CommentsViewModel::class.java)

        vm.outputs.commentComposerStatus().subscribe { commentComposerStatus.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showCommentComposer().subscribe { showCommentComposer.onNext(it) }.addToDisposable(disposables)

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

        val vm = Factory(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build(),
            Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        ).create(CommentsViewModel::class.java)

        val currentUserAvatar = TestSubscriber<String?>()
        vm.outputs.currentUserAvatar().subscribe { currentUserAvatar.onNext(it) }.addToDisposable(disposables)

        // set user avatar with small url
        currentUserAvatar.assertValue(userAvatar.small())
    }

    @Test
    fun testCommentsViewModel_EmptyState_FromUpdate() {
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
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

        val vm = Factory(env, Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update())).create(CommentsViewModel::class.java)
        val commentsList = TestSubscriber<List<CommentCardData>>()
        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)

        commentsList.assertNoValues()
    }

    @Test
    fun testCommentsViewModel_EmptyState_FromProject() {
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
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

        val vm = Factory(env, Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))).create(CommentsViewModel::class.java)
        val commentsList = TestSubscriber<List<CommentCardData>>()

        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)
        commentsList.assertNoValues()
    }

    @Test
    fun testCommentsViewModel_InitialLoad_Error_ForUpdate() {
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
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
        val intent = Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update())
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)
        val commentsList = TestSubscriber<List<CommentCardData>?>()

        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)

        commentsList.assertNoValues()
    }

    @Test
    fun testCommentsViewModel_InitialLoad_Error_ForProject() {
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
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

        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)
        val commentsList = TestSubscriber<List<CommentCardData>?>()

        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)

        commentsList.assertNoValues()
    }

    @Test
    fun testCommentsViewModel_ProjectCommentsEmit() {
        val commentsList = BehaviorSubject.create<List<CommentCardData>?>()
        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(environment(), intent).create(CommentsViewModel::class.java)

        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)

        // Comments should emit.
        val commentCardDataList = commentsList.value
        assertEquals(1, commentCardDataList?.size)
        assertEquals(CommentFactory.comment(), commentCardDataList?.get(0)?.comment)
    }

    /*
   * test when no comment available
   */

    @Test
    fun testCommentsViewModel_EmptyCommentState_ForUpdate() {
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {

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

        val intent = Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update())
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)
        vm.outputs.setEmptyState().subscribe { showEmptyState.onNext(it) }.addToDisposable(disposables)

        showEmptyState.assertValue(true)
    }

    @Test
    fun testCommentsViewModel_EmptyCommentState_ForProject() {
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {

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

        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)
        vm.outputs.setEmptyState().subscribe { showEmptyState.onNext(it) }.addToDisposable(disposables)

        showEmptyState.assertValue(true)
    }

    /*
     * test when comment(s) available
     */
    @Test
    fun testCommentsViewModel_CommentsAvailableState() {
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.just(CommentEnvelopeFactory.commentsEnvelope())
            }
        }).build()

        val intent = Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update())
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)
        vm.outputs.setEmptyState().subscribe { showEmptyState.onNext(it) }.addToDisposable(disposables)

        showEmptyState.assertValues(false)
    }

    @Test
    fun testCommentsViewModel_ProjectRefresh() {
        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(environment(), intent).create(CommentsViewModel::class.java)

        // Start the view model with a project.
        vm.inputs.refresh()
        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)

        // Comments should emit.
        commentsList.assertValueCount(1)
    }

    @Test
    fun testCommentsViewModel_ProjectRefresh_AndInitialLoad_withError() {
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }).build()

        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)

        vm.outputs.initialLoadCommentsError().subscribe { initialLoadError.onNext(it) }.addToDisposable(disposables)
        vm.outputs.shouldShowInitialLoadErrorUI().subscribe { shouldShowInitialLoadErrorCell.onNext(it) }.addToDisposable(disposables)

        vm.outputs.paginateCommentsError().subscribe { paginationError.onNext(it) }.addToDisposable(disposables)
        vm.outputs.shouldShowPaginationErrorUI().subscribe { shouldShowPaginatedCell.onNext(it) }.addToDisposable(disposables)

        // Start the view model with a project.
        vm.inputs.refresh()
        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)

        // Comments should emit.
        commentsList.assertValueCount(0)
        initialLoadError.assertValueCount(2)
        shouldShowInitialLoadErrorCell.assertValues(true, true)
        shouldShowPaginatedCell.assertNoValues()
    }

    /*
  * test Pagination
  */
    @Test
    fun testCommentsViewModel_ProjectLoadingMore() {
        var firstCall = true
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return if (firstCall)
                    Observable.just(CommentEnvelopeFactory.commentsEnvelope())
                else
                    Observable.just(CommentEnvelopeFactory.emptyCommentsEnvelope())
            }
        }).build()

        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)

        firstCall = false
        // get the next page which is end of page
        vm.inputs.nextPage()
        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)

        commentsList.assertValueCount(1)
    }

    /*
    * test Pagination
    */
    @Test
    fun testCommentsViewModel_ProjectLoadingMore_AndInsertNewComment() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .avatar(AvatarFactory.avatar())
            .build()

        val createdAt = DateTime.now()

        var firstCall = true

        val commentsList = BehaviorSubject.create<List<CommentCardData>>()
        val testScheduler = TestScheduler()

        val env = environment().toBuilder()
            .currentUserV2(MockCurrentUserV2(currentUser))
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                    return if (firstCall)
                        Observable.just(CommentEnvelopeFactory.commentsEnvelope())
                    else
                        Observable.just(CommentEnvelopeFactory.emptyCommentsEnvelope())
                }
            })
            .schedulerV2(testScheduler).build()

        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)

        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)

        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser)
        vm.outputs.scrollToTop().subscribe { scrollToTop.onNext(it) }.addToDisposable(disposables)

        // post a comment
        vm.inputs.insertNewCommentToList(commentCardData.comment?.body()!!, createdAt)
        assertEquals(1, vm.newlyPostedCommentsList.size)
        assertEquals(2, commentsList.value?.size)
        assertEquals(CommentFactory.comment(), commentsList.value?.last()?.comment)

        firstCall = false
        // get the next page which is end of page
        vm.inputs.nextPage()
        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)

        assertEquals(1, vm.newlyPostedCommentsList.size)
        assertEquals(2, commentsList.value?.size)

        vm.inputs.refresh()
        assertEquals(0, vm.newlyPostedCommentsList.size)
        assertEquals(1, commentsList.value?.size)
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

        val intent = Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update())
        val vm = Factory(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build(),
            intent
        ).create(CommentsViewModel::class.java)

        val commentCardData = CommentFactory.liveCommentCardData(createdAt = createdAt, currentUser = currentUser)

        val commentsList = BehaviorSubject.create<List<CommentCardData>>()
        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)
        vm.outputs.scrollToTop().subscribe { scrollToTop.onNext(it) }.addToDisposable(disposables)

        // post a comment
        vm.inputs.insertNewCommentToList(commentCardData.comment?.body()!!, createdAt)
        assertEquals(1, vm.newlyPostedCommentsList.size)
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

        val intent = Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update())
        val vm = Factory(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build(),
            intent
        ).create(CommentsViewModel::class.java)

        vm.outputs.showCommentGuideLinesLink().subscribe { openCommentGuideLines.onNext(it) }.addToDisposable(disposables)

        // post a comment
        vm.inputs.onShowGuideLinesLinkClicked()
        openCommentGuideLines.assertValueCount(1)
    }

    @Test
    fun backButtonPressed_whenEmits_shouldEmitToCloseActivityStream_FromUpdate() {
        val intent = Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update())
        val vm = Factory(environment(), intent).create(CommentsViewModel::class.java)

        vm.outputs.closeCommentsPage().subscribe { closeCommentPage.onNext(it) }.addToDisposable(disposables)

        vm.inputs.backPressed()
        closeCommentPage.assertValueCount(1)
    }

    @Test
    fun backButtonPressed_whenEmits_shouldEmitToCloseActivityStream_FromProject() {
        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(environment(), intent).create(CommentsViewModel::class.java)

        vm.outputs.closeCommentsPage().subscribe { closeCommentPage.onNext(it) }.addToDisposable(disposables)

        vm.inputs.backPressed()
        closeCommentPage.assertValueCount(1)
    }

    @Test
    fun onReplyClicked_whenEmits_shouldEmitToCloseThreadActivity_FromUpdate() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .avatar(AvatarFactory.avatar())
            .build()

        val comment1 = CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(1).body("comment1").build()

        val commentEnvelope = CommentEnvelopeFactory.commentsEnvelope().toBuilder()
            .comments(listOf(comment1))
            .build()

        val testScheduler = TestScheduler()
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.just(commentEnvelope)
            }
        })
            .currentUserV2(MockCurrentUserV2(currentUser))
            .schedulerV2(testScheduler)
            .build()

        val intent = Intent().putExtra(IntentKey.UPDATE, UpdateFactory.update())
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)

        vm.inputs.onReplyClicked(comment1, true)
        vm.outputs.startThreadActivity().take(0).subscribe {
            assertEquals(it.first.first.comment, comment1)
            assertTrue(it.first.second)
        }.addToDisposable(disposables)
    }

    @Test
    fun onReplyClicked_whenEmits_shouldEmitToCloseThreadActivity_FromProject() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .avatar(AvatarFactory.avatar())
            .build()

        val comment1 = CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(1).body("comment1").build()

        val commentEnvelope = CommentEnvelopeFactory.commentsEnvelope().toBuilder()
            .comments(listOf(comment1))
            .build()

        val testScheduler = TestScheduler()
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.just(commentEnvelope)
            }
        })
            .currentUserV2(MockCurrentUserV2(currentUser))
            .schedulerV2(testScheduler)
            .build()

        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)

        vm.inputs.onReplyClicked(comment1, true)
        vm.outputs.startThreadActivity().take(0).subscribe {
            assertEquals(it.first.first.comment, comment1)
            assertTrue(it.first.second)
        }.addToDisposable(disposables)
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
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
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

        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(env, intent = intent).create(CommentsViewModel::class.java)

        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)

        commentsList.assertValueCount(1)
        vm.outputs.commentsList().take(0).subscribe {
            val newList = it
            assertTrue(newList.size == 2)
            assertTrue(newList[0].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)

        // - New posted comment with status "TRYING_TO_POST"
        vm.inputs.insertNewCommentToList(newPostedComment.body(), DateTime.now())
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        assertEquals(1, vm.newlyPostedCommentsList.size)
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
        }.addToDisposable(disposables)

        // - Check the status of the newly posted comment has been updated to "COMMENT_FOR_LOGIN_BACKED_USERS"
        vm.inputs.refreshComment(newPostedComment, 0)
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
        }.addToDisposable(disposables)
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testComments_PullToRefreshWithPendingComment() {
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
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
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

        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)

        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)
        vm.outputs.hasPendingComments().subscribe { hasPendingComments.onNext(it) }.addToDisposable(disposables)

        commentsList.assertValueCount(1)
        vm.outputs.commentsList().take(0).subscribe {
            val newList = it
            assertTrue(newList.size == 2)
            assertTrue(newList[0].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)

        vm.inputs.checkIfThereAnyPendingComments(false)

        this.hasPendingComments.assertValue(Pair(false, false))
        // - New posted comment with status "TRYING_TO_POST"
        vm.inputs.insertNewCommentToList(newPostedComment.body(), DateTime.now())
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        assertEquals(1, vm.newlyPostedCommentsList.size)
        assertEquals(CommentCardStatus.TRYING_TO_POST.commentCardStatus, vm.newlyPostedCommentsList[0].commentCardState)
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
        }.addToDisposable(disposables)

        vm.inputs.checkIfThereAnyPendingComments(false)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        this.hasPendingComments.assertValues(Pair(false, false), Pair(true, false))

        // - Check the status of the newly posted comment
        vm.inputs.refreshComment(newPostedComment, 0)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        // - Check Pull to refresh
        vm.inputs.checkIfThereAnyPendingComments(false)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        this.hasPendingComments.assertValues(Pair(false, false), Pair(true, false), Pair(false, false))
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)

        assertEquals(1, vm.newlyPostedCommentsList.size)
        assertEquals(
            CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus,
            vm.newlyPostedCommentsList[0].commentCardState
        )

        vm.onResumeActivity()
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun testComments_BackWithPendingComment() {
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
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
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

        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)

        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)
        vm.outputs.hasPendingComments().subscribe { hasPendingComments.onNext(it) }.addToDisposable(disposables)

        commentsList.assertValueCount(1)
        vm.outputs.commentsList().take(0).subscribe {
            val newList = it
            assertTrue(newList.size == 2)
            assertTrue(newList[0].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)

        vm.inputs.checkIfThereAnyPendingComments(true)

        this.hasPendingComments.assertValue(Pair(false, true))
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
        }.addToDisposable(disposables)

        vm.inputs.checkIfThereAnyPendingComments(true)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        this.hasPendingComments.assertValues(Pair(false, true), Pair(true, true))

        // - Check the status of the newly posted comment
        vm.inputs.refreshComment(newPostedComment, 0)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        // - Check Pull to refresh
        vm.inputs.checkIfThereAnyPendingComments(true)
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        this.hasPendingComments.assertValues(Pair(false, true), Pair(true, true), Pair(false, true))
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)

        assertEquals(1, vm.newlyPostedCommentsList.size)
        assertEquals(
            CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus,
            vm.newlyPostedCommentsList[0].commentCardState
        )
    }

    @Test
    fun testComments_onShowCanceledPledgeComment() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .build()

        val comment1 = CommentFactory.commentWithCanceledPledgeAuthor(currentUser).toBuilder().id(1).body("comment1").build()

        val commentEnvelope = CommentEnvelopeFactory.commentsEnvelope().toBuilder()
            .comments(listOf(comment1))
            .build()

        val testScheduler = TestScheduler()
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
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

        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)

        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)

        commentsList.assertValueCount(0)
        vm.outputs.commentsList().take(0).subscribe {
            val newList = it
            assertTrue(newList[0].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData1.commentCardState)
        }.addToDisposable(disposables)

        vm.inputs.onShowCanceledPledgeComment(comment1)

        vm.outputs.commentsList().take(1).subscribe {
            val newList = it
            assertTrue(newList[0].comment?.body() == commentCardData2.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData2.commentCardState)
        }.addToDisposable(disposables)
    }

    @Test
    fun testCommentsViewModel_deepLink_to_ThreadActivity() {
        val commentID = "Q29tbWVudC0zMzU2MTY4Ng"
        val commentableId = "RnJlZWZvcm1Qb3N0LTM0MTQ2ODk="

        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .avatar(AvatarFactory.avatar())
            .build()

        val comment1 = CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(1).body("comment1").build()

        val testScheduler = TestScheduler()

        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
            override fun getComment(commentableId: String): Observable<Comment> {
                return Observable.just(comment1)
            }
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.just(
                    CommentEnvelopeFactory.commentsEnvelope().toBuilder().commentableId(commentableId).comments(
                        listOf(comment1)
                    ).build()
                )
            }
        }).currentUserV2(MockCurrentUserV2(currentUser))
            .schedulerV2(testScheduler)
            .build()

        val intent = Intent().apply {
            putExtra(IntentKey.COMMENT, commentID)
            putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        }
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)

        vm.outputs.startThreadActivity().take(0).subscribe {
            assertEquals(it.first.first.commentableId, commentID)
            assertFalse(it.first.second)
        }.addToDisposable(disposables)

        vm.onResumeActivity()
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName)
        vm.onResumeActivity()
        segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testCommentsViewModel_ProjectCommentsWithCancelledPledgeCommentsEmit() {
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .avatar(AvatarFactory.avatar())
            .build()

        val comment1 = CommentFactory.commentToPostWithUser(currentUser).toBuilder().id(1).body("comment1").build()

        val user2 = UserFactory.user()
            .toBuilder()
            .id(2)
            .avatar(AvatarFactory.avatar())
            .build()

        val comment2 = CommentFactory.commentToPostWithUser(user2).toBuilder().id(1).body("comment1").authorCanceledPledge(true).build()
        val comment3 = CommentFactory.commentToPostWithUser(user2).toBuilder().id(1).body("comment1").authorCanceledPledge(true).repliesCount(3).build()

        val commentCardData1 = CommentCardData.builder()
            .comment(comment1)
            .build()

        val commentCardData3 = CommentCardData.builder()
            .comment(comment3)
            .build()

        val commentEnvelope = CommentEnvelopeFactory.commentsEnvelope().toBuilder()
            .comments(listOf(comment1, comment2, comment3))
            .build()

        val testScheduler = TestScheduler()
        val env = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
            override fun getProjectComments(slug: String, cursor: String?, limit: Int): Observable<CommentEnvelope> {
                return Observable.just(commentEnvelope)
            }
        })
            .currentUserV2(MockCurrentUserV2(currentUser))
            .schedulerV2(testScheduler)
            .build()

        val intent = Intent().putExtra(IntentKey.PROJECT_DATA, ProjectDataFactory.project(ProjectFactory.project()))
        val vm = Factory(env, intent).create(CommentsViewModel::class.java)

        vm.outputs.commentsList().subscribe { commentsList.onNext(it) }.addToDisposable(disposables)

        commentsList.assertValueCount(1)
        vm.outputs.commentsList().take(0).subscribe {
            val newList = it
            assertTrue(newList.size == 2)
            assertTrue(newList[0].comment?.body() == commentCardData1.comment?.body())
            assertTrue(newList[0].commentCardState == commentCardData1.commentCardState)

            assertTrue(newList[1].comment?.body() == commentCardData3.comment?.body())
            assertTrue(newList[1].commentCardState == commentCardData3.commentCardState)
        }.addToDisposable(disposables)
    }
}
