package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Message;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.viewholders.MessageViewHolder;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;

public interface MessageHolderViewModel {

  interface Inputs {
    /** Call to configure the view model with a message. */
    void configureWith(Message message);
  }

  interface Outputs {
    /** Emits the message body text view text. */
    Observable<String> messageBodyTextViewText();

    /** Emits a boolean that determineswhether creator's avatar image should be hidden. */
    Observable<Boolean> creatorAvatarImageHidden();

    /** Emits the url for the creator's avatar image. */
    Observable<String> creatorAvatarImageUrl();
  }

  final class ViewModel extends ActivityViewModel<MessageViewHolder> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      final Observable<User> freshUser = this.client.fetchCurrentUser()
        .retry(2)
        .compose(neverError());
      freshUser.subscribe(this.currentUser::refresh);

      this.messageBodyTextViewText = this.message.map(Message::body);

      this.creatorAvatarImageHidden = this.message
        .compose(combineLatestPair(this.currentUser.loggedInUser()))
        .map(mu -> mu.first.sender().id() == mu.second.id());

      this.creatorAvatarImageUrl = this.message
        .compose(combineLatestPair(this.creatorAvatarImageHidden))
        .filter(messageAndHidden -> !messageAndHidden.second)
        .map(mu -> mu.first.sender().avatar().medium());
    }

    private final PublishSubject<Message> message = PublishSubject.create();

    private final Observable<Boolean> creatorAvatarImageHidden;
    private final Observable<String> creatorAvatarImageUrl;
    private final Observable<String> messageBodyTextViewText;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override
    public void configureWith(final @NonNull Message message) {
      this.message.onNext(message);
    }

    @Override public @NonNull Observable<Boolean> creatorAvatarImageHidden() {
      return this.creatorAvatarImageHidden;
    }
    @Override public @NonNull Observable<String> creatorAvatarImageUrl() {
      return this.creatorAvatarImageUrl;
    }
    @Override public @NonNull Observable<String> messageBodyTextViewText() {
      return this.messageBodyTextViewText;
    }
  }
}
