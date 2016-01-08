package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Project;

import org.joda.time.DateTime;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static com.kickstarter.libs.utils.SystemUtils.*;

public final class RefTagUtils {
  private RefTagUtils() {}

  static final @NonNull String COOKIE_VALUE_SEPARATOR = "%3F";

  /**
   * Name of the cookie that should store the ref tag for a particular project.
   */
  protected static @NonNull String cookieNameForProject(final @NonNull Project project) {
    return "ref_" + project.id();
  }

  /**
   * Value to store in the ref tag cookie.
   */
  protected static @NonNull String cookieValueForRefTag(final @NonNull RefTag refTag) {
    return refTag.tag() + COOKIE_VALUE_SEPARATOR + String.valueOf(secondsSinceEpoch());
  }

  /**
     * Finds the ref tag cookie associated with a project. Returns `null` if no cookie has yet been set.
   */
  public static @Nullable HttpCookie findRefTagCookieForProject(final @NonNull Project project, final @NonNull CookieManager cookieManager) {
    final CookieStore cookieStore = cookieManager.getCookieStore();
    final List<HttpCookie> cookies = cookieStore.getCookies();

    for (final HttpCookie cookie : cookies) {
      if (cookieNameForProject(project).equals(cookie.getName())) {
        return cookie;
      }
    }

    return null;
  }

  /**
   * If a ref tag cookie has been stored for this project this returns the ref tag embedded in the cookie. If a
   * cookie has not yet been set it returns `null`.
   */
  public static @Nullable RefTag storedCookieRefTagForProject(final @NonNull Project project, final @NonNull CookieManager cookieManager) {

    final HttpCookie cookie = findRefTagCookieForProject(project, cookieManager);
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
  public static @Nullable HttpCookie buildCookieForRefTagAndProject(final @NonNull RefTag refTag, final @NonNull Project project) {
    final HttpCookie cookie = new HttpCookie(cookieNameForProject(project), cookieValueForRefTag(refTag));

    // Try extracting the path and domain for the cookie from the project.
    try {
      final URL url = new URL(project.webProjectUrl());
      cookie.setPath(url.getPath());/**/
      cookie.setDomain(url.getHost());
    } catch (MalformedURLException e) {
      return null;
    }

    cookie.setVersion(0);

    // Cookie expires on the project deadline, or some days into the future if there is no deadline.
    final DateTime deadline = project.deadline();
    if (deadline != null) {
      cookie.setMaxAge(deadline.getMillis() / 1000l);
    } else {
      cookie.setMaxAge(new DateTime().plusDays(10).getMillis() / 1000l);
    }

    return cookie;
  }
}
