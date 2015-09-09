package com.kickstarter.libs;

public class ApiCapabilities {
  public static boolean canDebugWebViews() {
    return android.os.Build.VERSION.SDK_INT >= 19;
  }

  public static boolean canEvaluateJavascript() {
    return android.os.Build.VERSION.SDK_INT >= 19;
  }
}
