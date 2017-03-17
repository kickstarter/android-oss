package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.ui.activities.MessageThreadsActivity;

public interface MessageThreadsViewModel {

  interface Inputs {

  }

  interface Outputs {

  }

  final class ViewModel extends ActivityViewModel<MessageThreadsActivity> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);
    }

    public final MessageThreadsViewModel.Inputs inputs = this;
    public final MessageThreadsViewModel.Outputs outputs = this;
  }
}
