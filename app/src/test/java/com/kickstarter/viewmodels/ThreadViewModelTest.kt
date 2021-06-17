package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.AvatarFactory
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Comment
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.views.CommentComposerStatus
import org.junit.Test
import rx.observers.TestSubscriber

class ThreadViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ThreadViewModel.ViewModel
    private val getComment = TestSubscriber<Comment>()
    private val focusCompose = TestSubscriber<Boolean>()

    private val replyComposerStatus = TestSubscriber<CommentComposerStatus>()
    private val showReplyComposer = TestSubscriber<Boolean>()

    private fun setUpEnvironment() {
        setUpEnvironment(environment())
    }

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ThreadViewModel.ViewModel(environment)
        this.vm.getRootComment().subscribe(getComment)
        this.vm.shouldFocusOnCompose().subscribe(focusCompose)
    }

    @Test
    fun testGetRootComment() {
        setUpEnvironment()

        val comment = CommentFactory.comment(avatar = AvatarFactory.avatar())

        this.vm.intent(Intent().putExtra(IntentKey.COMMENT, comment))
        getComment.assertValue(comment)

        this.vm.intent(Intent().putExtra("Some other Key", comment))
        getComment.assertValue(comment)
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

        val vm = ThreadViewModel.ViewModel(
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
    fun testThreadViewModel_whenUserLoggedInAndBacking_shouldShowEnabledComposer() {
        val vm = ThreadViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build()
        )
        vm.outputs.replyComposerStatus().subscribe(replyComposerStatus)
        vm.outputs.showReplyComposer().subscribe(showReplyComposer)

        // Start the view model with a backed project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.backedProject()))

        // The comment composer should be shown to backer and enabled to write comments
        replyComposerStatus.assertValue(CommentComposerStatus.ENABLED)
        showReplyComposer.assertValues(true, true)
    }

    @Test
    fun testThreadViewModel_whenUserIsLoggedOut_composerShouldBeGone() {
        val vm = ThreadViewModel.ViewModel(environment().toBuilder().build())

        vm.outputs.replyComposerStatus().subscribe(replyComposerStatus)
        vm.outputs.showReplyComposer().subscribe(showReplyComposer)

        // Start the view model with a backed project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        // The comment composer should be hidden and disabled to write comments as no user logged-in
        showReplyComposer.assertValue(false)
        replyComposerStatus.assertValue(CommentComposerStatus.GONE)
    }

    @Test
    fun testThreadViewModel_whenUserIsLoggedInNotBacking_shouldShowDisabledComposer() {
        val vm = ThreadViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build()
        )
        vm.outputs.replyComposerStatus().subscribe(replyComposerStatus)
        vm.outputs.showReplyComposer().subscribe(showReplyComposer)

        // Start the view model with a backed project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, ProjectFactory.project()))

        // The comment composer should show but in disabled state
        replyComposerStatus.assertValue(CommentComposerStatus.DISABLED)
        showReplyComposer.assertValues(true, true)
    }

    @Test
    fun testThreadViewModel_whenUserIsCreator_shouldShowEnabledComposer() {
        val currentUser = UserFactory.user().toBuilder().id(1234).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .creator(currentUser)
            .isBacking(false)
            .build()
        val vm = ThreadViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )

        vm.outputs.replyComposerStatus().subscribe(replyComposerStatus)
        vm.outputs.showReplyComposer().subscribe(showReplyComposer)

        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // The comment composer enabled to write comments for creator
        showReplyComposer.assertValues(true, true)
        replyComposerStatus.assertValues(CommentComposerStatus.ENABLED)
    }

    @Test
    fun testThreadViewModel_enableCommentComposer_notBackingNotCreator() {
        val creator = UserFactory.creator().toBuilder().id(222).build()
        val currentUser = UserFactory.user().toBuilder().id(111).build()
        val project = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .isBacking(false)
            .build()
        val vm = ThreadViewModel.ViewModel(
            environment().toBuilder().currentUser(MockCurrentUser(currentUser)).build()
        )

        vm.outputs.replyComposerStatus().subscribe(replyComposerStatus)
        vm.outputs.showReplyComposer().subscribe(showReplyComposer)

        // Start the view model with a project.
        vm.intent(Intent().putExtra(IntentKey.PROJECT, project))

        // Comment composer should be disabled and shown the disabled msg if not backing and not creator.
        showReplyComposer.assertValues(true, true)
        replyComposerStatus.assertValue(CommentComposerStatus.DISABLED)
    }
}
