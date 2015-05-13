package com.kickstarter.ui.activities;

import android.os.Bundle;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.presenters.TwoFactorPresenter;

@RequiresPresenter(TwoFactorPresenter.class)
public class TwoFactorActivity extends BaseActivity<TwoFactorPresenter> {
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.two_factor_layout);
  }
}
