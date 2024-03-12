package com.kickstarter.viewmodels

import android.content.Intent
import android.graphics.Typeface
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.MessageThreadFactory.messageThread
import com.kickstarter.mock.factories.MessageThreadsEnvelopeFactory.messageThreadsEnvelope
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.models.Empty
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.Mailbox
import com.kickstarter.viewmodels.MessageThreadsViewModel.Factory
import com.kickstarter.viewmodels.MessageThreadsViewModel.MessageThreadsViewModel
import com.kickstarter.viewmodels.usecases.LoginUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class MessageThreadsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: MessageThreadsViewModel

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
    private val isFetchingMessageThreads = TestSubscriber<Boolean>()

    private val disposables = CompositeDisposable()

    @After
    fun cleanUp() {
        disposables.clear()
    }
    private fun setUpEnvironment(env: Environment, intent: Intent) {
        vm = Factory(env, intent).create(MessageThreadsViewModel::class.java)

        vm.outputs.hasNoMessages().subscribe { hasNoMessages.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.hasNoUnreadMessages().subscribe { hasNoUnreadMessages.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.mailboxTitle().subscribe { mailboxTitle.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.messageThreadList().subscribe { messageThreadList.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.messageThreadList().map { obj: List<MessageThread?> -> obj.size }
            .subscribe { messageThreadListCount.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.unreadCountTextViewColorInt().subscribe { unreadCountTextViewColorInt.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.unreadCountTextViewTypefaceInt().subscribe { unreadCountTextViewTypefaceInt.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.unreadCountToolbarTextViewIsGone().subscribe { unreadCountToolbarTextViewIsGone.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.unreadMessagesCount().subscribe { unreadMessagesCount.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.unreadMessagesCountIsGone().subscribe { unreadMessagesCountIsGone.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.isFetchingMessageThreads().subscribe { isFetchingMessageThreads.onNext(it) }
            .addToDisposable(disposables)
    }

    @Test
    fun testMessageThreadsEmit_Pagination() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()

        val inboxEnvelope = messageThreadsEnvelope()
            .toBuilder()
            .messageThreads(listOf(messageThread()))
            .build()

        val sentEnvelope = messageThreadsEnvelope()
            .toBuilder()
            .messageThreads(listOf(messageThread(), messageThread()))
            .build()

        val project = project().toBuilder().unreadMessagesCount(5).build()

        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun fetchMessageThreads(
                project: Project?,
                mailbox: Mailbox
            ): Observable<MessageThreadsEnvelope> {
                return Observable.just(if (mailbox === Mailbox.INBOX) inboxEnvelope else sentEnvelope)
            }

            override fun fetchMessageThreadsWithPaginationPath(paginationPath: String): Observable<MessageThreadsEnvelope> {
                return Observable.just(sentEnvelope)
            }

            override fun fetchProject(param: String): Observable<Project> {
                return Observable.just(project)
            }
        }

        val intent = Intent().putExtra(IntentKey.PROJECT, project)
        val environment = environment().toBuilder()
            .apiClientV2(apiClient)
            .currentUserV2(currentUser)
            .build()

        setUpEnvironment(environment, intent)

        val loginUserCase = LoginUseCase(environment)

        loginUserCase.setToken("beefbod5")
        loginUserCase.setUser(user().toBuilder().unreadMessagesCount(0).build())

        messageThreadList.assertValueCount(1)
        messageThreadListCount.assertValues(1)

        vm.inputs.swipeRefresh()
        messageThreadList.assertValueCount(1)

        vm.inputs.nextPage()

        isFetchingMessageThreads.assertValues(false)
        messageThreadList.assertValueCount(1)
    }

    @Test
    fun testMessageThreadsEmit_NoProjectIntent() {
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()

        val inboxEnvelope = messageThreadsEnvelope()
            .toBuilder()
            .messageThreads(listOf(messageThread()))
            .build()

        val sentEnvelope = messageThreadsEnvelope()
            .toBuilder()
            .messageThreads(listOf(messageThread(), messageThread()))
            .build()

        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun fetchMessageThreads(
                project: Project?,
                mailbox: Mailbox
            ): Observable<MessageThreadsEnvelope> {
                return Observable.just(if (mailbox === Mailbox.INBOX) inboxEnvelope else sentEnvelope)
            }
        }

        val environment = environment().toBuilder()
            .apiClientV2(apiClient)
            .currentUserV2(currentUser)
            .build()

        val intent = Intent().putExtra(IntentKey.PROJECT, Empty.INSTANCE)
        setUpEnvironment(environment, intent)

        val loginUserCase = LoginUseCase(environment)

        loginUserCase.setToken("beefbod5")
        loginUserCase.setUser(user().toBuilder().unreadMessagesCount(0).build())

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
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2()

        val inboxEnvelope = messageThreadsEnvelope()
            .toBuilder()
            .messageThreads(listOf(messageThread()))
            .build()

        val sentEnvelope = messageThreadsEnvelope()
            .toBuilder()
            .messageThreads(listOf(messageThread(), messageThread()))
            .build()

        val project = project().toBuilder().unreadMessagesCount(5).build()

        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
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

        val environment = environment().toBuilder()
            .apiClientV2(apiClient)
            .currentUserV2(currentUser)
            .build()

        val intent = Intent().putExtra(IntentKey.PROJECT, project)

        setUpEnvironment(environment, intent)

        val loginUserCase = LoginUseCase(environment)

        loginUserCase.setToken("beefbod5")
        loginUserCase.setUser(user().toBuilder().unreadMessagesCount(0).build())

        messageThreadList.assertValueCount(1)
        messageThreadListCount.assertValues(1)

        vm.inputs.onResume()

        messageThreadList.assertValueCount(1)
        messageThreadListCount.assertValues(1)

        vm.inputs.mailbox(Mailbox.SENT)

        messageThreadList.assertValueCount(3)
        messageThreadListCount.assertValues(1, 0, 2)
    }

    @Test
    fun testHasUnreadMessages() {
        val user = user().toBuilder().unreadMessagesCount(3).build()
        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        val environment = environment().toBuilder()
            .apiClientV2(apiClient)
            .build()

        setUpEnvironment(environment, Intent())

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
        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        val environment = environment().toBuilder().apiClientV2(apiClient).build()
        setUpEnvironment(environment, Intent())

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
        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        val environment = environment().toBuilder()
            .apiClientV2(apiClient)
            .build()

        setUpEnvironment(environment, Intent())

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
        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun fetchCurrentUser(): Observable<User> {
                return Observable.just(user)
            }
        }

        val environment = environment().toBuilder()
            .apiClientV2(apiClient)
            .build()

        setUpEnvironment(environment, Intent())

        vm.inputs.onResume()

        mailboxTitle.assertValue(R.string.messages_navigation_inbox)

        vm.inputs.mailbox(Mailbox.SENT)
        mailboxTitle.assertValues(
            R.string.messages_navigation_inbox,
            R.string.messages_navigation_sent
        )
    }
}
