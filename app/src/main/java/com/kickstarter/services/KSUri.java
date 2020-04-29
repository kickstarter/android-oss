package com.kickstarter.services;

import android.net.Uri;

import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.libs.utils.UriUtilsKt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

public final class KSUri {
  private KSUri() {}

  public static boolean isApiUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && Secrets.RegExpPattern.API.matcher(UriUtilsKt.host(uri)).matches();
  }

  public static boolean isCheckoutUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && NATIVE_CHECKOUT_PATTERN.matcher(UriUtilsKt.path(uri)).matches();
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

  public static boolean isHivequeenUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && Secrets.RegExpPattern.HIVEQUEEN.matcher(UriUtilsKt.host(uri)).matches();
  }

  public static boolean isKickstarterUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return UriUtilsKt.host(uri).equals(Uri.parse(webEndpoint).getHost());
  }

  public static boolean isKSFavIcon(final Uri uri, final String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && UriUtilsKt.lastPathSegment(uri).equals("favicon.ico");
  }

  public static boolean isWebViewUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && !isKSFavIcon(uri, webEndpoint);
  }

  public static boolean isNewGuestCheckoutUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && NEW_GUEST_CHECKOUT_PATTERN.matcher(UriUtilsKt.path(uri)).matches();
  }

  public static boolean isProjectUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && PROJECT_PATTERN.matcher(UriUtilsKt.path(uri)).matches();
  }

  public static boolean isProjectPreviewUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isProjectUri(uri, webEndpoint) && ObjectUtils.isNotNull(uri.getQueryParameter("token"));
  }

  public static boolean isSignupUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && UriUtilsKt.path(uri).equals("/signup");
  }

  public static boolean isStagingUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && Secrets.RegExpPattern.STAGING.matcher(UriUtilsKt.host(uri)).matches();
  }

  public static boolean isCheckoutThanksUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && CHECKOUT_THANKS_PATTERN.matcher(UriUtilsKt.path(uri)).matches();
  }

  public static boolean isModalUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && uri.getQueryParameter("modal") != null && uri.getQueryParameter("modal").equals("true");
  }

  public static boolean isProjectSurveyUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && PROJECT_SURVEY.matcher(UriUtilsKt.path(uri)).matches();
  }

  public static boolean isProjectUpdateCommentsUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && PROJECT_UPDATE_COMMENTS_PATTERN.matcher(UriUtilsKt.path(uri)).matches();
  }

  public static boolean isProjectUpdateUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && PROJECT_UPDATE_PATTERN.matcher(UriUtilsKt.path(uri)).matches();
  }

  public static boolean isProjectUpdatesUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && PROJECT_UPDATES_PATTERN.matcher(UriUtilsKt.path(uri)).matches();
  }

  public static boolean isUserSurveyUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && USER_SURVEY.matcher(UriUtilsKt.path(uri)).matches();
  }

  public static boolean isWebUri(final @NonNull Uri uri, final @NonNull String webEndpoint) {
    return isKickstarterUri(uri, webEndpoint) && !isApiUri(uri, webEndpoint);
  }

  // /projects/:creator_param/:project_param/checkouts/1/thanks
  private static final Pattern CHECKOUT_THANKS_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/checkouts\\/\\d+\\/thanks\\z"
  );

  // /discover/categories/param
  private static final Pattern DISCOVER_CATEGORIES_PATTERN = Pattern.compile("\\A\\/discover\\/categories\\/.*");

  // /discover/param
  private static final Pattern DISCOVER_SCOPE_PATTERN = Pattern.compile("\\A\\/discover\\/([a-zA-Z0-9-_]+)\\z");

  // /discover/places/param
  private static final Pattern DISCOVER_PLACES_PATTERN = Pattern.compile("\\A\\/discover\\/places\\/[a-zA-Z0-9-_]+\\z");

  //  /projects/:creator_param/:project_param/pledge
  private static final Pattern NATIVE_CHECKOUT_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/pledge\\z"
  );

  // /checkouts/:checkout_id/guest/new
  private static final Pattern NEW_GUEST_CHECKOUT_PATTERN = Pattern.compile(
    "\\A\\/checkouts\\/[a-zA-Z0-9-_]+\\/guest\\/new\\z"
  );

  // /projects/:creator_param/:project_param
  private static final Pattern PROJECT_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/?\\z"
  );

  //  /projects/:creator_param/:project_param/surveys/:survey_param
  private static final Pattern PROJECT_SURVEY = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/surveys\\/[a-zA-Z0-9-_]+\\z"
  );

  // /projects/:creator_param/:project_param/posts/:update_param/comments
  private static final Pattern PROJECT_UPDATE_COMMENTS_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/posts\\/[a-zA-Z0-9-_]+\\/comments\\z"
  );

  // /projects/:creator_param/:project_param/posts/:update_param
  private static final Pattern PROJECT_UPDATE_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/posts\\/[a-zA-Z0-9-_]+\\z"
  );

  // /projects/:creator_param/:project_param/posts
  private static final Pattern PROJECT_UPDATES_PATTERN = Pattern.compile(
    "\\A\\/projects(\\/[a-zA-Z0-9_-]+)?\\/[a-zA-Z0-9_-]+\\/posts\\z"
  );

  // /users/:user_param/surveys/:survey_response_id": userSurvey
  private static final Pattern USER_SURVEY = Pattern.compile(
    "\\A\\/users(\\/[a-zA-Z0-9_-]+)?\\/surveys\\/[a-zA-Z0-9-_]+\\z"
  );
}
