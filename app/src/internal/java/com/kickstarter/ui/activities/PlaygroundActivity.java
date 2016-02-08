package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Playground;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.ui.viewmodels.PlaygroundViewModel;

@RequiresViewModel(PlaygroundViewModel.class)
public final class PlaygroundActivity extends BaseActivity<PlaygroundViewModel> {
  private final Playground playground = new Playground();

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.playground_layout);
  }
}
