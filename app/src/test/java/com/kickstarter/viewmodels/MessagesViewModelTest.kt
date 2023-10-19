package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.BackingFactory.backing
import com.kickstarter.mock.factories.MessageFactory.message
import com.kickstarter.mock.factories.MessageThreadEnvelopeFactory.empty
import com.kickstarter.mock.factories.MessageThreadEnvelopeFactory.messageThreadEnvelope
import com.kickstarter.mock.factories.MessageThreadFactory.messageThread
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.UserFactory.creator
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.models.Backing
import com.kickstarter.models.BackingWrapper
import com.kickstarter.models.Message
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.MessageThreadEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.data.MessageSubject
import com.kickstarter.viewmodels.MessagesViewModel.Factory
import com.kickstarter.viewmodels.MessagesViewModel.MessagesViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class MessagesViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: MessagesViewModel

    private val backButtonIsGone = TestSubscriber<Boolean>()
    private val backingAndProject = TestSubscriber<Pair<Backing, Project>>()
    private val backingInfoViewIsGone = TestSubscriber<Boolean>()
    private val closeButtonIsGone = TestSubscriber<Boolean>()
    private val creatorNameTextViewText = TestSubscriber<String>()
    private val messageEditTextHint = TestSubscriber<String>()
    private val messageEditTextShouldRequestFocus = TestSubscriber<Unit>()
    private val messageList = TestSubscriber<List<Message>>()
    private val projectNameTextViewText = TestSubscriber<String>()
    private val projectNameToolbarTextViewText = TestSubscriber<String>()
    private val recyclerViewDefaultBottomPadding = TestSubscriber<Unit>()
    private val recyclerViewInitialBottomPadding = TestSubscriber<Int>()
    private val scrollRecyclerViewToBottom = TestSubscriber<Unit>()
    private val sendMessageButtonIsEnabled = TestSubscriber<Boolean>()
    private val setMessageEditText = TestSubscriber<String>()
    private val showMessageErrorToast = TestSubscriber<String>()
    private val startBackingActivity = TestSubscriber<BackingWrapper>()
    private val successfullyMarkedAsRead = TestSubscriber<Unit>()
    private val toolbarIsExpanded = TestSubscriber<Boolean>()
    private val viewPledgeButtonIsGone = TestSubscriber<Boolean>()

    private val disposables = CompositeDisposable()

    @After
    fun cleanUp() {
        disposables.clear()
    }
    private fun setUpEnvironment(environment: Environment, intent: Intent) {
        vm = Factory(environment, intent).create(MessagesViewModel::class.java)
        vm.outputs.backButtonIsGone().subscribe { backButtonIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.backingAndProject().subscribe { backingAndProject.onNext(it) }.addToDisposable(disposables)
        vm.outputs.backingInfoViewIsGone().subscribe { backingInfoViewIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.closeButtonIsGone().subscribe { closeButtonIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.messageEditTextHint().subscribe { messageEditTextHint.onNext(it) }.addToDisposable(disposables)
        vm.outputs.messageEditTextShouldRequestFocus().subscribe { messageEditTextShouldRequestFocus.onNext(it) }.addToDisposable(disposables)
        vm.outputs.messageList().subscribe { messageList.onNext(it) }.addToDisposable(disposables)
        vm.outputs.creatorNameTextViewText().subscribe { creatorNameTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectNameTextViewText().subscribe { projectNameTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.projectNameToolbarTextViewText().subscribe { projectNameToolbarTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.recyclerViewDefaultBottomPadding().subscribe { recyclerViewDefaultBottomPadding.onNext(it) }.addToDisposable(disposables)
        vm.outputs.recyclerViewInitialBottomPadding().subscribe { recyclerViewInitialBottomPadding.onNext(it) }.addToDisposable(disposables)
        vm.outputs.scrollRecyclerViewToBottom().subscribe { scrollRecyclerViewToBottom.onNext(it) }.addToDisposable(disposables)
        vm.outputs.sendMessageButtonIsEnabled().subscribe { sendMessageButtonIsEnabled.onNext(it) }.addToDisposable(disposables)
        vm.outputs.setMessageEditText().subscribe { setMessageEditText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showMessageErrorToast().subscribe { showMessageErrorToast.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startBackingActivity().subscribe { startBackingActivity.onNext(it) }.addToDisposable(disposables)
        vm.outputs.successfullyMarkedAsRead().subscribe { successfullyMarkedAsRead.onNext(it) }.addToDisposable(disposables)
        vm.outputs.toolbarIsExpanded().subscribe { toolbarIsExpanded.onNext(it) }.addToDisposable(disposables)
        vm.outputs.viewPledgeButtonIsGone().subscribe { viewPledgeButtonIsGone.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testBackButton_IsGone() {
        setUpEnvironment(environment().toBuilder().currentUserV2(MockCurrentUserV2(user())).build(), messagesContextIntent(messageThread()))

        // Back button is gone if navigating from non-backer modal view.
        backButtonIsGone.assertValues(true)
        closeButtonIsGone.assertValues(false)
    }

    @Test
    fun testBackButton_IsVisible() {
        setUpEnvironment(environment().toBuilder().currentUserV2(MockCurrentUserV2(user())).build(), backerModalContextIntent(backing(), project()))

        // Back button is visible if navigating from backer modal view.
        backButtonIsGone.assertValues(false)
        closeButtonIsGone.assertValues(true)
    }

    @Test
    fun testBackingAndProject_Participant() {
        val project = project().toBuilder()
            .isBacking(false)
            .build()
        val backing = backing().toBuilder()
            .project(project)
            .build()
        val messageThread = messageThread().toBuilder()
            .project(project)
            .backing(backing)
            .build()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchMessagesForThread(messageThread: MessageThread): Observable<MessageThreadEnvelope> {
                return Observable.just(messageThreadEnvelope())
            }

            override fun fetchProjectBacking(project: Project, user: User): Observable<Backing> {
                return Observable.just(backing)
            }
        }

        // Start the view model with a message thread.
        setUpEnvironment(
            environment().toBuilder()
                .apiClientV2(apiClient)
                .currentUserV2(MockCurrentUserV2(user()))
                .build(),
            messagesContextIntent(messageThread)
        )

        backingAndProject.assertValues(Pair.create(backing, backing.project()))
        backingInfoViewIsGone.assertValues(false)
    }

    @Test
    fun testBackingInfo_NoBacking() {
        val project = project().toBuilder()
            .isBacking(false)
            .build()
        val messageThread = messageThread().toBuilder()
            .project(project)
            .backing(null)
            .build()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchProjectBacking(project: Project, user: User): Observable<Backing> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }
        // Start the view model with a message thread.
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(MockCurrentUserV2(user()))
                .build(),
            messagesContextIntent(messageThread)
        )

        backingAndProject.assertNoValues()
        backingInfoViewIsGone.assertValues(true)
    }

    @Test
    fun testConfiguredWithThread() {
        val messageThread = messageThread()
        // Start the view model with a message thread.
        setUpEnvironment(
            environment().toBuilder().apiClientV2(MockApiClientV2()).currentUserV2(MockCurrentUserV2(user())).build(),
            messagesContextIntent(messageThread)
        )

        backingAndProject.assertValueCount(1)
        messageList.assertValueCount(1)
    }

    @Test
    fun testConfiguredWithProject_AndBacking() {
        val backing = backing()
        val project = project()

        // Start the view model with a backing and a project.
        setUpEnvironment(
            environment().toBuilder().apiClientV2(MockApiClientV2()).currentUserV2(MockCurrentUserV2(user())).build(),
            backerModalContextIntent(backing, project)
        )

        backingAndProject.assertValueCount(1)
        messageList.assertValueCount(1)
    }

    @Test
    fun testCreatorViewingProjectMessages() {
        val creator = creator().toBuilder().name("Sharon").build()
        val participant = user().toBuilder().name("Timothy").build()
        val currentUser: CurrentUserTypeV2 = MockCurrentUserV2(creator)
        val messageThread = messageThread()
            .toBuilder()
            .project(project().toBuilder().creator(creator).build())
            .participant(participant)
            .build()

        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchMessagesForThread(thread: MessageThread): Observable<MessageThreadEnvelope> {
                return Observable.just(
                    messageThreadEnvelope().toBuilder().messageThread(messageThread).build()
                )
            }
        }

        // Start the view model with a message thread.
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(currentUser).build(),
            messagesContextIntent(messageThread)
        )

        // Creator name is the project creator, edit text hint is always the participant.
        creatorNameTextViewText.assertValues(creator.name())
        messageEditTextHint.assertValues(participant.name())
    }

    @Test
    fun testProjectData_ExistingMessages() {
        val messageThread = messageThread()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchMessagesForThread(thread: MessageThread): Observable<MessageThreadEnvelope> {
                return Observable.just(messageThreadEnvelope())
            }
        }

        // Start the view model with a message thread.
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(MockCurrentUserV2(user()))
                .build(),
            messagesContextIntent(messageThread)
        )

        creatorNameTextViewText.assertValues(messageThread.project()?.creator()?.name())
        projectNameTextViewText.assertValues(messageThread.project()?.name())
        projectNameToolbarTextViewText.assertValues(messageThread.project()?.name())
    }

    @Test
    fun testMessageEditTextHint() {
        val messageThread = messageThread()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchMessagesForThread(thread: MessageThread): Observable<MessageThreadEnvelope> {
                return Observable.just(messageThreadEnvelope())
            }
        }

        // Start the view model with a message thread.
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(MockCurrentUserV2(user()))
                .build(),
            messagesContextIntent(messageThread)
        )

        messageEditTextHint.assertValues(messageThread.project()?.creator()?.name())
    }

    @Test
    fun testMessagesEmit() {
        val envelope = messageThreadEnvelope()
            .toBuilder()
            .messages(listOf(message()))
            .build()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchMessagesForThread(messageThread: MessageThread): Observable<MessageThreadEnvelope> {
                return Observable.just(envelope)
            }
        }

        // Start the view model with a message thread.
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(MockCurrentUserV2(user()))
                .build(),
            messagesContextIntent(messageThread())
        )

        // Messages emit, keyboard not shown.
        messageList.assertValueCount(1)
        messageEditTextShouldRequestFocus.assertNoValues()
    }

    @Test
    fun testNoMessages() {
        val backing = backing()
        val project = project()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchMessagesForBacking(backing: Backing): Observable<MessageThreadEnvelope> {
                return Observable.just(empty())
            }
        }

        // Start the view model with a backing and a project.
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(MockCurrentUserV2(user()))
                .build(),
            backerModalContextIntent(backing, project)
        )

        // All data except for messages should emit.
        messageList.assertValue(emptyList())
        creatorNameTextViewText.assertValues(project.creator().name())
        backingAndProject.assertValues(Pair.create(backing, project))
    }

    @Test
    fun testRecyclerViewBottomPadding() {
        val appBarTotalScrolLRange = 327

        // Start the view model with a message thread.
        setUpEnvironment(
            environment().toBuilder().currentUser(MockCurrentUser(user())).build(),
            messagesContextIntent(messageThread())
        )

        // View initially loaded with a 0 (expanded) offset.
        vm.inputs.appBarOffset(0)
        vm.inputs.appBarTotalScrollRange(appBarTotalScrolLRange)

        // Only initial bottom padding emits.
        recyclerViewDefaultBottomPadding.assertNoValues()
        recyclerViewInitialBottomPadding.assertValues(appBarTotalScrolLRange)

        // User scrolls.
        vm.inputs.appBarOffset(-30)
        vm.inputs.appBarTotalScrollRange(appBarTotalScrolLRange)

        // Default padding emits, initial padding does not emit again.
        recyclerViewDefaultBottomPadding.assertValueCount(1)
        recyclerViewInitialBottomPadding.assertValues(appBarTotalScrolLRange)

        // User scrolls.
        vm.inputs.appBarOffset(20)
        vm.inputs.appBarTotalScrollRange(appBarTotalScrolLRange)

        // Padding does not change.
        recyclerViewDefaultBottomPadding.assertValueCount(1)
        recyclerViewInitialBottomPadding.assertValues(appBarTotalScrolLRange)
    }

    @Test
    fun testSendMessage_Error() {
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun sendMessage(
                messageSubject: MessageSubject,
                body: String
            ): Observable<Message> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }

        // Start the view model with a message thread.
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(MockCurrentUserV2(user()))
                .build(),
            messagesContextIntent(messageThread())
        )

        // Send a message unsuccessfully.
        vm.inputs.messageEditTextChanged("Hello there")
        vm.inputs.sendMessageButtonClicked()

        // Error toast is displayed, errored message body remains in edit text, no new message is emitted.
        showMessageErrorToast.assertValueCount(1)
        setMessageEditText.assertNoValues()
    }

    @Test
    fun testSendMessage_Success() {
        val sentMessage = message()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun sendMessage(
                messageSubject: MessageSubject,
                body: String
            ): Observable<Message> {
                return Observable.just(sentMessage)
            }
        }

        // Start the view model with a message thread.
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(MockCurrentUserV2(user()))
                .build(),
            messagesContextIntent(messageThread())
        )

        // Initial messages emit.
        messageList.assertValueCount(1)

        // Send a message successfully.
        vm.inputs.messageEditTextChanged("Salutations friend!")
        vm.inputs.sendMessageButtonClicked()

        // New message list emits.
        messageList.assertValueCount(2)

        // Reply edit text should be cleared and view should be scrolled to new message.
        setMessageEditText.assertValues("")
        scrollRecyclerViewToBottom.assertValueCount(1)
    }

    @Test
    fun testSendMessageButtonIsEnabled() {
        setUpEnvironment(
            environment().toBuilder().currentUserV2(MockCurrentUserV2(user())).build(),
            messagesContextIntent(messageThread())
        )

        sendMessageButtonIsEnabled.assertNoValues()
        vm.inputs.messageEditTextChanged("hello")
        sendMessageButtonIsEnabled.assertValues(true)
        vm.inputs.messageEditTextChanged("")
        sendMessageButtonIsEnabled.assertValues(true, false)
    }

    @Test
    fun testShouldRequestFocus() {
        val backing = backing()
        val envelope = messageThreadEnvelope()
            .toBuilder()
            .messages(null)
            .build()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchMessagesForBacking(backing: Backing): Observable<MessageThreadEnvelope> {
                return Observable.just(envelope)
            }
        }

        // Start the view model with a backing and project.
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(MockCurrentUserV2(user()))
                .build(),
            backerModalContextIntent(backing, project())
        )

        messageEditTextShouldRequestFocus.assertValueCount(1)
    }

    @Test
    fun testStartBackingActivity_AsBacker() {
        val user = user()
        val project = project().toBuilder().isBacking(true).build()
        val backing = backing()
        val messageThread = messageThread()
            .toBuilder()
            .project(project)
            .build()
        val messageThreadEnvelope = messageThreadEnvelope()
            .toBuilder()
            .messageThread(messageThread)
            .build()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchMessagesForBacking(backing: Backing): Observable<MessageThreadEnvelope> {
                return Observable.just(messageThreadEnvelope)
            }
        }
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(MockCurrentUserV2(user))
                .build(),
            backerModalContextIntent(backing, project)
        )

        vm.inputs.viewPledgeButtonClicked()
        startBackingActivity.assertValues(BackingWrapper(backing, user, project))
    }

    @Test
    fun testStartBackingActivity_AsBacker_EmptyThread() {
        val user = user()
        val project = project().toBuilder().isBacking(true).build()
        val backing = backing()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchMessagesForBacking(backing: Backing): Observable<MessageThreadEnvelope> {
                return Observable.just(empty())
            }
        }
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(MockCurrentUserV2(user))
                .build(),
            creatorBioModalContextIntent(backing, project)
        )

        vm.inputs.viewPledgeButtonClicked()
        startBackingActivity.assertValues(BackingWrapper(backing, user, project))
    }

    @Test
    fun testStartBackingActivity_AsCreator() {
        val backer = user().toBuilder().name("Vanessa").build()
        val creator = user().toBuilder().name("Jessica").build()
        val backing = backing()
        val project = project().toBuilder().creator(creator).build()
        val messageThread = messageThread()
            .toBuilder()
            .backing(backing)
            .participant(backer)
            .project(project)
            .build()
        val messageThreadEnvelope = messageThreadEnvelope()
            .toBuilder()
            .messageThread(messageThread)
            .build()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchMessagesForThread(messageThread: MessageThread): Observable<MessageThreadEnvelope> {
                return Observable.just(messageThreadEnvelope)
            }

            override fun fetchProjectBacking(project: Project, user: User): Observable<Backing> {
                return Observable.just(backing)
            }
        }
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(MockCurrentUserV2(creator))
                .build(),
            messagesContextIntent(messageThread)
        )

        vm.inputs.viewPledgeButtonClicked()

        startBackingActivity.assertValues(
            BackingWrapper(
                messageThread.backing()!!,
                backer,
                project
            )
        )
    }

    @Test
    fun testSuccessfullyMarkedAsRead() {
        val messageThread = messageThread()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun markAsRead(thread: MessageThread): Observable<MessageThread> {
                return Observable.just(messageThread)
            }
        }
        setUpEnvironment(
            environment().toBuilder().currentUserV2(MockCurrentUserV2(user())).apiClientV2(apiClient)
                .build(),
            messagesContextIntent(messageThread)
        )

        successfullyMarkedAsRead.assertValueCount(1)
    }

    @Test
    fun testToolbarIsExpanded_NoMessages() {
        val backing = backing()
        val envelope = messageThreadEnvelope()
            .toBuilder()
            .messages(null)
            .build()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchMessagesForBacking(backing: Backing): Observable<MessageThreadEnvelope> {
                return Observable.just(envelope)
            }
        }

        // Start the view model with a backing and project.
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(MockCurrentUserV2(user()))
                .build(),
            backerModalContextIntent(backing, project())
        )

        vm.inputs.messageEditTextIsFocused(true)

        // Toolbar stays expanded when keyboard opens and no messages.
        toolbarIsExpanded.assertValue(false)
    }

    @Test
    fun testToolbarIsExpanded_WithMessages() {
        val backing = backing()
        val envelope = messageThreadEnvelope()
            .toBuilder()
            .messages(listOf(message()))
            .build()
        val apiClient: MockApiClientV2 = object : MockApiClientV2() {
            override fun fetchMessagesForBacking(backing: Backing): Observable<MessageThreadEnvelope> {
                return Observable.just(envelope)
            }
        }

        // Start the view model with a backing and project.
        setUpEnvironment(
            environment().toBuilder().apiClientV2(apiClient).currentUserV2(MockCurrentUserV2(user()))
                .build(),
            backerModalContextIntent(backing, project())
        )

        vm.inputs.messageEditTextIsFocused(true)

        // Toolbar collapsed when keyboard opens and there are messages.
        toolbarIsExpanded.assertValues(false)
    }

    @Test
    fun testViewMessages_FromPush() {
        setUpEnvironment(environment().toBuilder().currentUserV2(MockCurrentUserV2(user())).build(), pushContextIntent())

        backButtonIsGone.assertValues(true)
        closeButtonIsGone.assertValues(false)
        viewPledgeButtonIsGone.assertValues(false)
    }

    @Test
    fun testViewPledgeButton_IsGone_backerModal() {
        setUpEnvironment(
            environment().toBuilder().currentUserV2(MockCurrentUserV2(user())).build(),
            backerModalContextIntent(backing(), project())
        )

        // View pledge button is hidden when context is from the backer modal.
        viewPledgeButtonIsGone.assertValues(true)
    }

    @Test
    fun testViewPledgeButton_IsVisible_creatorBioModal() {
        setUpEnvironment(
            environment().toBuilder().currentUserV2(MockCurrentUserV2(user())).build(),
            creatorBioModalContextIntent(backing(), project())
        )

        // View pledge button is shown when context is from the creator bio modal.
        viewPledgeButtonIsGone.assertValues(false)
    }

    @Test
    fun testViewPledgeButton_IsVisible() {
        setUpEnvironment(
            environment().toBuilder().currentUserV2(MockCurrentUserV2(user())).build(),
            messagesContextIntent(messageThread())
        )

        // View pledge button is shown when context is from anywhere but the backer modal.
        viewPledgeButtonIsGone.assertValues(false)
    }

    companion object {
        private fun backerModalContextIntent(backing: Backing, project: Project): Intent {
            return Intent()
                .putExtra(IntentKey.BACKING, backing)
                .putExtra(IntentKey.PROJECT, project)
                .putExtra(
                    IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT,
                    MessagePreviousScreenType.BACKER_MODAL
                )
        }

        private fun creatorBioModalContextIntent(backing: Backing, project: Project): Intent {
            return Intent()
                .putExtra(IntentKey.BACKING, backing)
                .putExtra(IntentKey.PROJECT, project)
                .putExtra(
                    IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT,
                    MessagePreviousScreenType.CREATOR_BIO_MODAL
                )
        }

        private fun messagesContextIntent(messageThread: MessageThread): Intent {
            return Intent()
                .putExtra(IntentKey.MESSAGE_THREAD, messageThread)
                .putExtra(
                    IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT,
                    MessagePreviousScreenType.MESSAGES
                )
        }

        private fun pushContextIntent(): Intent {
            return messagesContextIntent(messageThread())
                .putExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT, MessagePreviousScreenType.PUSH)
        }
    }
}
