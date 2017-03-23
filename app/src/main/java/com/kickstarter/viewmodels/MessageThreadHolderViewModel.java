package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.User;
import com.kickstarter.ui.viewholders.MessageThreadViewHolder;

import org.joda.time.DateTime;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface MessageThreadHolderViewModel {

  interface Inputs {
    /** Call to configure with a MessageThread. */
    void configureWith(MessageThread messageThread);
  }

  interface Outputs {
    /** Emits the date to display. */
    Observable<DateTime> dateDateTime();

    /** Emits the message body to display. */
    Observable<String> messageBodyTextViewText();

    /** Emits the participant's avatar url to display. */
    Observable<String> participantAvatarUrl();

    /** Emits the participant name to display. */
    Observable<String> participantNameTextViewText();

    /** Emits a boolean to determine if the unread indicator should be hidden. */
    Observable<Boolean> unreadIndicatorImageViewHidden();
  }

  final class ViewModel extends ActivityViewModel<MessageThreadViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<Message> lastMessage = this.messageThread.map(MessageThread::lastMessage);
      final Observable<User> participant = this.messageThread.map(MessageThread::participant);

      this.dateDateTime = lastMessage.map(Message::createdAt);
      this.messageBodyTextViewText = lastMessage.map(Message::body);
      this.participantAvatarUrl = participant.map(p -> p.avatar().medium());
      this.participantNameTextViewText = participant.map(User::name);
      this.unreadIndicatorImageViewHidden = this.messageThread.map(m -> m.unreadMessagesCount() == 0);
    }

    private final PublishSubject<MessageThread> messageThread = PublishSubject.create();

    private final Observable<DateTime> dateDateTime;
    private final Observable<String> messageBodyTextViewText;
    private final Observable<String> participantAvatarUrl;
    private final Observable<String> participantNameTextViewText;
    private final Observable<Boolean> unreadIndicatorImageViewHidden;

    public final MessageThreadHolderViewModel.Inputs inputs = this;
    public final MessageThreadHolderViewModel.Outputs outputs = this;

    @Override public void configureWith(final @NonNull MessageThread messageThread) {
      this.messageThread.onNext(messageThread);
    }

    @Override public @NonNull Observable<DateTime> dateDateTime() {
      return this.dateDateTime;
    }
    @Override public @NonNull Observable<String> messageBodyTextViewText() {
      return this.messageBodyTextViewText;
    }
    @Override public @NonNull Observable<String> participantAvatarUrl() {
      return this.participantAvatarUrl;
    }
    @Override public @NonNull Observable<String> participantNameTextViewText() {
      return this.participantNameTextViewText;
    }
    @Override public @NonNull Observable<Boolean> unreadIndicatorImageViewHidden() {
      return this.unreadIndicatorImageViewHidden;
    }
  }
}
