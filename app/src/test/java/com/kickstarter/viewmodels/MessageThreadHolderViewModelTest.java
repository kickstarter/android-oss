package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.MessageThreadFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.MessageThread;

import org.joda.time.DateTime;
import org.junit.Test;

import rx.observers.TestSubscriber;

public final class MessageThreadHolderViewModelTest extends KSRobolectricTestCase {
  private MessageThreadHolderViewModel.ViewModel vm;
  private final TestSubscriber<DateTime> dateDateTime = new TestSubscriber<>();
  private final TestSubscriber<String> messageBodyTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> participantAvatarUrl = new TestSubscriber<>();
  private final TestSubscriber<String> participantNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> unreadIndicatorImageViewHidden = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment env) {
    this.vm = new MessageThreadHolderViewModel.ViewModel(env);
    this.vm.outputs.dateDateTime().subscribe(dateDateTime);
    this.vm.outputs.messageBodyTextViewText().subscribe(messageBodyTextViewText);
    this.vm.outputs.participantAvatarUrl().subscribe(participantAvatarUrl);
    this.vm.outputs.participantNameTextViewText().subscribe(participantNameTextViewText);
    this.vm.outputs.unreadIndicatorViewHidden().subscribe(unreadIndicatorImageViewHidden);
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
  public void testUnreadIndicator() {
    final MessageThread messageThread = MessageThreadFactory.messageThread();
    setUpEnvironment(environment());

    final MessageThread messageThreadWithUnread = messageThread
      .toBuilder()
      .unreadMessagesCount(2)
      .build();

    final MessageThread messageThreadWithNoUnread = messageThread
      .toBuilder()
      .unreadMessagesCount(0)
      .build();

    // Configure the view model with a message thread with unreads.
    this.vm.inputs.configureWith(messageThreadWithUnread);
    this.unreadIndicatorImageViewHidden.assertValues(false);

    // Configure the view model with a message thread with no unreads.
    this.vm.inputs.configureWith(messageThreadWithNoUnread);
    this.unreadIndicatorImageViewHidden.assertValues(false, true);
  }
}
