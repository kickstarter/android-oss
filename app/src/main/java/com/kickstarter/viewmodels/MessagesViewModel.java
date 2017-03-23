package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.MessageThread;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.MessagesActivity;

import rx.Observable;

public interface MessagesViewModel {

  interface Inputs {

  }

  interface Outputs {

  }

  final class ViewModel extends ActivityViewModel<MessagesActivity> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<MessageThread> messageThread = intent()
        .take(1)
        .map(i -> i.getParcelableExtra(IntentKey.MESSAGE_THREAD))
        .ofType(MessageThread.class);




    }


    public final Inputs inputs = this;
    public final Outputs outputs = this;

  }
}
