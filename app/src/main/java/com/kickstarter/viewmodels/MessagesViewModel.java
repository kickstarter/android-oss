package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.MessageThreadEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.MessagesActivity;

import java.util.List;

import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.values;

public interface MessagesViewModel {

  interface Inputs {
  }

  interface Outputs {
    /** Emits the creator name to be displayed. */
    Observable<String> creatorNameTextViewText();

    /** Emits a list of messages to be displayed. */
    Observable<List<Message>> messages();

    /** Emits the project name to be displayed. */
    Observable<String> projectNameTextViewText();
  }

  final class ViewModel extends ActivityViewModel<MessagesActivity> implements Inputs, Outputs {
    private final ApiClientType client;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();

      final Observable<MessageThread> messageThread = intent()
        .take(1)
        .map(i -> i.getParcelableExtra(IntentKey.MESSAGE_THREAD))
        .ofType(MessageThread.class);

      final Observable<Notification<MessageThreadEnvelope>> envelopeNotification = messageThread
        .switchMap(thread -> this.client.fetchMessagesForThread(thread).materialize())
        .share();

      final Observable<MessageThreadEnvelope> messageThreadEnvelope = envelopeNotification.compose(values());

      messageThread
        .map(thread -> thread.project().creator().name())
        .compose(bindToLifecycle())
        .subscribe(this.creatorNameTextViewText::onNext);

      messageThreadEnvelope
        .map(MessageThreadEnvelope::messages)
        .compose(bindToLifecycle())
        .subscribe(this.messages::onNext);

      messageThread
        .map(thread -> thread.project().name())
        .compose(bindToLifecycle())
        .subscribe(this.projectNameTextViewText::onNext);
    }

    private final BehaviorSubject<String> creatorNameTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<List<Message>> messages = BehaviorSubject.create();
    private final BehaviorSubject<String> projectNameTextViewText = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public @NonNull Observable<String> creatorNameTextViewText() {
      return this.creatorNameTextViewText;
    }
    @Override public @NonNull Observable<List<Message>> messages() {
      return this.messages;
    }
    @Override public @NonNull Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }
  }
}
