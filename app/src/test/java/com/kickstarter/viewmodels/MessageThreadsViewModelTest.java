package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.MessageThreadFactory;
import com.kickstarter.factories.MessageThreadsEnvelopeFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

public class MessageThreadsViewModelTest extends KSRobolectricTestCase {
  private MessageThreadsViewModel.ViewModel vm;
  private final TestSubscriber<Boolean> hasNoMessages = new TestSubscriber<>();
  private final TestSubscriber<Boolean> hasNoUnreadMessages = new TestSubscriber<>();
  private final TestSubscriber<List<MessageThread>> messageThreads = new TestSubscriber<>();
  private final TestSubscriber<Integer> unreadMessagesCount = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment env) {
    this.vm = new MessageThreadsViewModel.ViewModel(env);
    this.vm.outputs.hasNoMessages().subscribe(this.hasNoMessages);
    this.vm.outputs.hasNoUnreadMessages().subscribe(this.hasNoUnreadMessages);
    this.vm.outputs.messageThreads().subscribe(this.messageThreads);
    this.vm.outputs.unreadMessagesCount().subscribe(this.unreadMessagesCount);
  }

  @Test
  public void testMessageThreadsEmit() {
    final MessageThreadsEnvelope envelope = MessageThreadsEnvelopeFactory.messageThreadsEnvelope()
      .toBuilder()
      .messageThreads(Collections.singletonList(MessageThreadFactory.messageThread()))
      .build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<MessageThreadsEnvelope> fetchMessageThreads() {
        return Observable.just(envelope);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());

    this.messageThreads.assertValueCount(1);
  }

  @Test
  public void testUnreadCountTextView() {
    final User user = UserFactory.user().toBuilder().unreadMessagesCount(3).build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<User> fetchCurrentUser() {
        return Observable.just(user);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());

    // Unread count text view is shown.
    this.unreadMessagesCount.assertValues(user.unreadMessagesCount());
    this.hasNoUnreadMessages.assertValues(false);
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

    this.hasNoMessages.assertValues(true);
    this.unreadMessagesCount.assertNoValues();
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

    this.hasNoUnreadMessages.assertValues(true);
    this.unreadMessagesCount.assertValues(0);
  }
}
