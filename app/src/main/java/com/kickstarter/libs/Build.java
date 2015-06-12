package com.kickstarter.libs;

import android.content.pm.PackageInfo;

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
}
