package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.presenters.SignupPresenter;

import butterknife.ButterKnife;

@RequiresPresenter(SignupPresenter.class)
public class SignupActivity extends BaseActivity<SignupPresenter> {

  @Override
  protected  void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.signup_layout);
    ButterKnife.bind(this);
  }
}
