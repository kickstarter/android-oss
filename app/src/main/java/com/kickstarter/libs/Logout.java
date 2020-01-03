package com.kickstarter.libs;

import com.facebook.login.LoginManager;
import com.qualtrics.digital.Qualtrics;

import java.net.CookieManager;

import androidx.annotation.NonNull;

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
    Qualtrics.instance().resetTimer();
    Qualtrics.instance().resetViewCounter();
    LoginManager.getInstance().logOut();
  }
}
