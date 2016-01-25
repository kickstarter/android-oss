package com.kickstarter.viewmodels.inputs;

import android.support.annotation.NonNull;

import com.kickstarter.ui.activities.LoginToutActivity;
import com.kickstarter.ui.data.ActivityResult;

import java.util.List;

public interface LoginToutViewModelInputs {
  void activityResult(final @NonNull ActivityResult activityResult);
  void facebookLoginClick(final @NonNull LoginToutActivity activity, final @NonNull List<String> facebookPermissions);
}
