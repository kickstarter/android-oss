package com.kickstarter.libs;

import android.content.pm.PackageInfo;

import com.kickstarter.BuildConfig;

public class Build {
  final PackageInfo packageInfo;

  public Build(final PackageInfo packageInfo) {
    this.packageInfo = packageInfo;
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
