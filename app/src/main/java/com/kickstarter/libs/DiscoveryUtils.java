package com.kickstarter.libs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.services.DiscoveryParams;

public class DiscoveryUtils {
  private DiscoveryUtils() {}

  public static @ColorInt int primaryColor(@NonNull final Context context, @NonNull final DiscoveryParams params) {
    return params.category() != null ?
      params.category().colorWithAlpha() :
      context.getResources().getColor(R.color.discovery_primary);
  }

  public static @ColorInt int secondaryColor(@NonNull final Context context, @NonNull final DiscoveryParams params) {
    return params.category() != null ?
      params.category().secondaryColor(context) :
      context.getResources().getColor(R.color.discovery_secondary);
  }

  public static boolean overlayShouldBeLight(@NonNull final DiscoveryParams params) {
    return params.category() == null || params.category().overlayShouldBeLight();
  }

  public static @ColorInt int overlayTextColor(@NonNull final Context context, @NonNull final DiscoveryParams params) {
    return overlayTextColor(context, overlayShouldBeLight(params));
  }

  public static @ColorInt int overlayTextColor(@NonNull final Context context, final boolean light) {
    final @ColorRes int color = light ? KSColorUtils.lightColorId() : KSColorUtils.darkColorId();
    return context.getResources().getColor(color);
  }
}
