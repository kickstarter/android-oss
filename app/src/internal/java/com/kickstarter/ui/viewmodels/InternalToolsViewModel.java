package com.kickstarter.ui.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.ui.activities.InternalToolsActivity;

public class InternalToolsViewModel extends ActivityViewModel<InternalToolsActivity> {
  public InternalToolsViewModel(final @NonNull Environment environment) {
    super(environment);
  }
}
