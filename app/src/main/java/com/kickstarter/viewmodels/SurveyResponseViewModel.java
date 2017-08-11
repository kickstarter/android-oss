package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.ui.activities.SurveyResponseActivity;

public interface SurveyResponseViewModel {

  interface Inputs {

  }

  interface Outputs {

  }

  final class ViewModel extends ActivityViewModel<SurveyResponseActivity> implements Inputs, Outputs {
    public ViewModel(final @NonNull Environment environment) {
      super(environment);
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;
  }
}
