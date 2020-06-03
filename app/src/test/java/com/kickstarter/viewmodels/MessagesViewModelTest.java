package com.kickstarter.viewmodels;

import android.content.Intent;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaContext;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.mock.factories.ApiExceptionFactory;
import com.kickstarter.mock.factories.BackingFactory;
import com.kickstarter.mock.factories.MessageFactory;
import com.kickstarter.mock.factories.MessageThreadEnvelopeFactory;
import com.kickstarter.mock.factories.MessageThreadFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.Backing;
import com.kickstarter.models.BackingWrapper;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.apiresponses.MessageThreadEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.data.MessageSubject;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.observers.TestSubscriber;

public final class MessagesViewModelTest extends KSRobolectricTestCase {
  private MessagesViewModel.ViewModel vm;
  private final TestSubscriber<Boolean> backButtonIsGone = new TestSubscriber<>();
  private final TestSubscriber<Pair<Backing, Project>> backingAndProject = new TestSubscriber<>();
  private final TestSubscriber<Boolean> backingInfoViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> closeButtonIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> creatorNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Void> goBack = new TestSubscriber<>();
  private final TestSubscriber<String> messageEditTextHint = new TestSubscriber<>();
  private final TestSubscriber<Void> messageEditTextShouldRequestFocus = new TestSubscriber<>();
  private final TestSubscriber<List<Message>> messageList = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameToolbarTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Void> recyclerViewDefaultBottomPadding = new TestSubscriber<>();
  private final TestSubscriber<Integer> recyclerViewInitialBottomPadding = new TestSubscriber<>();
  private final TestSubscriber<Void> scrollRecyclerViewToBottom = new TestSubscriber<>();
  private final TestSubscriber<Boolean> sendMessageButtonIsEnabled = new TestSubscriber<>();
  private final TestSubscriber<String> setMessageEditText = new TestSubscriber<>();
  private final TestSubscriber<String> showMessageErrorToast = new TestSubscriber<>();
  private final TestSubscriber<BackingWrapper> startBackingActivity = new TestSubscriber<>();
  private final TestSubscriber<Void> successfullyMarkedAsRead = new TestSubscriber<>();
  private final TestSubscriber<Boolean> toolbarIsExpanded = new TestSubscriber<>();
  private final TestSubscriber<Boolean> viewPledgeButtonIsGone = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new MessagesViewModel.ViewModel(environment);
    this.vm.outputs.backButtonIsGone().subscribe(this.backButtonIsGone);
    this.vm.outputs.backingAndProject().subscribe(this.backingAndProject);
    this.vm.outputs.backingInfoViewIsGone().subscribe(this.backingInfoViewIsGone);
    this.vm.outputs.closeButtonIsGone().subscribe(this.closeButtonIsGone);
    this.vm.outputs.goBack().subscribe(this.goBack);
    this.vm.outputs.messageEditTextHint().subscribe(this.messageEditTextHint);
    this.vm.outputs.messageEditTextShouldRequestFocus().subscribe(this.messageEditTextShouldRequestFocus);
    this.vm.outputs.messageList().subscribe(this.messageList);
    this.vm.outputs.creatorNameTextViewText().subscribe(this.creatorNameTextViewText);
    this.vm.outputs.projectNameTextViewText().subscribe(this.projectNameTextViewText);
    this.vm.outputs.projectNameToolbarTextViewText().subscribe(this.projectNameToolbarTextViewText);
    this.vm.outputs.recyclerViewDefaultBottomPadding().subscribe(this.recyclerViewDefaultBottomPadding);
    this.vm.outputs.recyclerViewInitialBottomPadding().subscribe(this.recyclerViewInitialBottomPadding);
    this.vm.outputs.scrollRecyclerViewToBottom().subscribe(this.scrollRecyclerViewToBottom);
    this.vm.outputs.sendMessageButtonIsEnabled().subscribe(this.sendMessageButtonIsEnabled);
    this.vm.outputs.setMessageEditText().subscribe(this.setMessageEditText);
    this.vm.outputs.showMessageErrorToast().subscribe(this.showMessageErrorToast);
    this.vm.outputs.startBackingActivity().subscribe(this.startBackingActivity);
    this.vm.outputs.successfullyMarkedAsRead().subscribe(this.successfullyMarkedAsRead);
    this.vm.outputs.toolbarIsExpanded().subscribe(this.toolbarIsExpanded);
    this.vm.outputs.viewPledgeButtonIsGone().subscribe(this.viewPledgeButtonIsGone);
  }

  @Test
  public void testBackButton_IsGone() {
    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build());
    this.vm.intent(messagesContextIntent(MessageThreadFactory.messageThread()));

    // Back button is gone if navigating from non-backer modal view.
    this.backButtonIsGone.assertValues(true);
    this.closeButtonIsGone.assertValues(false);
  }

  @Test
  public void testBackButton_IsVisible() {
    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build());
    this.vm.intent(backerModalContextIntent(BackingFactory.backing(), ProjectFactory.project()));

    // Back button is visible if navigating from backer modal view.
    this.backButtonIsGone.assertValues(false);
    this.closeButtonIsGone.assertValues(true);
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
    final Project project = ProjectFactory.project().toBuilder()
      .isBacking(false)
      .build();

    final MessageThread messageThread = MessageThreadFactory.messageThread().toBuilder()
      .project(project)
      .backing(null)
      .build();

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user) {
        return Observable.error(ApiExceptionFactory.badRequestException());
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    // Start the view model with a message thread.
    this.vm.intent(messagesContextIntent(messageThread));

    this.backingAndProject.assertNoValues();
    this.backingInfoViewIsGone.assertValues(true);
  }

  @Test
  public void testConfiguredWithThread() {
    final MessageThread messageThread = MessageThreadFactory.messageThread();
    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build());

    // Start the view model with a message thread.
    this.vm.intent(messagesContextIntent(messageThread));

    this.backingAndProject.assertValueCount(1);
    this.messageList.assertValueCount(1);
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
    this.messageList.assertValueCount(1);
    this.koalaTest.assertValues(KoalaEvent.VIEWED_MESSAGE_THREAD);
  }

  @Test
  public void testCreatorViewingProjectMessages() {
    final User creator = UserFactory.creator().toBuilder().name("Sharon").build();
    final User participant = UserFactory.user().toBuilder().name("Timothy").build();
    final CurrentUserType currentUser = new MockCurrentUser(creator);

    final MessageThread messageThread = MessageThreadFactory.messageThread()
      .toBuilder()
      .project(ProjectFactory.project().toBuilder().creator(creator).build())
      .participant(participant)
      .build();

    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForThread(final @NonNull MessageThread thread) {
        return Observable.just(
          MessageThreadEnvelopeFactory.messageThreadEnvelope().toBuilder().messageThread(messageThread).build()
        );
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(currentUser).build()
    );

    // Start the view model with a message thread.
    this.vm.intent(messagesContextIntent(messageThread));

    // Creator name is the project creator, edit text hint is always the participant.
    this.creatorNameTextViewText.assertValues(creator.name());
    this.messageEditTextHint.assertValues(participant.name());
  }

  @Test
  public void testGoBack() {
    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build());
    this.vm.intent(messagesContextIntent(MessageThreadFactory.messageThread()));
    this.vm.inputs.backOrCloseButtonClicked();
    this.goBack.assertValueCount(1);
  }

  @Test
  public void testProjectData_ExistingMessages() {
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

    this.creatorNameTextViewText.assertValues(messageThread.project().creator().name());
    this.projectNameTextViewText.assertValues(messageThread.project().name());
    this.projectNameToolbarTextViewText.assertValues(messageThread.project().name());
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

    // Messages emit, keyboard not shown.
    this.messageList.assertValueCount(1);
    this.messageEditTextShouldRequestFocus.assertNoValues();
  }

  @Test
  public void testNoMessages() {
    final Backing backing = BackingFactory.backing();
    final Project project = ProjectFactory.project();

    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForBacking(final @NonNull Backing backing) {
        return Observable.just(MessageThreadEnvelopeFactory.empty());
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    // Start the view model with a backing and a project.
    this.vm.intent(backerModalContextIntent(backing, project));

    // All data except for messages should emit.
    this.messageList.assertNoValues();
    this.creatorNameTextViewText.assertValues(project.creator().name());
    this.backingAndProject.assertValues(Pair.create(backing, project));
  }

  @Test
  public void testRecyclerViewBottomPadding() {
    final int appBarTotalScrolLRange = 327;

    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build());

    // Start the view model with a message thread.
    this.vm.intent(messagesContextIntent(MessageThreadFactory.messageThread()));

    // View initially loaded with a 0 (expanded) offset.
    this.vm.inputs.appBarOffset(0);
    this.vm.inputs.appBarTotalScrollRange(appBarTotalScrolLRange);

    // Only initial bottom padding emits.
    this.recyclerViewDefaultBottomPadding.assertNoValues();
    this.recyclerViewInitialBottomPadding.assertValues(appBarTotalScrolLRange);

    // User scrolls.
    this.vm.inputs.appBarOffset(-30);
    this.vm.inputs.appBarTotalScrollRange(appBarTotalScrolLRange);

    // Default padding emits, initial padding does not emit again.
    this.recyclerViewDefaultBottomPadding.assertValueCount(1);
    this.recyclerViewInitialBottomPadding.assertValues(appBarTotalScrolLRange);

    // User scrolls.
    this.vm.inputs.appBarOffset(20);
    this.vm.inputs.appBarTotalScrollRange(appBarTotalScrolLRange);

    // Padding does not change.
    this.recyclerViewDefaultBottomPadding.assertValueCount(1);
    this.recyclerViewInitialBottomPadding.assertValues(appBarTotalScrolLRange);
  }

  @Test
  public void testSendMessage_Error() {
    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<Message> sendMessage(final @NonNull MessageSubject messageSubject, final @NonNull String body) {
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

    // Error toast is displayed, errored message body remains in edit text, no new message is emitted.
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
      public @NonNull Observable<Message> sendMessage(final @NonNull MessageSubject messageSubject, final @NonNull String body) {
        return Observable.just(sentMessage);
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    // Start the view model with a message thread.
    this.vm.intent(messagesContextIntent(MessageThreadFactory.messageThread()));

    // Initial messages emit.
    this.messageList.assertValueCount(1);

    // Send a message successfully.
    this.vm.inputs.messageEditTextChanged("Salutations friend!");
    this.vm.inputs.sendMessageButtonClicked();

    // New message list emits.
    this.messageList.assertValueCount(2);

    // Reply edit text should be cleared and view should be scrolled to new message.
    this.setMessageEditText.assertValues("");
    this.scrollRecyclerViewToBottom.assertValueCount(1);

    this.koalaTest.assertValues(KoalaEvent.VIEWED_MESSAGE_THREAD, KoalaEvent.SENT_MESSAGE);
  }

  @Test
  public void testSendMessageButtonIsEnabled() {
    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build());
    this.vm.intent(messagesContextIntent(MessageThreadFactory.messageThread()));

    this.sendMessageButtonIsEnabled.assertNoValues();

    this.vm.inputs.messageEditTextChanged("hello");
    this.sendMessageButtonIsEnabled.assertValues(true);

    this.vm.inputs.messageEditTextChanged("");
    this.sendMessageButtonIsEnabled.assertValues(true, false);
  }

  @Test
  public void testShouldRequestFocus() {
    final Backing backing = BackingFactory.backing();

    final MessageThreadEnvelope envelope = MessageThreadEnvelopeFactory.messageThreadEnvelope()
      .toBuilder()
      .messages(null)
      .build();

    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForBacking(final @NonNull Backing backing) {
        return Observable.just(envelope);
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    // Start the view model with a backing and project.
    this.vm.intent(backerModalContextIntent(backing, ProjectFactory.project()));

    this.messageEditTextShouldRequestFocus.assertValueCount(1);
  }

  @Test
  public void testStartBackingActivity_AsBacker() {
    final User user = UserFactory.user();
    final Project project = ProjectFactory.project().toBuilder().isBacking(true).build();
    final Backing backing = BackingFactory.backing();

    final MessageThread messageThread = MessageThreadFactory.messageThread()
      .toBuilder()
      .project(project)
      .build();

    final MessageThreadEnvelope messageThreadEnvelope = MessageThreadEnvelopeFactory.messageThreadEnvelope()
      .toBuilder()
      .messageThread(messageThread)
      .build();

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForBacking(final @NonNull Backing backing) {
        return Observable.just(messageThreadEnvelope);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(user)).build());

    this.vm.intent(backerModalContextIntent(backing, project));
    this.vm.inputs.viewPledgeButtonClicked();

    this.startBackingActivity.assertValues(new BackingWrapper(backing, user, project));
  }

  @Test
  public void testStartBackingActivity_AsBacker_EmptyThread() {
    final User user = UserFactory.user();
    final Project project = ProjectFactory.project().toBuilder().isBacking(true).build();
    final Backing backing = BackingFactory.backing();

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForBacking(final @NonNull Backing backing) {
        return Observable.just(MessageThreadEnvelopeFactory.empty());
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(user)).build());

    this.vm.intent(creatorBioModalContextIntent(backing, project));
    this.vm.inputs.viewPledgeButtonClicked();

    this.startBackingActivity.assertValues(new BackingWrapper(backing, user, project));
  }

  @Test
  public void testStartBackingActivity_AsCreator() {
    final User backer = UserFactory.user().toBuilder().name("Vanessa").build();
    final User creator = UserFactory.user().toBuilder().name("Jessica").build();
    final Backing backing = BackingFactory.backing();
    final Project project = ProjectFactory.project().toBuilder().creator(creator).build();

    final MessageThread messageThread = MessageThreadFactory.messageThread()
      .toBuilder()
      .backing(backing)
      .participant(backer)
      .project(project)
      .build();

    final MessageThreadEnvelope messageThreadEnvelope = MessageThreadEnvelopeFactory.messageThreadEnvelope()
      .toBuilder()
      .messageThread(messageThread)
      .build();

    final MockApiClient apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForThread(final @NonNull MessageThread messageThread) {
        return Observable.just(messageThreadEnvelope);
      }

      @NonNull
      @Override
      public Observable<Backing> fetchProjectBacking(@NonNull final Project project, @NonNull final User user) {
        return Observable.just(backing);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(creator)).build());

    this.vm.intent(messagesContextIntent(messageThread));
    this.vm.inputs.viewPledgeButtonClicked();

    this.startBackingActivity.assertValues(new BackingWrapper(messageThread.backing(), backer, project));
  }

  @Test
  public void testSuccessfullyMarkedAsRead() {
    final MessageThread messageThread = MessageThreadFactory.messageThread();

    final MockApiClient apiClient = new MockApiClient() {
      @NonNull
      @Override
      public Observable<MessageThread> markAsRead(final @NonNull MessageThread thread) {
        return Observable.just(messageThread);
      }
    };

    setUpEnvironment(
      environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).apiClient(apiClient).build()
    );

    this.vm.intent(messagesContextIntent(messageThread));

    this.successfullyMarkedAsRead.assertValueCount(1);
  }

  @Test
  public void testToolbarIsExpanded_NoMessages() {
    final Backing backing = BackingFactory.backing();

    final MessageThreadEnvelope envelope = MessageThreadEnvelopeFactory.messageThreadEnvelope()
      .toBuilder()
      .messages(null)
      .build();

    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForBacking(final @NonNull Backing backing) {
        return Observable.just(envelope);
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    // Start the view model with a backing and project.
    this.vm.intent(backerModalContextIntent(backing, ProjectFactory.project()));
    this.vm.inputs.messageEditTextIsFocused(true);

    // Toolbar stays expanded when keyboard opens and no messages.
    this.toolbarIsExpanded.assertNoValues();
  }

  @Test
  public void testToolbarIsExpanded_WithMessages() {
    final Backing backing = BackingFactory.backing();

    final MessageThreadEnvelope envelope = MessageThreadEnvelopeFactory.messageThreadEnvelope()
      .toBuilder()
      .messages(Collections.singletonList(MessageFactory.message()))
      .build();

    final MockApiClient apiClient = new MockApiClient() {
      @Override public @NonNull Observable<MessageThreadEnvelope> fetchMessagesForBacking(final @NonNull Backing backing) {
        return Observable.just(envelope);
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(new MockCurrentUser(UserFactory.user())).build()
    );

    // Start the view model with a backing and project.
    this.vm.intent(backerModalContextIntent(backing, ProjectFactory.project()));
    this.vm.inputs.messageEditTextIsFocused(true);

    // Toolbar collapsed when keyboard opens and there are messages.
    this.toolbarIsExpanded.assertValues(false);
  }

  @Test
  public void testViewMessages_FromPush() {
    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build());
    this.vm.intent(pushContextIntent());

    this.backButtonIsGone.assertValues(true);
    this.closeButtonIsGone.assertValues(false);
    this.viewPledgeButtonIsGone.assertValues(false);
  }

  @Test
  public void testViewPledgeButton_IsGone_backerModal() {
    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build());
    this.vm.intent(backerModalContextIntent(BackingFactory.backing(), ProjectFactory.project()));

    // View pledge button is hidden when context is from the backer modal.
    this.viewPledgeButtonIsGone.assertValues(true);
  }

  @Test
  public void testViewPledgeButton_IsVisible_creatorBioModal() {
    setUpEnvironment(environment().toBuilder().currentUser(new MockCurrentUser(UserFactory.user())).build());
    this.vm.intent(creatorBioModalContextIntent(BackingFactory.backing(), ProjectFactory.project()));

    // View pledge button is shown when context is from the creator bio modal.
    this.viewPledgeButtonIsGone.assertValues(false);
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

  private static @NonNull Intent creatorBioModalContextIntent(final @NonNull Backing backing, final @NonNull Project project) {
    return new Intent()
      .putExtra(IntentKey.BACKING, backing)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Message.CREATOR_BIO_MODAL);
  }

  private static @NonNull Intent messagesContextIntent(final @NonNull MessageThread messageThread) {
    return new Intent()
      .putExtra(IntentKey.MESSAGE_THREAD, messageThread)
      .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Message.MESSAGES);
  }

  private static @NonNull Intent pushContextIntent() {
    return messagesContextIntent(MessageThreadFactory.messageThread())
      .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Message.PUSH);
  }
}
