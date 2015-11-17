package com.kickstarter.presenters.inputs;

public interface TwoFactorPresenterInputs {
  void code(String __);
  void email(String __);
  void fbAccessToken(String __);
  void isFacebookLogin(boolean __);
  void loginClick();
  void password(String __);
  void resendClick();
}
