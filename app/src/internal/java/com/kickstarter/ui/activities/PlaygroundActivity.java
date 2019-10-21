package com.kickstarter.ui.activities;

import android.os.Bundle;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.viewmodels.PlaygroundViewModel;

import androidx.annotation.Nullable;

@RequiresActivityViewModel(PlaygroundViewModel.ViewModel.class)
public final class PlaygroundActivity extends BaseActivity<PlaygroundViewModel.ViewModel> {

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.playground_layout);
  }
}
