package com.kickstarter.libs;

public class ApiCapabilities {
  private ApiCapabilities() {}

  public static boolean canDebugWebViews() {
    return android.os.Build.VERSION.SDK_INT >= 19;
  }

  public static boolean canEvaluateJavascript() {
    return android.os.Build.VERSION.SDK_INT >= 19;
  }

  public static boolean canSetDarkStatusBarIcons() {
    return android.os.Build.VERSION.SDK_INT >= 23;
  }

  public static boolean canSetStatusBarColor() {
    return android.os.Build.VERSION.SDK_INT >= 21;
  }
}
