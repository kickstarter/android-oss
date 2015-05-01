package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kickstarter.R;

import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login_layout);
    ButterKnife.inject(this);
  }
}
