package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.viewmodels.SurveyResponseViewModel;

@RequiresActivityViewModel(SurveyResponseViewModel.ViewModel.class)
public class SurveyResponseActivity extends BaseActivity<SurveyResponseViewModel.ViewModel> {

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }
}
