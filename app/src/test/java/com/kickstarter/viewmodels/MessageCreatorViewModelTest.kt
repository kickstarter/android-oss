package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.MessageThreadEnvelopeFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.MessageThreadEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.viewmodels.MessageCreatorViewModel.Factory
import com.kickstarter.viewmodels.MessageCreatorViewModel.MessageCreatorViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class MessageCreatorViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: MessageCreatorViewModel

    private val creatorName = TestSubscriber<String>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val sendButtonIsEnabled = TestSubscriber<Boolean>()
    private val showMessageThread = TestSubscriber<MessageThread>()
    private val showSentError = TestSubscriber<Int>()
    private val showSentSuccess = TestSubscriber<Int>()

    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment, project: Project? = ProjectFactory.project()) {

        // Configure the view model with a project intent.
        val intent = Intent().putExtra(IntentKey.PROJECT, project)
        this.vm = Factory(environment, intent).create(MessageCreatorViewModel::class.java)

        this.vm.outputs.creatorName().subscribe { this.creatorName.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.progressBarIsVisible().subscribe { this.progressBarIsVisible.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.sendButtonIsEnabled().subscribe { this.sendButtonIsEnabled.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showMessageThread().subscribe { this.showMessageThread.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showSentError().subscribe { this.showSentError.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showSentSuccess().subscribe { this.showSentSuccess.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testSendingMessageError() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun sendMessage(project: Project, recipient: User, body: String): Observable<Long> {
                    return Observable.error(Throwable("error"))
                }
            }).build()
        )

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
        setUpEnvironment(
            environment().toBuilder().apiClientV2(object : MockApiClientV2() {
                override fun fetchMessagesForThread(messageThreadId: Long): Observable<MessageThreadEnvelope> {
                    return Observable.error(Throwable("error"))
                }
            }).build()
        )

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

        setUpEnvironment(
            environment().toBuilder().apiClientV2(object : MockApiClientV2() {
                override fun fetchMessagesForThread(messageThreadId: Long): Observable<MessageThreadEnvelope> {
                    return Observable.just(MessageThreadEnvelopeFactory.messageThreadEnvelope())
                }
            }).build()
        )

        this.vm.inputs.messageBodyChanged("message")
        this.vm.inputs.sendButtonClicked()
        this.progressBarIsVisible.assertValues(true)
        this.sendButtonIsEnabled.assertValues(true, false)
        this.showSentError.assertNoValues()
        this.showSentSuccess.assertNoValues()
        this.showMessageThread.assertValueCount(1)
    }
}
