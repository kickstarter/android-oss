package com.kickstarter.presenters.inputs;

import android.view.View;

public interface TwoFactorPresenterInputs {
  void code(String __);
  void email(String __);
  void loginClick(View __);
  void password(String __);
  void resendClick(View __);
}
