package com.kickstarter.viewmodels;

import android.content.SharedPreferences;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Message;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.User;
import com.kickstarter.ui.SharedPreferenceKey;
import com.kickstarter.ui.viewholders.MessageThreadViewHolder;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface MessageThreadHolderViewModel {

  interface Inputs {
    /** Call to configure with a MessageThread. */
    void configureWith(MessageThread messageThread);

    /** Call when the the message thread card view has been clicked. */
    void messageThreadCardViewClicked();
  }

  interface Outputs {
    /** Emits when the card view should be elevated. */
    Observable<Boolean> cardViewIsElevated();

    /** Emits the date to display. */
    Observable<DateTime> dateDateTime();

    /** Emits when the date font weight is medium. */
    Observable<Boolean> dateTextViewIsMediumWeight();

    /** Emits the message body to display. */
    Observable<String> messageBodyTextViewText();

    /** Emits the participant's avatar url to display. */
    Observable<String> participantAvatarUrl();

    /** Emits when the participant name font weight is medium. */
    Observable<Boolean> participantNameTextViewIsMediumWeight();

    /** Emits the participant name to display. */
    Observable<String> participantNameTextViewText();

    /** Emits when we want to start the {@link com.kickstarter.ui.activities.MessagesActivity}. */
    Observable<MessageThread> startMessagesActivity();

    /** Emits a boolean to determine if the unread count text view should be gone. */
    Observable<Boolean> unreadCountTextViewIsGone();

    /** Emits the unread count text view text to be displayed. */
    Observable<String> unreadCountTextViewText();

    /** Emits a boolean to determine if the unread indicator should be hidden. */
    Observable<Boolean> unreadIndicatorViewHidden();
  }

  final class ViewModel extends ActivityViewModel<MessageThreadViewHolder> implements Inputs, Outputs {
    private final SharedPreferences sharedPreferences;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.sharedPreferences = environment.sharedPreferences();

      // Store the correct initial hasUnreadMessages value.
      this.messageThread
        .compose(observeForUI())
        .subscribe(thread -> setHasUnreadMessagesPreference(thread, this.sharedPreferences));

      final Observable<Boolean> hasUnreadMessages = Observable.merge(
        this.messageThread.map(thread -> hasUnreadMessages(thread, this.sharedPreferences)),
        this.messageThreadCardViewClicked.map(__ -> false)
      );

      final Observable<Message> lastMessage = this.messageThread.map(MessageThread::lastMessage);
      final Observable<User> participant = this.messageThread.map(MessageThread::participant);

      this.cardViewIsElevated = hasUnreadMessages;
      this.dateDateTime = lastMessage.map(Message::createdAt);
      this.dateTextViewIsMediumWeight = hasUnreadMessages;
      this.messageBodyTextViewText = lastMessage.map(Message::body);
      this.participantAvatarUrl = participant.map(p -> p.avatar().medium());
      this.participantNameTextViewIsMediumWeight = hasUnreadMessages;
      this.participantNameTextViewText = participant.map(User::name);
      this.startMessagesActivity = this.messageThread.compose(takeWhen(this.messageThreadCardViewClicked));
      this.unreadCountTextViewIsGone = hasUnreadMessages.map(BooleanUtils::negate);
      this.unreadCountTextViewText = this.messageThread
        .map(MessageThread::unreadMessagesCount)
        .map(NumberUtils::format);

      this.unreadIndicatorViewHidden = hasUnreadMessages.map(BooleanUtils::negate);

      this.messageThread
        .compose(takeWhen(this.messageThreadCardViewClicked))
        .subscribe(thread -> markedAsRead(thread, this.sharedPreferences));
    }

    private static @NonNull String cacheKey(final @NonNull MessageThread messageThread) {
      return SharedPreferenceKey.MESSAGE_THREAD_HAS_UNREAD_MESSAGES + messageThread.id();
    }

    private static boolean hasUnreadMessages(final @NonNull MessageThread messageThread,
      final @NonNull SharedPreferences sharedPreferences) {
      return sharedPreferences.getBoolean(cacheKey(messageThread), messageThread.unreadMessagesCount() > 0);
    }

    private static void markedAsRead(final @NonNull MessageThread messageThread,
      final @NonNull SharedPreferences sharedPreferences) {
      final SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putBoolean(cacheKey(messageThread), false);
      editor.apply();
    }

    private static void setHasUnreadMessagesPreference(final @NonNull MessageThread messageThread,
      final @NonNull SharedPreferences sharedPreferences) {

      final SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putBoolean(cacheKey(messageThread), messageThread.unreadMessagesCount() > 0);
      editor.apply();
    }

    private final PublishSubject<MessageThread> messageThread = PublishSubject.create();
    private final PublishSubject<Void> messageThreadCardViewClicked = PublishSubject.create();

    private final Observable<Boolean> cardViewIsElevated;
    private final Observable<DateTime> dateDateTime;
    private final Observable<Boolean> dateTextViewIsMediumWeight;
    private final Observable<String> messageBodyTextViewText;
    private final Observable<String> participantAvatarUrl;
    private final Observable<Boolean> participantNameTextViewIsMediumWeight;
    private final Observable<String> participantNameTextViewText;
    private final Observable<MessageThread> startMessagesActivity;
    private final Observable<Boolean> unreadCountTextViewIsGone;
    private final Observable<String> unreadCountTextViewText;
    private final Observable<Boolean> unreadIndicatorViewHidden;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void configureWith(final @NonNull MessageThread messageThread) {
      this.messageThread.onNext(messageThread);
    }
    @Override public void messageThreadCardViewClicked() {
      this.messageThreadCardViewClicked.onNext(null);
    }

    @Override public @NonNull Observable<Boolean> cardViewIsElevated() {
      return this.cardViewIsElevated;
    }
    @Override public @NonNull Observable<DateTime> dateDateTime() {
      return this.dateDateTime;
    }
    @Override public @NonNull Observable<Boolean> dateTextViewIsMediumWeight() {
      return this.dateTextViewIsMediumWeight;
    }
    @Override public @NonNull Observable<String> messageBodyTextViewText() {
      return this.messageBodyTextViewText;
    }
    @Override public @NonNull Observable<String> participantAvatarUrl() {
      return this.participantAvatarUrl;
    }
    @Override public @NonNull Observable<Boolean> participantNameTextViewIsMediumWeight() {
      return this.participantNameTextViewIsMediumWeight;
    }
    @Override public @NonNull Observable<String> participantNameTextViewText() {
      return this.participantNameTextViewText;
    }
    @Override public @NonNull Observable<MessageThread> startMessagesActivity() {
      return this.startMessagesActivity;
    }
    @Override public @NonNull Observable<Boolean> unreadCountTextViewIsGone() {
      return this.unreadCountTextViewIsGone;
    }
    @Override public @NonNull Observable<String> unreadCountTextViewText() {
      return this.unreadCountTextViewText;
    }
    @Override public @NonNull Observable<Boolean> unreadIndicatorViewHidden() {
      return this.unreadIndicatorViewHidden;
    }
  }
}
