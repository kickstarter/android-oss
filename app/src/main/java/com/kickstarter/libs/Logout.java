package com.kickstarter.libs;

import android.content.Context;

import java.net.CookieManager;

public class Logout {
  private final CookieManager cookieManager;
  private final CurrentUser currentUser;

  public Logout(final CookieManager cookieManager, final CurrentUser currentUser) {
    this.cookieManager = cookieManager;
    this.currentUser = currentUser;
  }

  public void execute(final Context context) {
    // TODO: Could subscribe to stream?
    currentUser.logout();
    cookieManager.getCookieStore().removeAll();
  }
}
