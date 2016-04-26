package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.ui.activities.HelpActivity;

public final class HelpViewModel extends ActivityViewModel<HelpActivity> {
  public HelpViewModel(final @NonNull Environment environment) {
    super(environment);
  }
}
