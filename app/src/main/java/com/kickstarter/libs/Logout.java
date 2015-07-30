package com.kickstarter.libs;

import android.content.Context;

import com.kickstarter.KsrApplication;

import java.net.CookieManager;

public class Logout {
  private final CurrentUser currentUser;

  public Logout(final CurrentUser currentUser) {
    this.currentUser = currentUser;
  }

  public void execute(final Context context) {
    currentUser.unset();
    // TODO: Inject cookie manager
    CookieManager cookieManager = ((KsrApplication) context.getApplicationContext()).getCookieManager();
    cookieManager.getCookieStore().removeAll();
  }
}
