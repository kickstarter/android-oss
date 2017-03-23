package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Message;
import com.kickstarter.ui.viewholders.MessageViewHolder;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface MessageHolderViewModel {

  interface Inputs {
    /** Call to configure the view model with a message. */
    void configureWith(Message message);
  }

  interface Outputs {
    /** Emits the message body text view text. */
    Observable<String> messageBodyTextViewText();
  }

  final class ViewModel extends ActivityViewModel<MessageViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.messageBodyTextViewText = this.message.map(Message::body);
    }

    private final PublishSubject<Message> message = PublishSubject.create();

    private final Observable<String> messageBodyTextViewText;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override
    public void configureWith(final @NonNull Message message) {
      this.message.onNext(message);
    }

    @Override public @NonNull Observable<String> messageBodyTextViewText() {
      return this.messageBodyTextViewText;
    }
  }
}
