package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.MessageThreadFactory;
import com.kickstarter.factories.MessageThreadsEnvelopeFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.MessageThread;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

public class MessageThreadsViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testMessageThreadsEmit() {
    final MessageThreadsEnvelope envelope = MessageThreadsEnvelopeFactory.messageThreadsEnvelope()
      .toBuilder()
      .messageThreads(Collections.singletonList(MessageThreadFactory.messageThread()))
      .build();

    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<MessageThreadsEnvelope> fetchMessageThreads() {
        return Observable.just(envelope);
      }
    };

    final Environment env = environment().toBuilder().apiClient(apiClient).build();
    final MessageThreadsViewModel.ViewModel vm = new MessageThreadsViewModel.ViewModel(env);

    final TestSubscriber<List<MessageThread>> messageThreads = new TestSubscriber<>();
    vm.outputs.messageThreads().subscribe(messageThreads);

    // todo: this needs to actually happen
    vm.inputs.refresh();

    // Message threads emit.
    messageThreads.assertValues(envelope.messageThreads());
  }
}
