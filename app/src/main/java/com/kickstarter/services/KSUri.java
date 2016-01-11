package com.kickstarter.services;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KSUri {
  public static boolean isApiUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && API_PATTERN.matcher(uri.getHost()).matches();
  }

  public static boolean isCookiesUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getPath().equals("/cookies");
  }

  public static boolean isDiscoverCategoriesPath(@NonNull final String path) {
    return DISCOVER_CATEGORIES_PATTERN.matcher(path).matches();
  }

  public static boolean isDiscoverScopePath(@NonNull final String path, @NonNull final String scope) {
    final Matcher matcher = DISCOVER_SCOPE_PATTERN.matcher(path);
    return matcher.matches() && scope.equals(matcher.group(1));
  }

  public static boolean isDiscoverPlacesPath(@NonNull final String path) {
    return DISCOVER_PLACES_PATTERN.matcher(path).matches();
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

  public static boolean isStagingUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && STAGING_PATTERN.matcher(uri.getHost()).matches();
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
    return isKickstarterUri(uri, webEndpoint) && !isApiUri(uri, webEndpoint);
  }

  // ***REMOVED***
  // ***REMOVED***
  private static final Pattern API_PATTERN = Pattern.compile("\\Aapi(-[a-z0-9\\.]+)?\\.kickstarter.com\\z");

  // /discover/categories/param
  private static final Pattern DISCOVER_CATEGORIES_PATTERN = Pattern.compile("\\A\\/discover\\/categories\\/.*");

  // /discover/param
  private static final Pattern DISCOVER_SCOPE_PATTERN = Pattern.compile("\\A\\/discover\\/([a-zA-Z0-9-_]+)\\z");

  // /discover/places/param
  private static final Pattern DISCOVER_PLACES_PATTERN = Pattern.compile("\\A\\/discover\\/places\\/[a-zA-Z0-9-_]+\\z");

  // environment.***REMOVED***
  private static final Pattern HIVEQUEEN_PATTERN = Pattern.compile("\\A([a-z0-9]+\\-)?[a-z0-9]+\\.dev\\.kickstarter.com\\z");

  // /projects/param-1/param-2
  private static final Pattern PROJECT_PATTERN = Pattern.compile("\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/?\\z");

  // ***REMOVED***
  private static final Pattern STAGING_PATTERN = Pattern.compile("\\Astaging\\.kickstarter\\.com\\z");

  // /projects/param-1/param-2/checkouts/1/thanks
  private static final Pattern CHECKOUT_THANKS_PATTERN = Pattern.compile("\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/checkouts\\/\\d+\\/thanks\\z");

  // /projects/param-1/param-2/pledge/new
  private static final Pattern NEW_PLEDGE_PATTERN = Pattern.compile("\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/pledge\\/new\\z");
}
