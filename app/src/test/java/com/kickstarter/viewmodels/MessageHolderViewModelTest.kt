package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.MessageFactory.message
import com.kickstarter.mock.factories.UserFactory.user
import org.junit.Test
import rx.observers.TestSubscriber

class MessageHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: MessageHolderViewModel.ViewModel
    private val messageBodyRecipientCardViewIsGone = TestSubscriber<Boolean>()
    private val messageBodyRecipientTextViewText = TestSubscriber<String>()
    private val messageBodySenderCardViewIsGone = TestSubscriber<Boolean>()
    private val messageBodySenderTextViewText = TestSubscriber<String>()
    private val participantAvatarImageHidden = TestSubscriber<Boolean>()
    private val participantAvatarImageUrl = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        vm = MessageHolderViewModel.ViewModel(environment)

        vm.outputs.messageBodyRecipientCardViewIsGone().subscribe(
            messageBodyRecipientCardViewIsGone
        )
        vm.outputs.messageBodyRecipientTextViewText().subscribe(messageBodyRecipientTextViewText)
        vm.outputs.messageBodySenderCardViewIsGone().subscribe(messageBodySenderCardViewIsGone)
        vm.outputs.messageBodySenderTextViewText().subscribe(messageBodySenderTextViewText)
        vm.outputs.participantAvatarImageHidden().subscribe(participantAvatarImageHidden)
        vm.outputs.participantAvatarImageUrl().subscribe(participantAvatarImageUrl)
    }

    @Test
    fun testMessageBodyTextViewFormatting_CurrentUserIsRecipient() {
        val recipient = user().toBuilder().name("Ima Backer").id(123L).build()
        val sender = user().toBuilder().name("Ima Creator").id(456L).build()
        val message = message().toBuilder()
            .recipient(recipient)
            .sender(sender)
            .build()

        val currentUser = MockCurrentUser(recipient)

        setUpEnvironment(environment().toBuilder().currentUser(currentUser).build())

        vm.inputs.configureWith(message)

        messageBodyRecipientCardViewIsGone.assertValues(false)
        messageBodyRecipientTextViewText.assertValues(message.body())
        messageBodySenderCardViewIsGone.assertValues(true)
        messageBodySenderTextViewText.assertNoValues()
    }

    @Test
    fun testMessageBodyTextViewFormatting_CurrentUserIsSender() {
        val recipient = user().toBuilder().name("Ima Creator").id(123L).build()
        val sender = user().toBuilder().name("Ima Backer").id(456L).build()
        val message = message().toBuilder()
            .recipient(recipient)
            .sender(sender)
            .build()

        val currentUser = MockCurrentUser(sender)

        setUpEnvironment(environment().toBuilder().currentUser(currentUser).build())

        vm.inputs.configureWith(message)

        messageBodyRecipientCardViewIsGone.assertValues(true)
        messageBodyRecipientTextViewText.assertNoValues()
        messageBodySenderCardViewIsGone.assertValues(false)
        messageBodySenderTextViewText.assertValues(message.body())
    }

    @Test
    fun testParticipantAvatarImage_CurrentUserIsRecipient() {
        val recipient = user().toBuilder().name("Ima Backer").id(123L).build()
        val sender = user().toBuilder().name("Ima Creator").id(456L).build()
        val message = message().toBuilder()
            .recipient(recipient)
            .sender(sender)
            .build()
        val currentUser = MockCurrentUser(recipient)
        setUpEnvironment(environment().toBuilder().currentUser(currentUser).build())
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
        val currentUser = MockCurrentUser(sender)
        setUpEnvironment(environment().toBuilder().currentUser(currentUser).build())
        vm.inputs.configureWith(message)

        // Avatar hidden for sender who is the backer.
        participantAvatarImageHidden.assertValues(true)
        participantAvatarImageUrl.assertNoValues()
    }
}
