package com.kickstarter.libs;

import com.facebook.login.LoginManager;

import java.net.CookieManager;

import androidx.annotation.NonNull;

public final class Logout {
  private final CookieManager cookieManager;
  private final CurrentUserType currentUser;
  private final CurrentUserTypeV2 currentUserV2;
  public Logout(final @NonNull CookieManager cookieManager, final @NonNull CurrentUserType currentUser, final @NonNull CurrentUserTypeV2 currentUserV2) {
    this.cookieManager = cookieManager;
    this.currentUser = currentUser;
    this.currentUserV2 = currentUserV2;
  }

  public void execute() {
    this.currentUser.logout();
    this.currentUserV2.logout();
    this.cookieManager.getCookieStore().removeAll();
    LoginManager.getInstance().logOut();
  }
}
