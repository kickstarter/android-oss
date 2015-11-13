package com.kickstarter.presenters.inputs;

public interface TwoFactorPresenterInputs {
  void code(String __);
  void email(String __);
  void loginClick(boolean __);
  void password(String __);
  void resendClick();
}
