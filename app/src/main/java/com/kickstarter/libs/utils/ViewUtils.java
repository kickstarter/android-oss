package com.kickstarter.libs.utils;

import android.content.Context;
import android.content.res.Configuration;

public class ViewUtils {
  public ViewUtils() {}

  public static boolean isLandscape(final Context context) {
    return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
  }

  public static boolean isPortrait(final Context context) {
    return !isLandscape(context);
  }
}
