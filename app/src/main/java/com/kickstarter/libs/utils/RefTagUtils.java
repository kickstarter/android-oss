package com.kickstarter.libs.utils;

import android.content.SharedPreferences;
import android.util.Pair;

import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

import org.joda.time.DateTime;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class RefTagUtils {
  private RefTagUtils() {}

  private static final @NonNull String COOKIE_VALUE_SEPARATOR = "%3F";

  /**
   * Name of the cookie that should store the ref tag for a particular project. Fits the template:
   *
   * ref_{project_pid}
   */
  protected static @NonNull String cookieNameForProject(final @NonNull Project project) {
    return "ref_" + project.id();
  }

  /**
   * Value to store in the ref tag cookie. Fits the template:
   *
   * {ref_tag} + {separator} + {time_of_setting}
   */
  protected static @NonNull String cookieValueForRefTag(final @NonNull RefTag refTag) {
    return refTag.tag() + COOKIE_VALUE_SEPARATOR + String.valueOf(SystemUtils.secondsSinceEpoch());
  }

  /**
   * If a ref tag cookie has been stored for this project this returns the ref tag embedded in the cookie. If a
   * cookie has not yet been set it returns `null`.
   */
  public static @Nullable RefTag storedCookieRefTagForProject(final @NonNull Project project,
    final @NonNull CookieManager cookieManager, final @NonNull SharedPreferences sharedPreferences) {

    final HttpCookie cookie = findRefTagCookieForProject(project, cookieManager, sharedPreferences);
    if (cookie == null) {
      return null;
    }

    final String[] components = cookie.getValue()
      .split(COOKIE_VALUE_SEPARATOR);

    if (components.length > 0) {
      return RefTag.from(components[0]);
    }

    return null;
  }

  /**
   * Constructs a cookie for the given ref tag and project. This method can return `null` if a cookie cannot be
   * constructed, e.g. the project has a malformed project url.
   */
  public static @Nullable HttpCookie buildCookieWithRefTagAndProject(final @NonNull RefTag refTag,
    final @NonNull Project project) {

    return buildCookieWithValueAndProject(cookieValueForRefTag(refTag), project);
  }

  /**
   * Constructs a cookie for the given cookie value and project. This method can return `null` if a cookie cannot be
   * constructed, e.g. the project has a malformed project url.
   */
  private static @Nullable HttpCookie buildCookieWithValueAndProject(final @NonNull String cookieValue,
    final @NonNull Project project) {

    final HttpCookie cookie = new HttpCookie(cookieNameForProject(project), cookieValue);

    // Try extracting the path and domain for the cookie from the project.
    try {
      final URL url = new URL(project.webProjectUrl());
      cookie.setPath(url.getPath());
      cookie.setDomain(url.getHost());
    } catch (MalformedURLException e) {
      return null;
    }

    cookie.setVersion(0);

    // Cookie expires on the project deadline, or some days into the future if there is no deadline.
    final DateTime deadline = project.deadline();
    if (deadline != null) {
      cookie.setMaxAge(ProjectUtils.timeInSecondsUntilDeadline(project));
    } else {
      cookie.setMaxAge(new DateTime().plusDays(10).getMillis() / 1000l);
    }

    return cookie;
  }

  /**
   * Converts a pair (params, project) into a (project, refTag) pair that does some extra logic around
   * featured projects.
   */
  public static @NonNull Pair<Project, RefTag> projectAndRefTagFromParamsAndProject(final @NonNull DiscoveryParams params,
    final @NonNull Project project) {
    final RefTag refTag = project.isFeaturedToday() ? RefTag.categoryFeatured() : DiscoveryParamsUtils.refTag(params);
    return new Pair<>(project, refTag);
  }

  /**
   * Stores the ref tag in a cookie and shared preference for the project.
   */
  public static void storeCookie(final @NonNull RefTag refTag, final @NonNull Project project,
    final @NonNull CookieManager cookieManager, final @NonNull SharedPreferences sharedPreferences) {

    final HttpCookie cookie = buildCookieWithRefTagAndProject(refTag, project);
    cookieManager.getCookieStore().add(null, cookie);

    if (cookie != null) {
      final SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putString(cookie.getName(), cookie.getValue());
      editor.apply();
    }
  }

  /**
   * Finds the ref tag cookie associated with a project. Returns `null` if no cookie has yet been set.
   */
  protected static @Nullable HttpCookie findRefTagCookieForProject(final @NonNull Project project,
    final @NonNull CookieManager cookieManager, final @NonNull SharedPreferences sharedPreferences) {

    final String cookieName = cookieNameForProject(project);

    // First try finding the cookie in the cookie store
    final CookieStore cookieStore = cookieManager.getCookieStore();
    for (final HttpCookie cookie : cookieStore.getCookies()) {
      if (cookieName.equals(cookie.getName())) {
        return cookie;
      }
    }

    // If we can't find it in the cookie store let's look in shared prefs
    final String cookieValue = sharedPreferences.getString(cookieName, null);
    if (cookieValue != null) {
      return buildCookieWithValueAndProject(cookieValue, project);
    }

    return null;
  }
}
