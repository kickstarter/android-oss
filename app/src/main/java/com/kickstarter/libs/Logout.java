package com.kickstarter.libs;

import android.support.annotation.NonNull;

import com.facebook.login.LoginManager;

import java.net.CookieManager;

public class Logout {
  private final CookieManager cookieManager;
  private final CurrentUser currentUser;

  public Logout(final @NonNull CookieManager cookieManager, final @NonNull CurrentUser currentUser) {
    this.cookieManager = cookieManager;
    this.currentUser = currentUser;
  }

  public void execute() {
    currentUser.logout();
    cookieManager.getCookieStore().removeAll();
    LoginManager.getInstance().logOut();
  }
}
