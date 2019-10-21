package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.ui.activities.PlaygroundActivity;

import androidx.annotation.NonNull;

public interface PlaygroundViewModel {

  interface Inputs {
  }

  interface Outputs {
  }

  final class ViewModel extends ActivityViewModel<PlaygroundActivity> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;
  }
}
