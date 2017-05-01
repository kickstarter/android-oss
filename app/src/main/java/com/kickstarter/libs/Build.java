package com.kickstarter.libs;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;

import com.kickstarter.BuildConfig;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Locale;

import hu.supercluster.paperwork.Paperwork;

public final class Build {
  private final PackageInfo packageInfo;

  public Build(final @NonNull PackageInfo packageInfo) {
    this.packageInfo = packageInfo;
  }

  public @NonNull String applicationId() {
    return packageInfo.packageName;
  }

  public DateTime dateTime(final @NonNull Context context) {
    return new DateTime(
      new Paperwork(context).get("BUILD_DATE"),
      DateTimeZone.UTC).withZone(DateTimeZone.getDefault()
    );
  }

  public static boolean isInternal() {
    return BuildConfig.FLAVOR_AUDIENCE.equals("internal");
  }

  public static boolean isExternal() {
    return !isInternal();
  }

  /**
   * Returns `true` if the build is compiled in debug mode, `false` otherwise.
   */
  public boolean isDebug() {
    return BuildConfig.DEBUG;
  }

  /**
   * Returns `true` if the build is compiled in release mode, `false` otherwise.
   */
  public boolean isRelease() {
    return !BuildConfig.DEBUG;
  }

  public String sha(final @NonNull Context context) {
    return new Paperwork(context).get("GIT_SHA");
  }

  public Integer versionCode() {
    return packageInfo.versionCode;
  }

  public String versionName() {
    return packageInfo.versionName;
  }

  public String variant() {
    // e.g. internalDebug, externalRelease
    return new StringBuilder().append(BuildConfig.FLAVOR_AUDIENCE)
      .append(BuildConfig.BUILD_TYPE.substring(0, 1).toUpperCase(Locale.US))
      .append(BuildConfig.BUILD_TYPE.substring(1))
      .toString();
  }
}
