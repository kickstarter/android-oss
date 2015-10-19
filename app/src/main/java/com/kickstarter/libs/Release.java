package com.kickstarter.libs;

import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;

import com.kickstarter.BuildConfig;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class Release {
  final PackageInfo packageInfo;

  public Release(@NonNull final PackageInfo packageInfo) {
    this.packageInfo = packageInfo;
  }

  public DateTime dateTime() {
    return new DateTime(BuildConfig.BUILD_DATE, DateTimeZone.UTC).withZone(DateTimeZone.getDefault());
  }

  public String sha() {
    return BuildConfig.GIT_SHA;
  }

  public Integer versionCode() {
    return packageInfo.versionCode;
  }

  public String versionName() {
    return packageInfo.versionName;
  }

  public String variant() {
    // e.g. internalDebug, externalRelease
    return new StringBuilder().append(BuildConfig.FLAVOR)
      .append(BuildConfig.BUILD_TYPE.substring(0, 1).toUpperCase())
      .append(BuildConfig.BUILD_TYPE.substring(1))
      .toString();
  }
}
