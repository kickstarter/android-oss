package com.kickstarter.libs;

import android.support.annotation.NonNull;

import com.facebook.login.LoginManager;

import java.net.CookieManager;

public class Logout {
  private final CookieManager cookieManager;
  private final CurrentUser currentUser;

  public Logout(@NonNull final CookieManager cookieManager, @NonNull final CurrentUser currentUser) {
    this.cookieManager = cookieManager;
    this.currentUser = currentUser;
  }

  public void execute() {
    currentUser.logout();
    cookieManager.getCookieStore().removeAll();
    LoginManager.getInstance().logOut();
  }
}
