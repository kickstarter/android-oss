package com.kickstarter.services;

import android.net.Uri;

import java.util.regex.Pattern;

public class KickstarterUri {
 public static boolean isKickstarterUri(final Uri uri, final String webEndpoint) {
    return uri.getHost().equals(Uri.parse(webEndpoint).getHost());
  }

  public static boolean isProjectUri(final Uri uri, final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && PROJECT_PATTERN.matcher(uri.getPath()).matches();
  }

  public static boolean isSignupUri(final Uri uri, final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getPath().equals("/signup");
  }

  public static boolean isCheckoutThanksUri(final Uri uri, final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && CHECKOUT_THANKS_PATTERN.matcher(uri.getPath()).matches();
  }

  protected boolean isProjectNewPledgeUri(final Uri uri, final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && NEW_PLEDGE_PATTERN.matcher(uri.getPath()).matches();
  }

  // /projects/slug-1/slug-2
  private static final Pattern PROJECT_PATTERN = Pattern.compile("\\A\\/projects/[a-zA-Z0-9_-]+\\/[a-zA-Z0-9_-]+\\/?\\z");

  // /projects/slug-1/slug-2/checkouts/1/thanks
  private static final Pattern CHECKOUT_THANKS_PATTERN = Pattern.compile("\\A\\/projects/[a-zA-Z0-9_-]+\\/[a-zA-Z0-9_-]+\\/checkouts\\/\\d+\\/thanks\\z");

  // /projects/slug-1/slug-2/pledge/new
  private static final Pattern NEW_PLEDGE_PATTERN = Pattern.compile("\\A\\/projects/[a-zA-Z0-9_-]+\\/[a-zA-Z0-9_-]+\\/pledge\\/new\\z");
}
