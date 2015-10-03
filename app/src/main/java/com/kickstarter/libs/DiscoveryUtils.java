package com.kickstarter.libs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.services.DiscoveryParams;

public class DiscoveryUtils {
  private DiscoveryUtils() {}

  public static int primaryColor(@NonNull final Context context, @NonNull final DiscoveryParams params) {
    return params.category() != null ?
      params.category().colorWithAlpha() :
      context.getResources().getColor(R.color.discovery_primary);
  }

  public static int secondaryColor(@NonNull final Context context, @NonNull final DiscoveryParams params) {
    return params.category() != null ?
      params.category().secondaryColor(context) :
      context.getResources().getColor(R.color.discovery_secondary);
  }

  public static boolean overlayShouldBeLight(@NonNull final DiscoveryParams params) {
    return params.category() == null || params.category().overlayShouldBeLight();
  }

  public static int overlayTextColor(@NonNull final Context context, @NonNull final DiscoveryParams params) {
    return overlayTextColor(context, overlayShouldBeLight(params));
  }

  public static int overlayTextColor(@NonNull final Context context, final boolean light) {
    final int color = light ? lightColorId() : darkColorId();
    return context.getResources().getColor(color);
  }

  public static int lightColorId() {
    return R.color.white;
  }

  public static int darkColorId() {
    return R.color.text_dark;
  }
}
