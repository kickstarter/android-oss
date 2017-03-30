package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.MessageThreadEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.MessagesActivity;

import java.util.List;

import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.values;

public interface MessagesViewModel {

  interface Inputs {
    /** Call when the message edit text changes. */
    void messageEditTextChanged(String messageBody);
  }

  interface Outputs {
    /** Emits the backing and project to populate the backing info header. */
    Observable<Pair<Backing, Project>> backingAndProject();

    /** Emits a boolean that determines if the backing info view should be hidden. */
    Observable<Boolean> backingInfoViewHidden();

    /** Emits the participant name to be displayed. */
    Observable<String> participantNameTextViewText();

    /** Emits a list of messages to be displayed. */
    Observable<List<Message>> messages();

    /** Emits the project name to be displayed. */
    Observable<String> projectNameTextViewText();
  }

  final class ViewModel extends ActivityViewModel<MessagesActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      final Observable<MessageThread> messageThread = intent()
        .take(1)
        .map(i -> i.getParcelableExtra(IntentKey.MESSAGE_THREAD))
        .ofType(MessageThread.class);

      final Observable<Notification<MessageThreadEnvelope>> envelopeNotification = messageThread
        .switchMap(thread -> this.client.fetchMessagesForThread(thread).materialize())
        .share();

      final Observable<MessageThreadEnvelope> messageThreadEnvelope = envelopeNotification.compose(values());

      final Observable<Notification<Backing>> backingNotification = Observable.combineLatest(
        messageThread,
        this.currentUser.observable(),
        Pair::create
      )
        .filter(mu -> mu.first.backing() == null)
        .switchMap(mu ->
          mu.first.project().isBacking()
            ? this.client.fetchProjectBacking(mu.first.project(), mu.second).materialize()
            : this.client.fetchProjectBacking(mu.first.project(), mu.first.participant()).materialize()
        )
        .share();

      final Observable<Backing> backing = Observable.merge(
        messageThread.map(MessageThread::backing).filter(ObjectUtils::isNotNull),
        backingNotification.compose(values())
      )
        .filter(ObjectUtils::isNotNull);

      Observable.merge(
        messageThread.map(MessageThread::backing).map(ObjectUtils::isNull),
        backing.map(ObjectUtils::isNull)
      )
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(this.backingInfoViewHidden::onNext);

      Observable.combineLatest(
        backing,
        messageThread.map(MessageThread::project),
        Pair::create
      )
        .compose(bindToLifecycle())
        .subscribe(this.backingAndProject::onNext);

      messageThread
        .map(thread -> thread.project().creator().name())
        .compose(bindToLifecycle())
        .subscribe(this.participantNameTextViewText::onNext);

      messageThreadEnvelope
        .map(MessageThreadEnvelope::messages)
        .compose(bindToLifecycle())
        .subscribe(this.messages::onNext);

      messageThread
        .map(thread -> thread.project().name())
        .compose(bindToLifecycle())
        .subscribe(this.projectNameTextViewText::onNext);
    }

    private final PublishSubject<String> messageEditText = PublishSubject.create();

    private final BehaviorSubject<Pair<Backing, Project>> backingAndProject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> backingInfoViewHidden = BehaviorSubject.create();
    private final BehaviorSubject<String> participantNameTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<List<Message>> messages = BehaviorSubject.create();
    private final BehaviorSubject<String> projectNameTextViewText = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void messageEditTextChanged(final @NonNull String messageBody) {
      this.messageEditText.onNext(messageBody);
    }

    @Override public @NonNull BehaviorSubject<Pair<Backing, Project>> backingAndProject() {
      return this.backingAndProject;
    }
    @Override public @NonNull BehaviorSubject<Boolean> backingInfoViewHidden() {
      return this.backingInfoViewHidden;
    }
    @Override public @NonNull Observable<String> participantNameTextViewText() {
      return this.participantNameTextViewText;
    }
    @Override public @NonNull Observable<List<Message>> messages() {
      return this.messages;
    }
    @Override public @NonNull Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }
  }
}
