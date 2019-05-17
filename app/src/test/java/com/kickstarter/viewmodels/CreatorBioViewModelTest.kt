package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import org.junit.Test
import rx.observers.TestSubscriber

class CreatorBioViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: CreatorBioViewModel.ViewModel

    private val messageIconIsGone = TestSubscriber<Boolean>()
    private val startComposeMessageActivity = TestSubscriber<Project>()
    private val startMessagesActivity = TestSubscriber<Project>()
    private val url = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment, project: Project? = ProjectFactory.project()) {
        this.vm = CreatorBioViewModel.ViewModel(environment)

        this.vm.outputs.messageIconIsGone().subscribe(this.messageIconIsGone)
        this.vm.outputs.startComposeMessageActivity().subscribe(this.startComposeMessageActivity)
        this.vm.outputs.startMessagesActivity().subscribe(this.startMessagesActivity)
        this.vm.outputs.url().subscribe(this.url)

        // Configure the view model with a project and url intent.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project)
                .putExtra(IntentKey.URL, "http://www.project.com/creator-bio"))
    }

    @Test
    fun testLoggedOutUser() {
        setUpEnvironment(environment())

        this.messageIconIsGone.assertValue(true)
    }

    @Test
    fun testLoggedInUser() {
        setUpEnvironment(environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build())

        this.messageIconIsGone.assertValue(false)
        this.vm.inputs.messageButtonClicked()
        this.startComposeMessageActivity.assertValueCount(1)
        this.startMessagesActivity.assertNoValues()
    }

    @Test
    fun testLoggedInBacker() {
        val project = ProjectFactory.project().toBuilder().isBacking(true).build()
        setUpEnvironment(environment().toBuilder().currentUser(MockCurrentUser(UserFactory.user())).build(), project)

        this.messageIconIsGone.assertValue(false)
        this.vm.inputs.messageButtonClicked()
        this.startMessagesActivity.assertValueCount(1)
        this.startComposeMessageActivity.assertNoValues()
    }

    @Test
    fun testLoggedInCreatorOfProject() {
        val creator = UserFactory.creator()
        val project = ProjectFactory.project().toBuilder().creator(creator).build()
        setUpEnvironment(environment().toBuilder().currentUser(MockCurrentUser(creator)).build(), project)

        this.messageIconIsGone.assertValue(true)
    }

    @Test
    fun testUrl() {
        setUpEnvironment(environment())

        this.url.assertValue("http://www.project.com/creator-bio")
    }

    @Test
    fun testTracking() {
        setUpEnvironment(environment())

        this.koalaTest.assertValue("Modal Dialog View")
    }

}
