package com.kickstarter.libs;

import android.support.annotation.NonNull;

import com.facebook.login.LoginManager;

import java.net.CookieManager;

public final class Logout {
  private final CookieManager cookieManager;
  private final CurrentUserType currentUser;

  public Logout(final @NonNull CookieManager cookieManager, final @NonNull CurrentUserType currentUser) {
    this.cookieManager = cookieManager;
    this.currentUser = currentUser;
  }

  public void execute() {
    this.currentUser.logout();
    this.cookieManager.getCookieStore().removeAll();
    LoginManager.getInstance().logOut();
  }
}
