package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Either;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaContext;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.services.apiresponses.MessageThreadEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.MessagesActivity;
import com.kickstarter.ui.data.MessagesData;

import java.util.List;

import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.errors;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.values;

public interface MessagesViewModel {

  interface Inputs {
    /** Call when the back or close button has been clicked. */
    void backOrCloseButtonClicked();

    /** Call when the message edit text changes. */
    void messageEditTextChanged(String messageBody);

    /** Call when the send message button has been clicked. */
    void sendMessageButtonClicked();

    /** Call when the view pledge button is clicked. */
    void viewPledgeButtonClicked();
  }

  interface Outputs {
    /** Emits a boolean that determines if the back button should be gone. */
    Observable<Boolean> backButtonIsGone();

    /** Emits the backing and project to populate the backing info header. */
    Observable<Pair<Backing, Project>> backingAndProject();

    /** Emits a boolean that determines if the backing info view should be gone. */
    Observable<Boolean> backingInfoViewIsGone();

    /** Emits a boolean that determines if the close button should be gone. */
    Observable<Boolean> closeButtonIsGone();

    /** Emits when we should navigate back. */
    Observable<Void> goBack();

    /** Emits a string to display as the message edit text hint. */
    Observable<String> messageEditTextHint();

    /** Emits a list of messages to be displayed. */
    Observable<List<Message>> messages();

    /** Emits the participant name to be displayed. */
    Observable<String> participantNameTextViewText();

    /** Emits the project name to be displayed. */
    Observable<String> projectNameTextViewText();

    /** Emits a string to set the message edit text to. */
    Observable<String> setMessageEditText();

    /** Emits a string to display in the message error toast. */
    Observable<String> showMessageErrorToast();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ViewPledgeActivity}. */
    Observable<Project> startViewPledgeActivity();

