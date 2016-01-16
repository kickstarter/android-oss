package com.kickstarter.libs.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.kickstarter.R;
import com.kickstarter.models.Category;
import com.kickstarter.services.DiscoveryParams;

public class DiscoveryUtils {
  private DiscoveryUtils() {}

  public static @Nullable Drawable imageWithOrientation(@NonNull final Category category,
    @NonNull final Context context) {
    final String baseImageName = category.baseImageName();
    if (baseImageName == null) {
      return null;
    }

    final String name = "category_"
      + baseImageName
      + "_"
      + (ViewUtils.isPortrait(context) ? "portrait" : "landscape");

    final @DrawableRes int identifier = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    if (identifier == 0) {
      return null;
    }

    return ContextCompat.getDrawable(context, identifier);
  }

  public static @ColorInt int primaryColor(@NonNull final Context context, @NonNull final DiscoveryParams params) {
    return params.category() != null ?
      params.category().colorWithAlpha() :
      ContextCompat.getColor(context, R.color.discovery_primary);
  }

  public static @ColorInt int secondaryColor(@NonNull final Context context, @NonNull final DiscoveryParams params) {
    return params.category() != null ?
      params.category().secondaryColor(context) :
      ContextCompat.getColor(context, R.color.discovery_secondary);
  }

  public static boolean overlayShouldBeLight(@NonNull final DiscoveryParams params) {
    return params.category() == null || params.category().overlayShouldBeLight();
  }

  public static @ColorInt int overlayTextColor(@NonNull final Context context, @NonNull final DiscoveryParams params) {
    return overlayTextColor(context, overlayShouldBeLight(params));
  }

  public static @ColorInt int overlayTextColor(@NonNull final Context context, final boolean light) {
    final @ColorRes int color = light ? KSColorUtils.lightColorId() : KSColorUtils.darkColorId();
    return ContextCompat.getColor(context, color);
  }
}
