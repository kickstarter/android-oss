package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.ui.activities.MessagesActivity;

public interface MessagesViewModel {

  final class ViewModel extends ActivityViewModel<MessagesActivity> {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);
    }
  }
}
