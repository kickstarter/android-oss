package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.MessageFactory.message
import com.kickstarter.mock.factories.UserFactory.user
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class MessageHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: MessageHolderViewModel.ViewModel
    private val messageBodyRecipientCardViewIsGone = TestSubscriber<Boolean>()
    private val messageBodyRecipientTextViewText = TestSubscriber<String>()
    private val messageBodySenderCardViewIsGone = TestSubscriber<Boolean>()
    private val messageBodySenderTextViewText = TestSubscriber<String>()
    private val participantAvatarImageHidden = TestSubscriber<Boolean>()
    private val participantAvatarImageUrl = TestSubscriber<String>()
    private val deliveryStatusTextViewIsGone = TestSubscriber<Boolean>()
    private val disposables = CompositeDisposable()

    @After
    fun clear() {
        disposables.clear()
    }

    private fun setUpEnvironment(environment: Environment) {
        vm = MessageHolderViewModel.ViewModel(environment)

        vm.outputs.messageBodyRecipientCardViewIsGone().subscribe { messageBodyRecipientCardViewIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.messageBodyRecipientTextViewText().subscribe { messageBodyRecipientTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.messageBodySenderCardViewIsGone().subscribe { messageBodySenderCardViewIsGone.onNext(it) }.addToDisposable(disposables)
        vm.outputs.messageBodySenderTextViewText().subscribe { messageBodySenderTextViewText.onNext(it) }.addToDisposable(disposables)
        vm.outputs.participantAvatarImageHidden().subscribe { participantAvatarImageHidden.onNext(it) }.addToDisposable(disposables)
        vm.outputs.participantAvatarImageUrl().subscribe { participantAvatarImageUrl.onNext(it) }.addToDisposable(disposables)
        vm.outputs.deliveryStatusTextViewIsGone().subscribe { deliveryStatusTextViewIsGone.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testMessageBodyTextViewFormatting_CurrentUserIsRecipient() {
        val recipient = user().toBuilder().name("Ima Backer").id(123L).build()
        val sender = user().toBuilder().name("Ima Creator").id(456L).build()
        val message = message().toBuilder()
            .recipient(recipient)
            .sender(sender)
            .build()

        val currentUser = MockCurrentUserV2(recipient)

        setUpEnvironment(environment().toBuilder().currentUserV2(currentUser).build())

        vm.inputs.configureWith(message)

        messageBodyRecipientCardViewIsGone.assertValues(false)
        messageBodyRecipientTextViewText.assertValues(message.body())
        messageBodySenderCardViewIsGone.assertValues(true)
        messageBodySenderTextViewText.assertNoValues()
        deliveryStatusTextViewIsGone.assertNoValues()

        vm.inputs.isLastPosition(false)
        deliveryStatusTextViewIsGone.assertValues(true)

        vm.inputs.isLastPosition(true)
        deliveryStatusTextViewIsGone.assertValues(true)
    }

    @Test
    fun testMessageBodyTextViewFormatting_CurrentUserIsSender() {
        val recipient = user().toBuilder().name("Ima Creator").id(123L).build()
        val sender = user().toBuilder().name("Ima Backer").id(456L).build()
        val message = message().toBuilder()
            .recipient(recipient)
            .sender(sender)
            .build()

        val currentUser = MockCurrentUserV2(sender)

        setUpEnvironment(environment().toBuilder().currentUserV2(currentUser).build())

        vm.inputs.configureWith(message)

        messageBodyRecipientCardViewIsGone.assertValues(true)
        messageBodyRecipientTextViewText.assertNoValues()
        messageBodySenderCardViewIsGone.assertValues(false)
        messageBodySenderTextViewText.assertValues(message.body())
        deliveryStatusTextViewIsGone.assertNoValues()

        vm.inputs.isLastPosition(true)
        deliveryStatusTextViewIsGone.assertValues(false)

        vm.inputs.isLastPosition(false)
        deliveryStatusTextViewIsGone.assertValues(false)
    }

    @Test
    fun testParticipantAvatarImage_CurrentUserIsRecipient() {
        val recipient = user().toBuilder().name("Ima Backer").id(123L).build()
        val sender = user().toBuilder().name("Ima Creator").id(456L).build()
        val message = message().toBuilder()
            .recipient(recipient)
            .sender(sender)
            .build()
        val currentUser = MockCurrentUserV2(recipient)
        setUpEnvironment(environment().toBuilder().currentUserV2(currentUser).build())
        vm.inputs.configureWith(message)

        // Avatar shown for sender who is the creator.
        participantAvatarImageHidden.assertValues(false)
        participantAvatarImageUrl.assertValues(message.sender().avatar().medium())
    }

    @Test
    fun testParticipantAvatarImage_CurrentUserIsSender() {
        val recipient = user().toBuilder().name("Ima Creator").id(123L).build()
        val sender = user().toBuilder().name("Ima Backer").id(456L).build()
        val message = message().toBuilder()
            .recipient(recipient)
            .sender(sender)
            .build()
        val currentUser = MockCurrentUserV2(sender)
        setUpEnvironment(environment().toBuilder().currentUserV2(currentUser).build())
        vm.inputs.configureWith(message)

        // Avatar hidden for sender who is the backer.
        participantAvatarImageHidden.assertValues(true)
        participantAvatarImageUrl.assertNoValues()
    }
}
