package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.factories.MessageFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.models.Message;
import com.kickstarter.models.User;

import org.junit.Test;

import rx.observers.TestSubscriber;

public final class MessageHolderViewModelTest extends KSRobolectricTestCase {
  private MessageHolderViewModel.ViewModel vm;
  private final TestSubscriber<Boolean> participantAvatarImageHidden = new TestSubscriber<>();
  private final TestSubscriber<String> participantAvatarImageUrl = new TestSubscriber<>();
  private final TestSubscriber<Boolean> messageBodyTextViewAlignParentEnd = new TestSubscriber<>();
  private final TestSubscriber<Integer> messageBodyTextViewBackgroundColorInt = new TestSubscriber<>();
  private final TestSubscriber<String> messageBodyTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Integer> messageBodyTextViewTextColorInt = new TestSubscriber<>();
  private final TestSubscriber<Boolean> shouldSetMessageBodyPaddingForSender = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new MessageHolderViewModel.ViewModel(environment);
    this.vm.outputs.participantAvatarImageHidden().subscribe(this.participantAvatarImageHidden);
    this.vm.outputs.participantAvatarImageUrl().subscribe(this.participantAvatarImageUrl);
    this.vm.outputs.messageBodyCardViewAlignParentEnd().subscribe(this.messageBodyTextViewAlignParentEnd);
    this.vm.outputs.messageBodyTextViewBackgroundColorInt().subscribe(this.messageBodyTextViewBackgroundColorInt);
    this.vm.outputs.messageBodyTextViewText().subscribe(this.messageBodyTextViewText);
    this.vm.outputs.messageBodyTextViewTextColorInt().subscribe(this.messageBodyTextViewTextColorInt);
    this.vm.outputs.shouldSetMessageBodyTextViewPaddingForSender().subscribe(this.shouldSetMessageBodyPaddingForSender);
  }

  @Test
  public void testEmitsMessageBody() {
    final Message message = MessageFactory.message();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(message);
    this.messageBodyTextViewText.assertValues(message.body());
  }

  @Test
  public void testParticipantAvatarImage_CurrentUserIsRecipient() {
    final User recipient = UserFactory.user().toBuilder().name("Ima Backer").id(123).build();
    final User sender = UserFactory.user().toBuilder().name("Ima Creator").id(456).build();

    final Message message = MessageFactory.message().toBuilder()
      .recipient(recipient)
      .sender(sender)
      .build();

    final MockCurrentUser currentUser = new MockCurrentUser(recipient);

    setUpEnvironment(environment().toBuilder().currentUser(currentUser).build());

    this.vm.inputs.configureWith(message);

    // Avatar shown for sender who is the creator.
    this.participantAvatarImageHidden.assertValues(false);
    this.participantAvatarImageUrl.assertValues(message.sender().avatar().medium());
  }

  @Test
  public void testParticipantAvatarImage_CurrentUserIsSender() {
    final User recipient = UserFactory.user().toBuilder().name("Ima Creator").id(123).build();
    final User sender = UserFactory.user().toBuilder().name("Ima Backer").id(456).build();

    final Message message = MessageFactory.message().toBuilder()
      .recipient(recipient)
      .sender(sender)
      .build();

    final MockCurrentUser currentUser = new MockCurrentUser(sender);

    setUpEnvironment(environment().toBuilder().currentUser(currentUser).build());

    this.vm.inputs.configureWith(message);

    // Avatar hidden for sender who is the backer.
    this.participantAvatarImageHidden.assertValues(true);
    this.participantAvatarImageUrl.assertNoValues();
  }

  @Test
  public void testMessageBodyTextViewFormatting_CurrentUserIsRecipient() {
    final User recipient = UserFactory.user().toBuilder().name("Ima Backer").id(123).build();
    final User sender = UserFactory.user().toBuilder().name("Ima Creator").id(456).build();

    final Message message = MessageFactory.message().toBuilder()
      .recipient(recipient)
      .sender(sender)
      .build();

    final MockCurrentUser currentUser = new MockCurrentUser(recipient);

    setUpEnvironment(environment().toBuilder().currentUser(currentUser).build());

    this.vm.inputs.configureWith(message);

    this.messageBodyTextViewAlignParentEnd.assertValues(false);
    this.shouldSetMessageBodyPaddingForSender.assertValues(false);
    this.messageBodyTextViewBackgroundColorInt.assertValues(R.color.ksr_grey_400);
    this.messageBodyTextViewTextColorInt.assertValues(R.color.ksr_text_navy_700);
  }

  @Test
  public void testMessageBodyTextViewFormatting_CurrentUserIsSender() {
    final User recipient = UserFactory.user().toBuilder().name("Ima Creator").id(123).build();
    final User sender = UserFactory.user().toBuilder().name("Ima Backer").id(456).build();

    final Message message = MessageFactory.message().toBuilder()
      .recipient(recipient)
      .sender(sender)
      .build();

    final MockCurrentUser currentUser = new MockCurrentUser(sender);

    setUpEnvironment(environment().toBuilder().currentUser(currentUser).build());

    this.vm.inputs.configureWith(message);

    this.messageBodyTextViewAlignParentEnd.assertValues(true);
    this.shouldSetMessageBodyPaddingForSender.assertValues(true);
    this.messageBodyTextViewBackgroundColorInt.assertValues(R.color.black);
    this.messageBodyTextViewTextColorInt.assertValues(R.color.white);
  }
}
