package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.viewmodels.CreatorBioViewModel.CreatorBioViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class CreatorBioViewModelTest : KSRobolectricTestCase() {
    private val disposables = CompositeDisposable()
    private lateinit var vm: CreatorBioViewModel

    private val messageIconIsGone = TestSubscriber<Boolean>()
    private val startComposeMessageActivity = TestSubscriber<Project>()
    private val startMessagesActivity = TestSubscriber<Project>()
    private val url = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment, project: Project? = ProjectFactory.project()) {
        // Configure the view model with a project and url intent.
        val intent =
            Intent().putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.URL, "http://www.project.com/creator-bio")

        this.vm = CreatorBioViewModel.Factory(environment, intent).create(CreatorBioViewModel::class.java)

        this.vm.outputs.messageIconIsGone().subscribe { this.messageIconIsGone.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.startComposeMessageActivity().subscribe { this.startComposeMessageActivity.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.startMessagesActivity().subscribe { this.startMessagesActivity.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.url().subscribe { this.url.onNext(it) }.addToDisposable(disposables)
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }

    @Test
    fun testMessageIconIsGone_WhenUserIsLoggedOut() {
        setUpEnvironment(environment())

        this.messageIconIsGone.assertValue(true)
    }

    @Test
    fun testGoToComposeMessageActivity_WhenLoggedInUserIsNotBacker() {
        setUpEnvironment(environment().toBuilder().currentUserV2(MockCurrentUserV2(UserFactory.user())).build())

        this.messageIconIsGone.assertValue(false)
        this.vm.inputs.messageButtonClicked()
        this.startComposeMessageActivity.assertValueCount(1)
        this.startMessagesActivity.assertNoValues()
    }

    @Test
    fun testGoToMessagesActivity_WhenLoggedInUserIsABacker() {
        val project = ProjectFactory.project().toBuilder().isBacking(true).build()
        setUpEnvironment(environment().toBuilder().currentUserV2(MockCurrentUserV2(UserFactory.user())).build(), project)

        this.messageIconIsGone.assertValue(false)
        this.vm.inputs.messageButtonClicked()
        this.startMessagesActivity.assertValueCount(1)
        this.startComposeMessageActivity.assertNoValues()
    }

    @Test
    fun testMessageIconIsGone_WhenLoggedInUserIsCreatorOfProject() {
        val creator = UserFactory.creator()
        val project = ProjectFactory.project().toBuilder().creator(creator).build()
        setUpEnvironment(environment().toBuilder().currentUserV2(MockCurrentUserV2(creator)).build(), project)

        this.messageIconIsGone.assertValue(true)
    }

    @Test
    fun testUrl() {
        setUpEnvironment(environment())

        this.url.assertValue("http://www.project.com/creator-bio")
    }
}
