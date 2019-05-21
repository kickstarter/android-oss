package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.MessageThreadEnvelope
import com.kickstarter.ui.IntentKey
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class MessageCreatorViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: MessageCreatorViewModel.ViewModel

    private val creatorName = TestSubscriber<String>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val sendButtonIsEnabled = TestSubscriber<Boolean>()
    private val showMessageThread = TestSubscriber<MessageThread>()
    private val showSentError = TestSubscriber<Int>()
    private val showSentSuccess = TestSubscriber<Int>()

    private fun setUpEnvironment(environment: Environment, project: Project? = ProjectFactory.project()) {
        this.vm = MessageCreatorViewModel.ViewModel(environment)

        this.vm.outputs.creatorName().subscribe(this.creatorName)
        this.vm.outputs.progressBarIsVisible().subscribe(this.progressBarIsVisible)
        this.vm.outputs.sendButtonIsEnabled().subscribe(this.sendButtonIsEnabled)
        this.vm.outputs.showMessageThread().subscribe(this.showMessageThread)
        this.vm.outputs.showSentError().subscribe(this.showSentError)
        this.vm.outputs.showSentSuccess().subscribe(this.showSentSuccess)

        // Configure the view model with a project intent.
        this.vm.intent(Intent().putExtra(IntentKey.PROJECT, project))
    }

    @Test
    fun testSendingMessageError() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun sendMessage(project: Project, recipient: User, body: String): Observable<Long> {
                return Observable.error(Throwable("error"))
            }
        }).build())

        this.vm.inputs.messageBodyChanged("message")
        this.vm.inputs.sendButtonClicked()
        this.progressBarIsVisible.assertValues(true, false)
        this.sendButtonIsEnabled.assertValues(true, false, true)
        this.showSentError.assertValue(R.string.social_error_could_not_send_message_backer)
        this.showSentSuccess.assertNoValues()
        this.showMessageThread.assertNoValues()
    }

    @Test
    fun testSendingMessageSuccess() {
        setUpEnvironment(environment().toBuilder().apiClient(object : MockApiClient() {
            override fun fetchMessagesForThread(messageThreadId: Long): Observable<MessageThreadEnvelope> {
                return Observable.error(Throwable("error"))
            }
        }).build())

        this.vm.inputs.messageBodyChanged("message")
        this.vm.inputs.sendButtonClicked()
        this.progressBarIsVisible.assertValues(true)
        this.sendButtonIsEnabled.assertValues(true, false)
        this.showSentError.assertNoValues()
        this.showSentSuccess.assertValue(R.string.Your_message_has_been_sent)
        this.showMessageThread.assertNoValues()
    }

    @Test
    fun testInitWithCreatorName() {
        val user = UserFactory.creator().toBuilder().name("Brook Lopez").build()
        val project = ProjectFactory.project().toBuilder().creator(user).build()
        setUpEnvironment(environment(), project)

        this.creatorName.assertValue("Brook Lopez")
        this.progressBarIsVisible.assertNoValues()
        this.sendButtonIsEnabled.assertNoValues()
        this.showSentError.assertNoValues()
        this.showSentSuccess.assertNoValues()
        this.showMessageThread.assertNoValues()
    }

    @Test
    fun testSendingMessageSuccessful() {
        setUpEnvironment(environment())

        this.vm.inputs.messageBodyChanged("message")
        this.vm.inputs.sendButtonClicked()
        this.progressBarIsVisible.assertValues(true)
        this.sendButtonIsEnabled.assertValues(true, false)
        this.showSentError.assertNoValues()
        this.showSentSuccess.assertNoValues()
        this.showMessageThread.assertValueCount(1)
    }

    @Test
    fun testTracking() {
        setUpEnvironment(environment())

        this.koalaTest.assertValue("Modal Dialog View")
    }

}
