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
  private final MessageThread defaultMessageThread = MessageThreadFactory.messageThread();
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
    this.vm.outputs.unreadIndicatorImageViewHidden().subscribe(unreadIndicatorImageViewHidden);
  }

  @Test
  public void testEmitsDateTime() {
    setUpEnvironment(environment());

    // Configure the view model with a message thread.
    this.vm.inputs.configureWith(this.defaultMessageThread);

    this.dateDateTime.assertValues(this.defaultMessageThread.lastMessage().createdAt());
  }

  @Test
  public void testEmitsMessageBodyTextViewText() {
    setUpEnvironment(environment());

    // Configure the view model with a message thread.
    this.vm.inputs.configureWith(this.defaultMessageThread);

    this.messageBodyTextViewText.assertValues(this.defaultMessageThread.lastMessage().body());
  }

  @Test
  public void testEmitsParticipantData() {
    setUpEnvironment(environment());

    // Configure the view model with a message thread.
    this.vm.inputs.configureWith(this.defaultMessageThread);

    // Emits participant's avatar url and name.
    this.participantAvatarUrl.assertValues(this.defaultMessageThread.participant().avatar().medium());
    this.participantNameTextViewText.assertValues(this.defaultMessageThread.participant().name());
  }

  @Test
  public void testUnreadIndicator() {
    setUpEnvironment(environment());

    final MessageThread messageThreadWithUnread = this.defaultMessageThread
      .toBuilder()
      .unreadMessagesCount(2)
      .build();

    final MessageThread messageThreadWithNoUnread = this.defaultMessageThread
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
