package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.ViewModel;
import com.kickstarter.ui.activities.SettingsActivity;

public class SettingsViewModel extends ViewModel<SettingsActivity> {
  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
  }
}
