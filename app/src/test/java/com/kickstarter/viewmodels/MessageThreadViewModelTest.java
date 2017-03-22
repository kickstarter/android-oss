package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.MessageThreadFactory;
import com.kickstarter.models.MessageThread;

import org.joda.time.DateTime;
import org.junit.Test;

import rx.observers.TestSubscriber;

public final class MessageThreadViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testEmitsDateTime() {
    final MessageThreadViewModel.ViewModel vm = new MessageThreadViewModel.ViewModel(environment());
    final MessageThread messageThread = MessageThreadFactory.messageThread();

    final TestSubscriber<DateTime> dateDateTime = new TestSubscriber<>();
    vm.outputs.dateDateTime().subscribe(dateDateTime);

    // Configure the view model with a message thread.
    vm.inputs.configureWith(messageThread);

    dateDateTime.assertValues(messageThread.lastMessage().createdAt());
  }

  @Test
  public void testEmitsMessageBodyTextViewText() {
    final MessageThreadViewModel.ViewModel vm = new MessageThreadViewModel.ViewModel(environment());
    final MessageThread messageThread = MessageThreadFactory.messageThread();

    final TestSubscriber<String> messageBodyTextViewText = new TestSubscriber<>();
    vm.outputs.messageBodyTextViewText().subscribe(messageBodyTextViewText);

    // Configure the view model with a message thread.
    vm.inputs.configureWith(messageThread);

    messageBodyTextViewText.assertValues(messageThread.lastMessage().body());
  }

  @Test
  public void testEmitsParticipantData() {
    final MessageThreadViewModel.ViewModel vm = new MessageThreadViewModel.ViewModel(environment());
    final MessageThread messageThread = MessageThreadFactory.messageThread();

    final TestSubscriber<String> participantAvatarUrl = new TestSubscriber<>();
    vm.outputs.participantAvatarUrl().subscribe(participantAvatarUrl);

    final TestSubscriber<String> participantNameTextViewText = new TestSubscriber<>();
    vm.outputs.participantNameTextViewText().subscribe(participantNameTextViewText);

    // Configure the view model with a message thread.
    vm.inputs.configureWith(messageThread);

    // Emits participant's avatar url and name.
    participantAvatarUrl.assertValues(messageThread.participant().avatar().medium());
    participantNameTextViewText.assertValues(messageThread.participant().name());
  }
}