    /** Emits a boolean that determines if the View pledge button should be gone. */
    Observable<Boolean> viewPledgeButtonIsGone();
  }

  final class ViewModel extends ActivityViewModel<MessagesActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      final Observable<Either<MessageThread, Pair<Project, Backing>>> configData = intent()
        .map(i -> {
          final MessageThread messageThread = i.getParcelableExtra(IntentKey.MESSAGE_THREAD);
          return messageThread != null
            ? new Either.Left<MessageThread, Pair<Project, Backing>>(messageThread)
            : new Either.Right<MessageThread, Pair<Project, Backing>>(
              Pair.create(i.getParcelableExtra(IntentKey.PROJECT), i.getParcelableExtra(IntentKey.BACKING))
          );
        });

      final Observable<KoalaContext.Message> koalaContext = intent()
        .map(i -> i.getSerializableExtra(IntentKey.KOALA_CONTEXT))
        .ofType(KoalaContext.Message.class);

      final Observable<Backing> configBacking = configData
        .map(Either::right)
        .filter(ObjectUtils::isNotNull)
        .map(PairUtils::second);

      final Observable<MessageThread> configThread = configData
        .map(Either::left)
        .filter(ObjectUtils::isNotNull);

      final Observable<Either<Backing, MessageThread>> backingOrThread = Observable.merge(
        configBacking.map(backing -> new Either.Left<>(backing)),
        configThread.map(thread -> new Either.Right<>(thread))
      );

      final Observable<Notification<Message>> messageNotification = backingOrThread
        .compose(combineLatestPair(this.messageEditTextChanged))
        .compose(takeWhen(this.sendMessageButtonClicked))
        .switchMap(backingOrThreadAndBody ->
          backingOrThreadAndBody.first.isLeft()
            ? this.client.sendMessageToBacking(backingOrThreadAndBody.first.left(), backingOrThreadAndBody.second)
            : this.client.sendMessageToThread(backingOrThreadAndBody.first.right(), backingOrThreadAndBody.second)
        )
        .materialize()
        .share();

      final Observable<Message> messageSent = messageNotification.compose(values()).ofType(Message.class);

      this.setMessageEditText = messageSent.map(__ -> "");

      final Observable<MessageThreadEnvelope> messageThreadEnvelope = Observable.merge(
        backingOrThread,
        backingOrThread.compose(takeWhen(messageSent))
      )
        .switchMap(bOrT -> {
          if (bOrT.isLeft()) {
            return this.client.fetchMessagesForBacking(bOrT.left());
          } else {
            return this.client.fetchMessagesForThread(bOrT.right());
          }
        })
        .compose(neverError())
        .share();

      final Observable<Project> project = configData
        .map(data -> data.isLeft() ? data.left().project() : data.right().first); // how do we avoid these warnings

      // todo: audit for collaborators
      final Observable<User> participant = Observable.merge(
        messageThreadEnvelope
          .map(MessageThreadEnvelope::messageThread)
          .filter(ObjectUtils::isNotNull)
          .map(MessageThread::participant),
        project.map(Project::creator)
      )
        .take(1);

      messageThreadEnvelope
        .map(MessageThreadEnvelope::messages)
        .filter(ObjectUtils::isNotNull)
        .compose(bindToLifecycle())
        .subscribe(this.messages::onNext);

      participant
        .map(User::name)
        .compose(bindToLifecycle())
        .subscribe(this.participantNameTextViewText::onNext);

      this.messageEditTextHint = this.participantNameTextViewText;

      Observable.combineLatest(
        backingOrThread,
        project,
        participant,
        this.currentUser.observable(),
        MessagesData::new
      )
        .switchMap(data -> backingAndProjectFromData(data, this.client))
        .compose(bindToLifecycle())
        .subscribe(this.backingAndProject::onNext);

      this.backingAndProject
        .map(bp -> bp.first == null)
        .compose(bindToLifecycle())
        .subscribe(this.backingInfoViewIsGone::onNext);

      koalaContext
        .map(c -> c.equals(KoalaContext.Message.BACKER_MODAL))
        .compose(bindToLifecycle())
        .subscribe(this.viewPledgeButtonIsGone::onNext);

      this.backButtonIsGone = this.viewPledgeButtonIsGone.map(BooleanUtils::negate);
      this.closeButtonIsGone = this.backButtonIsGone.map(BooleanUtils::negate);
      this.goBack = this.backOrCloseButtonClicked;
      this.startViewPledgeActivity = project.compose(takeWhen(this.viewPledgeButtonClicked));

      messageNotification
        .compose(errors())
        .map(ErrorEnvelope::fromThrowable)
        .map(ErrorEnvelope::errorMessage)
        .subscribe(this.showMessageErrorToast::onNext);

      project
        .map(Project::name)
        .compose(bindToLifecycle())
        .subscribe(this.projectNameTextViewText::onNext);

      project
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackViewedMessageThread);

      Observable.combineLatest(project, koalaContext, Pair::create)
        .compose(takeWhen(messageSent))
        .compose(bindToLifecycle())
        .subscribe(pc -> this.koala.trackSentMessage(pc.first, pc.second));
    }

    private static @NonNull Observable<Pair<Backing, Project>> backingAndProjectFromData(final @NonNull MessagesData data,
      final @NonNull ApiClientType client) {

      if (data.getBackingOrThread().isLeft()) {
        return Observable.just(Pair.create(data.getBackingOrThread().left(), data.getProject()));
      } else {
        final Observable<Notification<Backing>> backingNotification = data.getProject().isBacking()
          ? client.fetchProjectBacking(data.getProject(), data.getCurrentUser()).materialize().share()
          : client.fetchProjectBacking(data.getProject(), data.getParticipant()).materialize().share();

        return backingNotification
          .compose(values())
          .map(b -> Pair.create(b, data.getProject()));
      }
    }

    private final PublishSubject<Void> backOrCloseButtonClicked = PublishSubject.create();
    private final PublishSubject<String> messageEditTextChanged = PublishSubject.create();
    private final PublishSubject<Void> sendMessageButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> viewPledgeButtonClicked = PublishSubject.create();

    private final Observable<Boolean> backButtonIsGone;
    private final BehaviorSubject<Pair<Backing, Project>> backingAndProject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> backingInfoViewIsGone = BehaviorSubject.create();
    private final Observable<Boolean> closeButtonIsGone;
    private final Observable<Void> goBack;
    private final Observable<String> messageEditTextHint;
    private final BehaviorSubject<List<Message>> messages = BehaviorSubject.create();
    private final BehaviorSubject<String> participantNameTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<String> projectNameTextViewText = BehaviorSubject.create();
    private final PublishSubject<String> showMessageErrorToast = PublishSubject.create();
    private final Observable<String> setMessageEditText;
    private final Observable<Project> startViewPledgeActivity;
    private final BehaviorSubject<Boolean> viewPledgeButtonIsGone = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void backOrCloseButtonClicked() {
      this.backOrCloseButtonClicked.onNext(null);
    }
    @Override public void messageEditTextChanged(final @NonNull String messageBody) {
      this.messageEditTextChanged.onNext(messageBody);
    }
    @Override public void sendMessageButtonClicked() {
      this.sendMessageButtonClicked.onNext(null);
    }
    @Override public void viewPledgeButtonClicked() {
      this.viewPledgeButtonClicked.onNext(null);
    }

    @Override public @NonNull Observable<Boolean> backButtonIsGone() {
      return this.backButtonIsGone;
    }
    @Override public @NonNull Observable<Pair<Backing, Project>> backingAndProject() {
      return this.backingAndProject;
    }
    @Override public @NonNull Observable<Boolean> backingInfoViewIsGone() {
      return this.backingInfoViewIsGone;
    }
    @Override public @NonNull Observable<Boolean> closeButtonIsGone() {
      return this.closeButtonIsGone;
    }
    @Override public @NonNull Observable<Void> goBack() {
      return this.goBack;
    }
    @Override public @NonNull Observable<String> messageEditTextHint() {
      return this.messageEditTextHint;
    }
    @Override public @NonNull Observable<List<Message>> messages() {
      return this.messages;
    }
    @Override public @NonNull Observable<String> participantNameTextViewText() {
      return this.participantNameTextViewText;
    }
    @Override public @NonNull Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }
    @Override public @NonNull Observable<String> showMessageErrorToast() {
      return this.showMessageErrorToast;
    }
    @Override public @NonNull Observable<String> setMessageEditText() {
      return this.setMessageEditText;
    }
    @Override public @NonNull Observable<Project> startViewPledgeActivity() {
      return this.startViewPledgeActivity;
    }
    @Override public @NonNull Observable<Boolean> viewPledgeButtonIsGone() {
      return this.viewPledgeButtonIsGone;
    }
  }
}
