package com.kickstarter.ui.activities;

import android.os.Bundle;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.presenters.CheckoutPresenter;

import butterknife.ButterKnife;

@RequiresPresenter(CheckoutPresenter.class)
public class CheckoutActivity extends BaseActivity<CheckoutPresenter> {
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.checkout_layout);
    ButterKnife.inject(this);
  }
}
