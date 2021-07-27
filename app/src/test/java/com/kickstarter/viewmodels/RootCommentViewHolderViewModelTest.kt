package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import com.kickstarter.models.Comment
import com.kickstarter.ui.views.CommentCardBadge
import org.junit.Test
import rx.observers.TestSubscriber
import type.CommentBadge

class RootCommentViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: RootCommentViewHolderViewModel.ViewModel

    private val bindRootComment = TestSubscriber<CommentCardData>()
    private val showCanceledPledgeRootCommentClicked = TestSubscriber<CommentCardStatus>()
    private val authorBadge = TestSubscriber<CommentCardBadge>()

    private fun setupEnvironment(environment: Environment) {
        this.vm = RootCommentViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.bindRootComment().subscribe(bindRootComment)
        this.vm.outputs.authorBadge().subscribe(authorBadge)
        this.vm.outputs.showCanceledPledgeRootComment().subscribe(showCanceledPledgeRootCommentClicked)
    }

    @Test
    fun bindRootCommentTest() {
        setupEnvironment(environment())
        val comment = CommentFactory.comment()
        this.vm.inputs.configureWith(comment)
        this.bindRootComment.assertValue(comment)
    }

    @Test
    fun bindCanceledRootCommentTest() {
        setupEnvironment(environment())
        val currentUser = UserFactory.user()
            .toBuilder()
            .id(1)
            .build()

        val comment = CommentFactory.commentWithCanceledPledgeAuthor(currentUser).toBuilder().id(1).body("comment1").build()
        val commentCardData1 = CommentCardData.builder()
            .comment(comment)
            .commentCardState(CommentCardStatus.CANCELED_PLEDGE_MESSAGE.commentCardStatus)
            .build()

        this.vm.inputs.configureWith(commentCardData1)

        vm.outputs.bindRootComment().take(0).subscribe {
            assertTrue(it.comment?.body() == commentCardData1.comment?.body())
            assertTrue(it.commentCardState == commentCardData1.commentCardState)
        }

        this.vm.inputs.onShowCanceledPledgeRootCommentClicked()

        this.showCanceledPledgeRootCommentClicked.assertValue(CommentCardStatus.CANCELED_PLEDGE_COMMENT)
    }

    @Test
    fun commentBadge_whenYou_shouldEmitYou() {
        val authorBadges = listOf<String>(CommentBadge.SUPERBACKER.rawValue())
        val author = UserFactory.user().toBuilder().id(1).build()
        val currentUser = UserFactory.user().toBuilder().id(1).build()
        val environment = environment().toBuilder()
            .currentUser(MockCurrentUser(currentUser))
            .build()
        setupEnvironment(environment)

        val comment = CommentFactory.commentFromCurrentUser(author, authorBadges)

        this.vm.inputs.configureWith(comment)

        this.authorBadge.assertValue(CommentCardBadge.YOU)
    }

    @Test
    fun commentBadge_whenSuperBacker_shouldEmitSuperbacker() {
        val currentUser = UserFactory.user().toBuilder().id(1).build()
        val environment = environment().toBuilder()
            .currentUser(MockCurrentUser(currentUser))
            .build()

        setupEnvironment(environment)
        val authorBadges = listOf<String>(CommentBadge.SUPERBACKER.rawValue())
        val author = UserFactory.user().toBuilder().id(2).build()
        val comment = CommentFactory.commentFromCurrentUser(author, authorBadges)

        this.vm.inputs.configureWith(comment)

        this.authorBadge.assertValue(CommentCardBadge.SUPERBACKER)
    }

    @Test
    fun commentBadge_whenCollaborator_shouldEmitCollaborator() {
        val currentUser = UserFactory.user().toBuilder().id(1).build()
        val environment = environment().toBuilder()
            .currentUser(MockCurrentUser(currentUser))
            .build()

        setupEnvironment(environment)
        val authorBadges = listOf<String>(CommentBadge.SUPERBACKER.rawValue(), CommentBadge.COLLABORATOR.rawValue())
        val author = UserFactory.user().toBuilder().id(2).build()
        val comment = CommentFactory.commentFromCurrentUser(author, authorBadges)
        val commentCardData = CommentCardData.builder()
            .comment(comment)
            .commentCardState(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus)
            .build()

        this.vm.inputs.configureWith(commentCardData)

        this.authorBadge.assertValue(CommentCardBadge.COLLABORATOR)
    }

    @Test
    fun commentBadge_whenNoBadge_shouldEmitNoBadge() {
        val currentUser = UserFactory.user().toBuilder().id(1).build()
        val environment = environment().toBuilder()
            .currentUser(MockCurrentUser(currentUser))
            .build()

        setupEnvironment(environment)
        val authorBadges = listOf<String>()
        val author = UserFactory.user().toBuilder().id(2).build()
        val comment = CommentFactory.commentFromCurrentUser(author, authorBadges)
        val commentCardData = CommentCardData.builder()
            .comment(comment)
            .commentCardState(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus)
            .build()

        this.vm.inputs.configureWith(commentCardData)

        this.authorBadge.assertValue(CommentCardBadge.NO_BADGE)
    }

    @Test
    fun commentBadge_whenNotLoggedInAndCommentIsFromCreator_shouldEmitCreator() {
        setupEnvironment(environment())
        val authorBadges = listOf<String>(CommentBadge.SUPERBACKER.rawValue(), CommentBadge.CREATOR.rawValue())
        val author = UserFactory.user().toBuilder().id(2).build()
        val comment = CommentFactory.commentFromCurrentUser(author, authorBadges)
        val commentCardData = CommentCardData.builder()
            .comment(comment)
            .commentCardState(CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS.commentCardStatus)
            .build()

        this.vm.inputs.configureWith(commentCardData)

        this.authorBadge.assertValue(CommentCardBadge.CREATOR)
    }
}
