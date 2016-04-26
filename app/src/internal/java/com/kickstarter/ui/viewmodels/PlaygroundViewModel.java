package com.kickstarter.ui.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.ui.activities.PlaygroundActivity;

public final class PlaygroundViewModel extends ActivityViewModel<PlaygroundActivity> {
  public PlaygroundViewModel(final @NonNull Environment environment) {
    super(environment);
  }
}
