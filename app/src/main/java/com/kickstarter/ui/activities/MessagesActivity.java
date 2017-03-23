package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.viewmodels.MessagesViewModel;

@RequiresActivityViewModel(MessagesViewModel.ViewModel.class)
public final class MessagesActivity extends BaseActivity<MessagesViewModel.ViewModel> {

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.messages_layout);
  }
}
