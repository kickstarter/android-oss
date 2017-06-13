package com.kickstarter.viewmodels;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ApiExceptionFactory;
import com.kickstarter.factories.BackingFactory;
import com.kickstarter.factories.MessageFactory;
import com.kickstarter.factories.MessageThreadEnvelopeFactory;
import com.kickstarter.factories.MessageThreadFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaContext;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.services.apiresponses.MessageThreadEnvelope;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

public final class MessagesViewModelTest extends KSRobolectricTestCase {
  private MessagesViewModel.ViewModel vm;
  private final TestSubscriber<Pair<Backing, Project>> backingAndProject = new TestSubscriber<>();
  private final TestSubscriber<Boolean> backingInfoViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> messageEditTextHint = new TestSubscriber<>();
  private final TestSubscriber<List<Message>> messages = new TestSubscriber<>();
  private final TestSubscriber<String> participantNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> setMessageEditText = new TestSubscriber<>();
  private final TestSubscriber<String> showMessageErrorToast = new TestSubscriber<>();
  private final TestSubscriber<Boolean> viewPledgeButtonIsGone = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new MessagesViewModel.ViewModel(environment);

    this.vm.outputs.backingAndProject().subscribe(this.backingAndProject);
    this.vm.outputs.backingInfoViewIsGone().subscribe(this.backingInfoViewIsGone);
    this.vm.outputs.messageEditTextHint().subscribe(this.messageEditTextHint);
    this.vm.outputs.messages().subscribe(this.messages);
    this.vm.outputs.participantNameTextViewText().subscribe(this.participantNameTextViewText);
    this.vm.outputs.projectNameTextViewText().subscribe(this.projectNameTextViewText);
    this.vm.outputs.setMessageEditText().subscribe(this.setMessageEditText);
    this.vm.outputs.showMessageErrorToast().subscribe(this.showMessageErrorToast);
    this.vm.outputs.viewPledgeButtonIsGone().subscribe(this.viewPledgeButtonIsGone);
  }

  @Test
  public void testBackingAndProject_Participant() {
    final Project project = ProjectFactory.project().toBuilder()
      .isBacking(false)
      .build();

    final Backing backing = BackingFactory.backing().toBuilder()
      .project(project)
      .build();

    final MessageThread messageThread = MessageThreadFactory.messageThread().toBuilder()
      .project(project)
      .backing(backing)
      .build();

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForThread(final @NonNull MessageThread messageThread) {
        return Observable.just(MessageThreadEnvelopeFactory.messageThreadEnvelope());
      }

      @Override
      public @NonNull Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user) {
        return Observable.just(backing);
      }
    };

    setUpEnvironment(
      environment().toBuilder()
        .apiClient(apiClient)
        .currentUser(new MockCurrentUser(UserFactory.user()))
        .build()
    );

    // Start the view model with a message thread.
    this.vm.intent(messagesContextIntent(messageThread));

    this.backingAndProject.assertValues(Pair.create(backing, backing.project()));
    this.backingInfoViewIsGone.assertValues(false);
  }

  @Test
  public void testBackingInfo_NoBacking() {
    final MessageThread messageThread = MessageThreadFactory.messageThread().toBuilder().backing(null).build();

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user) {
        return Observable.just(null);
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    // Start the view model with a message thread.
    this.vm.intent(messagesContextIntent(messageThread));

    this.backingAndProject.assertValues(Pair.create(null, messageThread.project()));
    this.backingInfoViewIsGone.assertValues(true);
  }

  @Test
  public void testConfiguredWithThread() {
    final MessageThread messageThread = MessageThreadFactory.messageThread();
    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build());

    // Start the view model with a message thread.
    this.vm.intent(messagesContextIntent(messageThread));

    this.backingAndProject.assertValueCount(1);
    this.messages.assertValueCount(1);
    this.koalaTest.assertValues(KoalaEvent.VIEWED_MESSAGE_THREAD);
  }

  @Test
  public void testConfiguredWithProject_AndBacking() {
    final Backing backing = BackingFactory.backing();
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build());

    // Start the view model with a backing and a project.
    this.vm.intent(backerModalContextIntent(backing, project));

    this.backingAndProject.assertValueCount(1);
    this.messages.assertValueCount(1);
    this.koalaTest.assertValues(KoalaEvent.VIEWED_MESSAGE_THREAD);
  }

  @Test
  public void testProjectData() {
    final MessageThread messageThread = MessageThreadFactory.messageThread();

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForThread(final @NonNull MessageThread thread) {
        return Observable.just(MessageThreadEnvelopeFactory.messageThreadEnvelope());
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    // Start the view model with a message thread.
    this.vm.intent(messagesContextIntent(messageThread));

    this.participantNameTextViewText.assertValues(messageThread.project().creator().name());
    this.projectNameTextViewText.assertValues(messageThread.project().name());
  }

  @Test
  public void testMessageEditTextHint() {
    final MessageThread messageThread = MessageThreadFactory.messageThread();

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForThread(final @NonNull MessageThread thread) {
        return Observable.just(MessageThreadEnvelopeFactory.messageThreadEnvelope());
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    // Start the view model with a message thread.
    this.vm.intent(messagesContextIntent(messageThread));

    this.messageEditTextHint.assertValues(messageThread.project().creator().name());
  }

  @Test
  public void testMessagesEmit() {
    final MessageThreadEnvelope envelope = MessageThreadEnvelopeFactory.messageThreadEnvelope()
      .toBuilder()
      .messages(Collections.singletonList(MessageFactory.message()))
      .build();

    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForThread(final @NonNull MessageThread messageThread) {
        return Observable.just(envelope);
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    // Start the view model with a message thread.
    this.vm.intent(messagesContextIntent(MessageThreadFactory.messageThread()));

    // Messages emit.
    this.messages.assertValueCount(1);
  }

  @Test
  public void testNoMessages() {
    final Backing backing = BackingFactory.backing();
    final Project project = ProjectFactory.project();

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForBacking(final @NonNull Backing backing) {
        return Observable.empty();
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    // Start the view model with a backing and a project.
    this.vm.intent(backerModalContextIntent(backing, project));

    // All data except for messages should emit.
    this.messages.assertNoValues();
    this.participantNameTextViewText.assertValues(project.creator().name());
    this.backingAndProject.assertValues(Pair.create(backing, project));
  }

  @Test
  public void testSendMessage_Error() {
    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<Message> sendMessageToThread(final @NonNull MessageThread thread, final @NonNull String body) {
        return Observable.error(ApiExceptionFactory.badRequestException());
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    // Start the view model with a message thread.
    this.vm.intent(messagesContextIntent(MessageThreadFactory.messageThread()));

    // Send a message unsuccessfully.
    this.vm.inputs.messageEditTextChanged("Hello there");
    this.vm.inputs.sendMessageButtonClicked();

    // Error toast is displayed, errored message body remains in edit text.
    this.showMessageErrorToast.assertValueCount(1);
    this.setMessageEditText.assertNoValues();

    // No sent message event tracked.
    this.koalaTest.assertValues(KoalaEvent.VIEWED_MESSAGE_THREAD);
  }

  @Test
  public void testSendMessage_Success() {
    final Message sentMessage = MessageFactory.message();

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<Message> sendMessageToThread(final @NonNull MessageThread thread, final @NonNull String body) {
        return Observable.just(sentMessage);
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    // Start the view model with a message thread.
    this.vm.intent(messagesContextIntent(MessageThreadFactory.messageThread()));

    // Send a message successfully.
    this.vm.inputs.messageEditTextChanged("Salutations friend!");
    this.vm.inputs.sendMessageButtonClicked();

    // Reply edit text should be cleared.
    this.setMessageEditText.assertValues("");

    this.koalaTest.assertValues(KoalaEvent.VIEWED_MESSAGE_THREAD, KoalaEvent.SENT_MESSAGE);
  }

  @Test
  public void testViewPledgeButton_IsGone() {
    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build());
    this.vm.intent(backerModalContextIntent(BackingFactory.backing(), ProjectFactory.project()));

    // View pledge button is hidden when context is from the backer modal.
    this.viewPledgeButtonIsGone.assertValues(true);
  }

  @Test
  public void testViewPledgeButton_IsVisible() {
    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build());
    this.vm.intent(messagesContextIntent(MessageThreadFactory.messageThread()));

    // View pledge button is shown when context is from anywhere but the backer modal.
    this.viewPledgeButtonIsGone.assertValues(false);
  }

  private static @NonNull Intent backerModalContextIntent(final @NonNull Backing backing, final @NonNull Project project) {
    return new Intent()
      .putExtra(IntentKey.BACKING, backing)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Message.BACKER_MODAL);
  }

  private static @NonNull Intent messagesContextIntent(final @NonNull MessageThread messageThread) {
    return new Intent()
      .putExtra(IntentKey.MESSAGE_THREAD, messageThread)
      .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Message.MESSAGES);
  }
}
