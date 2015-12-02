package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.viewmodels.SettingsViewModel;

import javax.inject.Inject;

import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresViewModel(SettingsViewModel.class)
public final class SettingsActivity extends BaseActivity<SettingsViewModel> {
  protected @BindColor(R.color.green) int green;
  protected @BindColor(R.color.gray) int gray;

  @Inject Logout logout;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);
  }

  @OnClick(R.id.log_out_button)
  public void logout() {
    logout.execute();
    final Intent intent = new Intent(this, DiscoveryActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
  }
}
