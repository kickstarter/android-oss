package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.MessageThreadFactory.messageThread
import com.kickstarter.models.MessageThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.joda.time.DateTime
import org.junit.Test

class MessageThreadHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: MessageThreadHolderViewModel.ViewModel

    private val cardViewIsElevated = TestSubscriber<Boolean>()
    private val dateDateTime = TestSubscriber<DateTime>()
    private val dateTextViewIsBold = TestSubscriber<Boolean>()
    private val messageBodyTextIsBold = TestSubscriber<Boolean>()
    private val messageBodyTextViewText = TestSubscriber<String>()
    private val participantAvatarUrl = TestSubscriber<String>()
    private val participantNameTextViewIsBold = TestSubscriber<Boolean>()
    private val participantNameTextViewText = TestSubscriber<String>()
    private val startMessagesActivity = TestSubscriber<MessageThread>()
    private val unreadCountTextViewIsGone = TestSubscriber<Boolean>()
    private val unreadCountTextViewText = TestSubscriber<String>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(env: Environment) {
        vm = MessageThreadHolderViewModel.ViewModel(env)
        vm.outputs.cardViewIsElevated().subscribe { cardViewIsElevated.onNext(it) }.addToDisposable(disposables)
        vm.outputs.dateDateTime().subscribe { dateDateTime.onNext(it) }.addToDisposable(disposables)
        vm.outputs.dateTextViewIsBold().subscribe { dateTextViewIsBold.onNext(it) }.addToDisposable(disposables)
        vm.outputs.messageBodyTextIsBold().subscribe { messageBodyTextIsBold.onNext(it) }.addToDisposable(disposables)
        vm.outputs.messageBodyTextViewText().subscribe { messageBodyTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.participantAvatarUrl().subscribe { participantAvatarUrl.onNext(it) }.addToDisposable(disposables)
        vm.outputs.participantNameTextViewIsBold().subscribe { participantNameTextViewIsBold.onNext(it) }.addToDisposable(disposables)
        vm.outputs.participantNameTextViewText().subscribe { participantNameTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.startMessagesActivity().subscribe { startMessagesActivity.onNext(it) }.addToDisposable(disposables)
        vm.outputs.unreadCountTextViewIsGone().subscribe { unreadCountTextViewIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.unreadCountTextViewText().subscribe { unreadCountTextViewText.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testEmitsDateTime() {
        val messageThread = messageThread()
        setUpEnvironment(environment())

        // Configure the view model with a message thread.
        vm.inputs.configureWith(messageThread)
        dateDateTime.assertValues(messageThread.lastMessage()?.createdAt()!!)
    }

    @Test
    fun testEmitsMessageBodyTextViewText() {
        val messageThread = messageThread()
        setUpEnvironment(environment())

        // Configure the view model with a message thread.
        vm.inputs.configureWith(messageThread)
        messageBodyTextViewText.assertValues(messageThread.lastMessage()?.body()!!)
    }

    @Test
    fun testEmitsParticipantData() {
        val messageThread = messageThread()
        setUpEnvironment(environment())

        // Configure the view model with a message thread.
        vm.inputs.configureWith(messageThread)

        // Emits participant's avatar url and name.
        participantAvatarUrl.assertValues(messageThread.participant()?.avatar()?.medium()!!)
        participantNameTextViewText.assertValues(messageThread.participant()?.name()!!)
    }

    @Test
    fun testMessageThread_Clicked() {
        val messageThread = messageThread()
            .toBuilder()
            .id(12345)
            .unreadMessagesCount(1)
            .build()

        setUpEnvironment(environment())

        vm.inputs.configureWith(messageThread)

        cardViewIsElevated.assertValues(true)
        dateTextViewIsBold.assertValues(true)
        messageBodyTextIsBold.assertValues(true)
        unreadCountTextViewIsGone.assertValues(false)

        vm.inputs.messageThreadCardViewClicked()

        cardViewIsElevated.assertValues(true, false)
        dateTextViewIsBold.assertValues(true, false)
        messageBodyTextIsBold.assertValues(true, false)
        unreadCountTextViewIsGone.assertValues(false, true)
    }

    @Test
    fun testMessageThread_HasNoUnreadMessages() {
        val messageThreadWithNoUnread = messageThread()
            .toBuilder()
            .unreadMessagesCount(0)
            .build()

        setUpEnvironment(environment())

        // Configure the view model with a message thread with no unread messages.
        vm.inputs.configureWith(messageThreadWithNoUnread)

        dateTextViewIsBold.assertValues(false)
        messageBodyTextIsBold.assertValues(false)
        participantNameTextViewIsBold.assertValues(false)
        unreadCountTextViewIsGone.assertValues(true)
        unreadCountTextViewText.assertValues(NumberUtils.format(messageThreadWithNoUnread.unreadMessagesCount()!!))
    }

    @Test
    fun testMessageThread_HasUnreadMessages() {
        val messageThreadWithUnread = messageThread()
            .toBuilder()
            .unreadMessagesCount(2)
            .build()

        setUpEnvironment(environment())

        // Configure the view model with a message thread with unread messages.
        vm.inputs.configureWith(messageThreadWithUnread)

        dateTextViewIsBold.assertValues(true)
        messageBodyTextIsBold.assertValues(true)
        participantNameTextViewIsBold.assertValues(true)
        unreadCountTextViewIsGone.assertValues(false)

        unreadCountTextViewText.assertValues(NumberUtils.format(messageThreadWithUnread.unreadMessagesCount()!!))
    }

    @Test
    fun testStartMessagesActivity() {
        val messageThread = messageThread()

        setUpEnvironment(environment())

        // Configure the view model with a message thread.
        vm.inputs.configureWith(messageThread)
        vm.inputs.messageThreadCardViewClicked()

        startMessagesActivity.assertValues(messageThread)
    }
}
