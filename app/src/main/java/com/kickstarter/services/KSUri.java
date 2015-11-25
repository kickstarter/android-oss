package com.kickstarter.services;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.regex.Pattern;

public final class KSUri {
  public static boolean isApiUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && API_PATTERN.matcher(uri.getHost()).matches();
  }

  public static boolean isCookiesUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getPath().equals("/cookies");
  }

  public static boolean isHelloUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getPath().equals("/hello");
  }

  public static boolean isHivequeenUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && HIVEQUEEN_PATTERN.matcher(uri.getHost()).matches();
  }

 public static boolean isKickstarterUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return uri.getHost().equals(Uri.parse(webEndpoint).getHost());
  }

  public static boolean isProjectUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && PROJECT_PATTERN.matcher(uri.getPath()).matches();
  }

  public static boolean isSignupUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getPath().equals("/signup");
  }

  public static boolean isCheckoutThanksUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && CHECKOUT_THANKS_PATTERN.matcher(uri.getPath()).matches();
  }

  public static boolean isModalUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getQueryParameter("modal") != null && uri.getQueryParameter("modal").equals("true");
  }

  public static boolean isPrivacyUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getPath().equals("/privacy");
  }

  public static boolean isProjectNewPledgeUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && NEW_PLEDGE_PATTERN.matcher(uri.getPath()).matches();
  }

  public static boolean isTermsOfUseUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getPath().equals("/terms-of-use");
  }

  public static boolean isWebUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) &&
      WEB_PATTERN.matcher(uri.getHost()).matches() &&
      !isApiUri(uri, webEndpoint);
  }

  // ***REMOVED***
  // ***REMOVED***
  private static final Pattern API_PATTERN = Pattern.compile("\\Aapi(-[a-z0-9\\.]+)?\\.kickstarter.com\\z");

  // environment.***REMOVED***
  private static final Pattern HIVEQUEEN_PATTERN = Pattern.compile("\\A([a-z0-9]+\\-)?[a-z0-9]+\\.dev\\.kickstarter.com\\z");

  // /projects/slug-1/slug-2
  private static final Pattern PROJECT_PATTERN = Pattern.compile("\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/?\\z");

  // /projects/slug-1/slug-2/checkouts/1/thanks
  private static final Pattern CHECKOUT_THANKS_PATTERN = Pattern.compile("\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/checkouts\\/\\d+\\/thanks\\z");

  // /projects/slug-1/slug-2/pledge/new
  private static final Pattern NEW_PLEDGE_PATTERN = Pattern.compile("\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/pledge\\/new\\z");

  // www.kickstarter.com
  // environment.kickstarter.com
  private static final Pattern WEB_PATTERN = Pattern.compile("\\A[a-zA-Z0-9_-]+.kickstarter.com\\z");
}
