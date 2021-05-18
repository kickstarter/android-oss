package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.ui.IntentKey
import org.junit.Test
import rx.observers.TestSubscriber

class CommentsViewModelTest : KSRobolectricTestCase() {
    private val enableCommentComposer = TestSubscriber<Boolean>()
    private val showCommentComposer = TestSubscriber<Void>()

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
}
