package com.kickstarter.viewmodels

import android.content.Intent
import android.graphics.Typeface
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.MessageThreadFactory.messageThread
import com.kickstarter.mock.factories.MessageThreadsEnvelopeFactory.messageThreadsEnvelope
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.Empty
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.Mailbox
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class MessageThreadsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: MessageThreadsViewModel.ViewModel

    private val hasNoMessages = TestSubscriber<Boolean>()
    private val hasNoUnreadMessages = TestSubscriber<Boolean>()
    private val mailboxTitle = TestSubscriber<Int>()
    private val messageThreadList = TestSubscriber<List<MessageThread>>()
    private val messageThreadListCount = TestSubscriber<Int>()
    private val unreadCountTextViewColorInt = TestSubscriber<Int>()
    private val unreadCountTextViewTypefaceInt = TestSubscriber<Int>()
    private val unreadCountToolbarTextViewIsGone = TestSubscriber<Boolean>()
    private val unreadMessagesCount = TestSubscriber<Int>()
    private val unreadMessagesCountIsGone = TestSubscriber<Boolean>()

    private fun setUpEnvironment(env: Environment) {
        vm = MessageThreadsViewModel.ViewModel(env)
        vm.outputs.hasNoMessages().subscribe(hasNoMessages)
        vm.outputs.hasNoUnreadMessages().subscribe(hasNoUnreadMessages)
        vm.outputs.mailboxTitle().subscribe(mailboxTitle)
        vm.outputs.messageThreadList().subscribe(messageThreadList)
        vm.outputs.messageThreadList().map { obj: List<MessageThread?> -> obj.size }
            .subscribe(messageThreadListCount)
        vm.outputs.unreadCountTextViewColorInt().subscribe(unreadCountTextViewColorInt)
        vm.outputs.unreadCountTextViewTypefaceInt().subscribe(unreadCountTextViewTypefaceInt)
        vm.outputs.unreadCountToolbarTextViewIsGone().subscribe(unreadCountToolbarTextViewIsGone)
        vm.outputs.unreadMessagesCount().subscribe(unreadMessagesCount)
        vm.outputs.unreadMessagesCountIsGone().subscribe(unreadMessagesCountIsGone)
    }

    @Test
    fun testMessageThreadsEmit_NoProjectIntent() {
        val currentUser: CurrentUserType = MockCurrentUser()
        currentUser.login(user().toBuilder().unreadMessagesCount(0).build(), "beefbod5")

        val inboxEnvelope = messageThreadsEnvelope()
            .toBuilder()
            .messageThreads(listOf(messageThread()))
            .build()

        val sentEnvelope = messageThreadsEnvelope()
            .toBuilder()
            .messageThreads(listOf(messageThread(), messageThread()))
            .build()

        val apiClient: ApiClientType = object : MockApiClient() {
            override fun fetchMessageThreads(
                project: Project?,
                mailbox: Mailbox
            ): Observable<MessageThreadsEnvelope> {
                return Observable.just(if (mailbox === Mailbox.INBOX) inboxEnvelope else sentEnvelope)
            }
        }

        setUpEnvironment(
            environment().toBuilder().apiClient(apiClient).currentUser(currentUser).build()
        )

        val intent = Intent().putExtra(IntentKey.PROJECT, Empty.INSTANCE)
        vm.intent(intent)

        messageThreadList.assertValueCount(2)
        messageThreadListCount.assertValues(0, 1)

        // Same message threads should not emit again.
        vm.inputs.onResume()

        messageThreadList.assertValueCount(2)
        messageThreadListCount.assertValues(0, 1)

        vm.inputs.mailbox(Mailbox.SENT)

        messageThreadList.assertValueCount(4)
        messageThreadListCount.assertValues(0, 1, 0, 2)
    }

    @Test
    fun testMessageThreadsEmit_WithProjectIntent() {
        val currentUser: CurrentUserType = MockCurrentUser()
        currentUser.login(user().toBuilder().unreadMessagesCount(0).build(), "beefbod5")

        val inboxEnvelope = messageThreadsEnvelope()
            .toBuilder()
            .messageThreads(listOf(messageThread()))
            .build()

        val sentEnvelope = messageThreadsEnvelope()
            .toBuilder()
            .messageThreads(listOf(messageThread(), messageThread()))
            .build()

        val project = project().toBuilder().unreadMessagesCount(5).build()

        val apiClient: ApiClientType = object : MockApiClient() {
            override fun fetchMessageThreads(
                project: Project?,
                mailbox: Mailbox
            ): Observable<MessageThreadsEnvelope> {
                return Observable.just(if (mailbox === Mailbox.INBOX) inboxEnvelope else sentEnvelope)
            }

            override fun fetchProject(param: String): Observable<Project> {
                return Observable.just(project)
            }
        }

        setUpEnvironment(
            environment().toBuilder().apiClient(apiClient).currentUser(currentUser).build()
        )

        val intent = Intent().putExtra(IntentKey.PROJECT, project)
        vm.intent(intent)

        messageThreadList.assertValueCount(2)
        messageThreadListCount.assertValues(0, 1)

        // Same message threads should not emit again.
        vm.inputs.onResume()

        messageThreadList.assertValueCount(2)
        messageThreadListCount.assertValues(0, 1)

        vm.inputs.mailbox(Mailbox.SENT)

        messageThreadList.assertValueCount(4)
        messageThreadListCount.assertValues(0, 1, 0, 2)
    }

    @Test
    fun testHasUnreadMessages() {
        val user = user().toBuilder().unreadMessagesCount(3).build()
        val apiClient: ApiClientType = object : MockApiClient() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        vm.intent(Intent())
        vm.inputs.onResume()

        // Unread count text view is shown.
        unreadMessagesCount.assertValues(user.unreadMessagesCount())
        unreadMessagesCountIsGone.assertValues(false)
        hasNoUnreadMessages.assertValues(false)
        unreadCountTextViewColorInt.assertValues(R.color.accent)
        unreadCountTextViewTypefaceInt.assertValues(Typeface.BOLD)
        unreadCountToolbarTextViewIsGone.assertValues(false)

        vm.inputs.mailbox(Mailbox.SENT)

        unreadMessagesCountIsGone.assertValues(false, true)
    }

    @Test
    fun testNoMessages() {
        val user = user().toBuilder().unreadMessagesCount(null).build()
        val apiClient: ApiClientType = object : MockApiClient() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        vm.intent(Intent())
        vm.inputs.onResume()

        hasNoMessages.assertValues(true)
        unreadMessagesCount.assertNoValues()
        unreadMessagesCountIsGone.assertValue(false)
        unreadCountTextViewColorInt.assertValues(R.color.kds_support_400)
        unreadCountTextViewTypefaceInt.assertValues(Typeface.NORMAL)
        unreadCountToolbarTextViewIsGone.assertValues(true)

        vm.inputs.mailbox(Mailbox.SENT)

        unreadMessagesCountIsGone.assertValues(false, true)
    }

    @Test
    fun testNoUnreadMessages() {
        val user = user().toBuilder().unreadMessagesCount(0).build()
        val apiClient: ApiClientType = object : MockApiClient() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        vm.intent(Intent())
        vm.inputs.onResume()

        hasNoUnreadMessages.assertValues(true)
        unreadMessagesCount.assertNoValues()
        unreadMessagesCountIsGone.assertValue(false)
        unreadCountTextViewColorInt.assertValues(R.color.kds_support_400)
        unreadCountTextViewTypefaceInt.assertValues(Typeface.NORMAL)
        unreadCountToolbarTextViewIsGone.assertValues(true)

        vm.inputs.mailbox(Mailbox.SENT)

        unreadMessagesCountIsGone.assertValues(false, true)
    }

    @Test
    fun testMailboxTitle() {
        val user = user()
        val apiClient: ApiClientType = object : MockApiClient() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        setUpEnvironment(environment().toBuilder().apiClient(apiClient).build())

        vm.intent(Intent())
        vm.inputs.onResume()

        mailboxTitle.assertValue(R.string.messages_navigation_inbox)

        vm.inputs.mailbox(Mailbox.SENT)
        mailboxTitle.assertValues(
            R.string.messages_navigation_inbox,
            R.string.messages_navigation_sent
        )
    }
}
