package com.kickstarter.ui.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.ui.activities.InternalToolsActivity;

public class InternalToolsViewModel extends ViewModel<InternalToolsActivity> {
  public InternalToolsViewModel(final @NonNull Environment environment) {
    super(environment);
  }
}
