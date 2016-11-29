package com.kickstarter.services;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.kickstarter.libs.utils.Secrets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KSUri {
  private KSUri() {}

  public static boolean isAndroidPayUri(@NonNull final Uri uri, @NonNull final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint)
      && (Secrets.ANDROID_PAY_PATTERN_1.matcher(uri.getPath()).matches()
        || Secrets.ANDROID_PAY_PATTERN_2.matcher(uri.getPath()).matches());
  }

  public static boolean isApiUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && Secrets.API_PATTERN.matcher(uri.getHost()).matches();
  }

  public static boolean isCookiesUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getPath().equals("/cookies");
  }

  public static boolean isDiscoverCategoriesPath(final @NonNull String path) {
    return DISCOVER_CATEGORIES_PATTERN.matcher(path).matches();
  }

  public static boolean isDiscoverScopePath(final @NonNull String path, final @NonNull String scope) {
    final Matcher matcher = DISCOVER_SCOPE_PATTERN.matcher(path);
    return matcher.matches() && scope.equals(matcher.group(1));
  }

  public static boolean isDiscoverPlacesPath(final @NonNull String path) {
    return DISCOVER_PLACES_PATTERN.matcher(path).matches();
  }

  public static boolean isHelloUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getPath().equals("/hello");
  }

  public static boolean isHivequeenUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && Secrets.HIVEQUEEN_PATTERN.matcher(uri.getHost()).matches();
  }

  public static boolean isKickstarterUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return uri.getHost().equals(Uri.parse(webEndpoint).getHost());
  }

  public static boolean isProjectUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && PROJECT_PATTERN.matcher(uri.getPath()).matches();
  }

  public static boolean isSignupUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getPath().equals("/signup");
  }

  public static boolean isStagingUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && STAGING_PATTERN.matcher(uri.getHost()).matches();
  }

  public static boolean isCheckoutThanksUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && CHECKOUT_THANKS_PATTERN.matcher(uri.getPath()).matches();
  }

  public static boolean isModalUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getQueryParameter("modal") != null && uri.getQueryParameter("modal").equals("true");
  }

  public static boolean isPrivacyUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getPath().equals("/privacy");
  }

  public static boolean isProjectNewPledgeUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && NEW_PLEDGE_PATTERN.matcher(uri.getPath()).matches();
  }

  public static boolean isTermsOfUseUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getPath().equals("/terms-of-use");
  }

  public static boolean isWebUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && !isApiUri(uri, webEndpoint);
  }

  // /discover/categories/param
  private static final Pattern DISCOVER_CATEGORIES_PATTERN = Pattern.compile("\\A\\/discover\\/categories\\/.*");

  // /discover/param
  private static final Pattern DISCOVER_SCOPE_PATTERN = Pattern.compile("\\A\\/discover\\/([a-zA-Z0-9-_]+)\\z");

  // /discover/places/param
  private static final Pattern DISCOVER_PLACES_PATTERN = Pattern.compile("\\A\\/discover\\/places\\/[a-zA-Z0-9-_]+\\z");

  // /projects/param-1/param-2
  private static final Pattern PROJECT_PATTERN = Pattern.compile("\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/?\\z");

  // ***REMOVED***
  private static final Pattern STAGING_PATTERN = Pattern.compile("\\Astaging\\.kickstarter\\.com\\z");

  // /projects/param-1/param-2/checkouts/1/thanks
  private static final Pattern CHECKOUT_THANKS_PATTERN = Pattern.compile("\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/checkouts\\/\\d+\\/thanks\\z");

  // /projects/param-1/param-2/pledge/new
  private static final Pattern NEW_PLEDGE_PATTERN = Pattern.compile("\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/pledge\\/new\\z");
}
