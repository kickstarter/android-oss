package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.MessageThreadFactory;
import com.kickstarter.factories.MessageThreadsEnvelopeFactory;
import com.kickstarter.factories.UserFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
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
  private TestSubscriber<List<MessageThread>> messageThreads = new TestSubscriber<>();
  private TestSubscriber<Boolean> unreadCountTextViewHidden = new TestSubscriber<>();
  private TestSubscriber<String> unreadCountTextViewText = new TestSubscriber<>();

  private void setUpEnvironment(Environment env) {
    this.vm = new MessageThreadsViewModel.ViewModel(env);
    this.vm.outputs.messageThreads().subscribe(this.messageThreads);
    this.vm.outputs.unreadCountTextViewHidden().subscribe(unreadCountTextViewHidden);
    this.vm.outputs.unreadCountTextViewText().subscribe(unreadCountTextViewText);
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
  public void testUnreadCountTextView_Hidden() {
    final User user = UserFactory.user().toBuilder().unreadMessagesCount(0).build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<User> fetchCurrentUser() {
        return Observable.just(user);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());

    this.unreadCountTextViewHidden.assertValues(true);
    this.unreadCountTextViewText.assertNoValues();
  }

  @Test
  public void testUnreadCountTextView_NotHidden() {
    final User user = UserFactory.user().toBuilder().unreadMessagesCount(3).build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override public @NonNull Observable<User> fetchCurrentUser() {
        return Observable.just(user);
      }
    };

    setUpEnvironment(environment().toBuilder().apiClient(apiClient).build());

    this.unreadCountTextViewHidden.assertValues(false);
    this.unreadCountTextViewText.assertValues(NumberUtils.format(user.unreadMessagesCount()));
  }
}
