package com.kickstarter.viewmodels;

import android.content.Intent;
import android.graphics.Typeface;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaContext;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.mock.factories.MessageThreadFactory;
import com.kickstarter.mock.factories.MessageThreadsEnvelopeFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.Empty;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.data.Mailbox;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rx.Observable;
import rx.observers.TestSubscriber;

public class MessageThreadsViewModelTest extends KSRobolectricTestCase {
  private MessageThreadsViewModel.ViewModel vm;
  private final TestSubscriber<Boolean> hasNoMessages = new TestSubscriber<>();
  private final TestSubscriber<Boolean> hasNoUnreadMessages = new TestSubscriber<>();
  private final TestSubscriber<Integer> mailboxTitle = new TestSubscriber<>();
  private final TestSubscriber<List<MessageThread>> messageThreadList = new TestSubscriber<>();
  private final TestSubscriber<Integer> messageThreadListCount = new TestSubscriber<>();
  private final TestSubscriber<Integer> unreadCountTextViewColorInt = new TestSubscriber<>();
  private final TestSubscriber<Integer> unreadCountTextViewTypefaceInt = new TestSubscriber<>();
  private final TestSubscriber<Boolean> unreadCountToolbarTextViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<Integer> unreadMessagesCount = new TestSubscriber<>();
  private final TestSubscriber<Boolean> unreadMessagesCountIsGone = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment env) {
    this.vm = new MessageThreadsViewModel.ViewModel(env);
    this.vm.outputs.hasNoMessages().subscribe(this.hasNoMessages);
    this.vm.outputs.hasNoUnreadMessages().subscribe(this.hasNoUnreadMessages);
    this.vm.outputs.mailboxTitle().subscribe(this.mailboxTitle);
    this.vm.outputs.messageThreadList().subscribe(this.messageThreadList);
    this.vm.outputs.messageThreadList().map(List::size).subscribe(this.messageThreadListCount);
    this.vm.outputs.unreadCountTextViewColorInt().subscribe(this.unreadCountTextViewColorInt);
    this.vm.outputs.unreadCountTextViewTypefaceInt().subscribe(this.unreadCountTextViewTypefaceInt);
    this.vm.outputs.unreadCountToolbarTextViewIsGone().subscribe(this.unreadCountToolbarTextViewIsGone);
    this.vm.outputs.unreadMessagesCount().subscribe(this.unreadMessagesCount);
    this.vm.outputs.unreadMessagesCountIsGone().subscribe(this.unreadMessagesCountIsGone);
  }

  @Test
  public void testMessageThreadsEmit_NoProjectIntent() {
    final CurrentUserType currentUser = new MockCurrentUser();
    currentUser.login(UserFactory.user().toBuilder().unreadMessagesCount(0).build(), "beefbod5");

    final MessageThreadsEnvelope inboxEnvelope = MessageThreadsEnvelopeFactory.messageThreadsEnvelope()
      .toBuilder()
      .messageThreads(Collections.singletonList(MessageThreadFactory.messageThread()))
      .build();

    final MessageThreadsEnvelope sentEnvelope = MessageThreadsEnvelopeFactory.messageThreadsEnvelope()
      .toBuilder()
      .messageThreads(Arrays.asList(MessageThreadFactory.messageThread(), MessageThreadFactory.messageThread()))
      .build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<MessageThreadsEnvelope> fetchMessageThreads(final @Nullable Project project,
        final @NonNull Mailbox mailbox) {
        return  Observable.just(mailbox == Mailbox.INBOX ? inboxEnvelope : sentEnvelope);
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(currentUser).build()
    );

    final Intent intent = new Intent().putExtra(IntentKey.PROJECT, Empty.INSTANCE)
      .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Mailbox.PROFILE);
    this.vm.intent(intent);
    this.messageThreadList.assertValueCount(2);
    this.messageThreadListCount.assertValues(0, 1);

    // Same message threads should not emit again.
    this.vm.inputs.onResume();
    this.messageThreadList.assertValueCount(2);
    this.messageThreadListCount.assertValues(0, 1);

    this.vm.inputs.mailbox(Mailbox.SENT);
    this.messageThreadList.assertValueCount(4);
    this.messageThreadListCount.assertValues(0, 1, 0, 2);
  }

  @Test
  public void testMessageThreadsEmit_WithProjectIntent() {
    final CurrentUserType currentUser = new MockCurrentUser();
    currentUser.login(UserFactory.user().toBuilder().unreadMessagesCount(0).build(), "beefbod5");

    final MessageThreadsEnvelope inboxEnvelope = MessageThreadsEnvelopeFactory.messageThreadsEnvelope()
      .toBuilder()
      .messageThreads(Collections.singletonList(MessageThreadFactory.messageThread()))
      .build();

    final MessageThreadsEnvelope sentEnvelope = MessageThreadsEnvelopeFactory.messageThreadsEnvelope()
      .toBuilder()
      .messageThreads(Arrays.asList(MessageThreadFactory.messageThread(), MessageThreadFactory.messageThread()))
      .build();

    final Project project = ProjectFactory.project().toBuilder().unreadMessagesCount(5).build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<MessageThreadsEnvelope> fetchMessageThreads(final @Nullable Project project,
        final @NonNull Mailbox mailbox) {
        return  Observable.just(mailbox == Mailbox.INBOX ? inboxEnvelope : sentEnvelope);
      }
      @Override public @NonNull Observable<Project> fetchProject(final @NonNull String param) {
        return Observable.just(project);
      }
    };

    setUpEnvironment(
      environment().toBuilder().apiClient(apiClient).currentUser(currentUser).build()
    );

    final Intent intent = new Intent().putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Mailbox.CREATOR_DASHBOARD);
    this.vm.intent(intent);
    this.messageThreadList.assertValueCount(2);
    this.messageThreadListCount.assertValues(0, 1);

    // Same message threads should not emit again.
    this.vm.inputs.onResume();
    this.messageThreadList.assertValueCount(2);
    this.messageThreadListCount.assertValues(0, 1);

    this.vm.inputs.mailbox(Mailbox.SENT);
    this.messageThreadList.assertValueCount(4);
    this.messageThreadListCount.assertValues(0, 1, 0, 2);
  }

  @Test
  public void testHasUnreadMessages() {
    final User user = UserFactory.user().toBuilder().unreadMessagesCount(3).build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<User> fetchCurrentUser() {
        return Observable.just(user);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.vm.intent(new Intent().putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Mailbox.DRAWER));
    this.vm.inputs.onResume();

    // Unread count text view is shown.
    this.unreadMessagesCount.assertValues(user.unreadMessagesCount());
    this.unreadMessagesCountIsGone.assertValues(false);
    this.hasNoUnreadMessages.assertValues(false);
    this.unreadCountTextViewColorInt.assertValues(R.color.accent);
    this.unreadCountTextViewTypefaceInt.assertValues(Typeface.BOLD);
    this.unreadCountToolbarTextViewIsGone.assertValues(false);

    this.vm.inputs.mailbox(Mailbox.SENT);
    this.unreadMessagesCountIsGone.assertValues(false, true);
  }

  @Test
  public void testNoMessages() {
    final User user = UserFactory.user().toBuilder().unreadMessagesCount(null).build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<User> fetchCurrentUser() {
        return Observable.just(user);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.vm.intent(new Intent().putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Mailbox.DRAWER));
    this.vm.inputs.onResume();

    this.hasNoMessages.assertValues(true);
    this.unreadMessagesCount.assertNoValues();
    this.unreadMessagesCountIsGone.assertValue(false);
    this.unreadCountTextViewColorInt.assertValues(R.color.kds_support_400);
    this.unreadCountTextViewTypefaceInt.assertValues(Typeface.NORMAL);
    this.unreadCountToolbarTextViewIsGone.assertValues(true);

    this.vm.inputs.mailbox(Mailbox.SENT);
    this.unreadMessagesCountIsGone.assertValues(false, true);
  }

  @Test
  public void testNoUnreadMessages() {
    final User user = UserFactory.user().toBuilder().unreadMessagesCount(0).build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<User> fetchCurrentUser() {
        return Observable.just(user);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.vm.intent(new Intent().putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Mailbox.DRAWER));
    this.vm.inputs.onResume();

    this.hasNoUnreadMessages.assertValues(true);
    this.unreadMessagesCount.assertNoValues();
    this.unreadMessagesCountIsGone.assertValue(false);
    this.unreadCountTextViewColorInt.assertValues(R.color.kds_support_400);
    this.unreadCountTextViewTypefaceInt.assertValues(Typeface.NORMAL);
    this.unreadCountToolbarTextViewIsGone.assertValues(true);

    this.vm.inputs.mailbox(Mailbox.SENT);
    this.unreadMessagesCountIsGone.assertValues(false, true);
  }

  @Test
  public void testMailboxTitle() {
    final User user = UserFactory.user();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<User> fetchCurrentUser() {
        return Observable.just(user);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());
    this.vm.intent(new Intent().putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Mailbox.DRAWER));
    this.vm.inputs.onResume();

    this.mailboxTitle.assertValue(R.string.messages_navigation_inbox);

    this.vm.inputs.mailbox(Mailbox.SENT);
    this.mailboxTitle.assertValues(R.string.messages_navigation_inbox, R.string.messages_navigation_sent);
  }
}
