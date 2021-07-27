package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.ui.data.CommentCardData
import com.kickstarter.ui.views.CommentCardStatus
import org.junit.Test
import rx.observers.TestSubscriber

class RootCommentViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: RootCommentViewHolderViewModel.ViewModel

    private val bindRootComment = TestSubscriber<CommentCardData>()
    private val showCanceledPledgeRootCommentClicked = TestSubscriber<CommentCardStatus>()

    private fun setupEnvironment() {
        this.vm = RootCommentViewHolderViewModel.ViewModel(environment())

        this.vm.outputs.bindRootComment().subscribe(bindRootComment)
        this.vm.outputs.showCanceledPledgeRootComment().subscribe(showCanceledPledgeRootCommentClicked)
    }

    @Test
    fun bindCanceledRootCommentTest() {
        setupEnvironment()
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
}
