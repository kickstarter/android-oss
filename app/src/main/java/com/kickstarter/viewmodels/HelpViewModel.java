package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.ui.activities.HelpActivity;

public final class HelpViewModel extends ViewModel<HelpActivity> {
  public HelpViewModel(final @NonNull Environment environment) {
    super(environment);
  }
}
