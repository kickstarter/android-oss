package com.kickstarter.libs;

public class ApiCapabilities {
  public static boolean debugWebViews() {
    return android.os.Build.VERSION.SDK_INT >= 19;
  }

  public static boolean evaluateJavascript() {
    return android.os.Build.VERSION.SDK_INT >= 19;
  }
}
