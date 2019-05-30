package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.mock.factories.MessageThreadFactory;
import com.kickstarter.models.MessageThread;

import org.joda.time.DateTime;
import org.junit.Test;

import androidx.annotation.NonNull;
import rx.observers.TestSubscriber;

public final class MessageThreadHolderViewModelTest extends KSRobolectricTestCase {
  private MessageThreadHolderViewModel.ViewModel vm;
  private final TestSubscriber<Boolean> cardViewIsElevated = new TestSubscriber<>();
  private final TestSubscriber<DateTime> dateDateTime = new TestSubscriber<>();
  private final TestSubscriber<Boolean> dateTextViewIsBold = new TestSubscriber<>();
  private final TestSubscriber<Boolean> messageBodyTextIsBold = new TestSubscriber<>();
  private final TestSubscriber<String> messageBodyTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> participantAvatarUrl = new TestSubscriber<>();
  private final TestSubscriber<Boolean> participantNameTextViewIsBold = new TestSubscriber<>();
  private final TestSubscriber<String> participantNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<MessageThread> startMessagesActivity = new TestSubscriber<>();
  private final TestSubscriber<Boolean> unreadCountTextViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> unreadCountTextViewText = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment env) {
    this.vm = new MessageThreadHolderViewModel.ViewModel(env);

    this.vm.outputs.cardViewIsElevated().subscribe(this.cardViewIsElevated);
    this.vm.outputs.dateDateTime().subscribe(this.dateDateTime);
    this.vm.outputs.dateTextViewIsBold().subscribe(this.dateTextViewIsBold);
    this.vm.outputs.messageBodyTextIsBold().subscribe(this.messageBodyTextIsBold);
    this.vm.outputs.messageBodyTextViewText().subscribe(this.messageBodyTextViewText);
    this.vm.outputs.participantAvatarUrl().subscribe(this.participantAvatarUrl);
    this.vm.outputs.participantNameTextViewIsBold().subscribe(this.participantNameTextViewIsBold);
    this.vm.outputs.participantNameTextViewText().subscribe(this.participantNameTextViewText);
    this.vm.outputs.startMessagesActivity().subscribe(this.startMessagesActivity);
    this.vm.outputs.unreadCountTextViewIsGone().subscribe(this.unreadCountTextViewIsGone);
    this.vm.outputs.unreadCountTextViewText().subscribe(this.unreadCountTextViewText);
  }

  @Test
  public void testEmitsDateTime() {
    final MessageThread messageThread = MessageThreadFactory.messageThread();
    setUpEnvironment(environment());

    // Configure the view model with a message thread.
    this.vm.inputs.configureWith(messageThread);

    this.dateDateTime.assertValues(messageThread.lastMessage().createdAt());
  }

  @Test
  public void testEmitsMessageBodyTextViewText() {
    final MessageThread messageThread = MessageThreadFactory.messageThread();
    setUpEnvironment(environment());

    // Configure the view model with a message thread.
    this.vm.inputs.configureWith(messageThread);

    this.messageBodyTextViewText.assertValues(messageThread.lastMessage().body());
  }

  @Test
  public void testEmitsParticipantData() {
    final MessageThread messageThread = MessageThreadFactory.messageThread();
    setUpEnvironment(environment());

    // Configure the view model with a message thread.
    this.vm.inputs.configureWith(messageThread);

    // Emits participant's avatar url and name.
    this.participantAvatarUrl.assertValues(messageThread.participant().avatar().medium());
    this.participantNameTextViewText.assertValues(messageThread.participant().name());
  }

  @Test
  public void testMessageThread_Clicked() {
    final MessageThread messageThread = MessageThreadFactory.messageThread()
      .toBuilder()
      .id(12345)
      .unreadMessagesCount(1)
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(messageThread);
    this.cardViewIsElevated.assertValues(true);
    this.dateTextViewIsBold.assertValues(true);
    this.messageBodyTextIsBold.assertValues(true);
    this.unreadCountTextViewIsGone.assertValues(false);

    this.vm.inputs.messageThreadCardViewClicked();
    this.cardViewIsElevated.assertValues(true, false);
    this.dateTextViewIsBold.assertValues(true, false);
    this.messageBodyTextIsBold.assertValues(true, false);
    this.unreadCountTextViewIsGone.assertValues(false, true);
  }

  @Test
  public void testMessageThread_HasNoUnreadMessages() {
    final MessageThread messageThreadWithNoUnread = MessageThreadFactory.messageThread()
      .toBuilder()
      .unreadMessagesCount(0)
      .build();

    setUpEnvironment(environment());

    // Configure the view model with a message thread with no unread messages.
    this.vm.inputs.configureWith(messageThreadWithNoUnread);

    this.dateTextViewIsBold.assertValues(false);
    this.messageBodyTextIsBold.assertValues(false);
    this.participantNameTextViewIsBold.assertValues(false);
    this.unreadCountTextViewIsGone.assertValues(true);
    this.unreadCountTextViewText.assertValues(NumberUtils.format(messageThreadWithNoUnread.unreadMessagesCount()));
  }

  @Test
  public void testMessageThread_HasUnreadMessages() {
    final MessageThread messageThreadWithUnread = MessageThreadFactory.messageThread()
      .toBuilder()
      .unreadMessagesCount(2)
      .build();

    setUpEnvironment(environment());

    // Configure the view model with a message thread with unread messages.
    this.vm.inputs.configureWith(messageThreadWithUnread);

    this.dateTextViewIsBold.assertValues(true);
    this.messageBodyTextIsBold.assertValues(true);
    this.participantNameTextViewIsBold.assertValues(true);
    this.unreadCountTextViewIsGone.assertValues(false);
    this.unreadCountTextViewText.assertValues(NumberUtils.format(messageThreadWithUnread.unreadMessagesCount()));
  }

  @Test
  public void testStartMessagesActivity() {
    final MessageThread messageThread = MessageThreadFactory.messageThread();
    setUpEnvironment(environment());

    // Configure the view model with a message thread.
    this.vm.inputs.configureWith(messageThread);
    this.vm.inputs.messageThreadCardViewClicked();

    this.startMessagesActivity.assertValues(messageThread);
  }
}
