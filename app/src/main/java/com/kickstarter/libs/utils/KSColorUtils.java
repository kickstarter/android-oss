package com.kickstarter.libs.utils;

import android.content.Context;
import android.graphics.Color;

import com.kickstarter.R;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

public final class KSColorUtils {
  private KSColorUtils() {}

  private final static float KICKSTARTER_LIGHTNESS_THRESHOLD = 0.72f;

  /**
   * Set the alpha portion of the color.
   *
   * @param color   the (a)rgb color to set an alpha for.
   * @param alpha   the new alpha value, between 0 and 255.
   */
  public static @ColorInt int setAlpha(final int color, @IntRange(from=0, to=255) final int alpha) {
    return (color & 0x00FFFFFF) | (alpha << 24);
  }

  /**
   * Darken the argb color by a percentage.
   *
   * @param color   the argb color to lighten.
   * @param percent percentage to darken by, between 0.0 and 1.0.
   */
  public static @ColorInt int darken(@ColorInt final int color, @FloatRange(from=0.0, to=1.0) final float percent) {
    final float[] hsl = new float[3];
    ColorUtils.colorToHSL(color, hsl);
    hsl[2] -= hsl[2] * percent;
    // HSLToColor sets alpha to fully opaque, so pluck the alpha from the original color.
    return (color & 0xFF000000) | (ColorUtils.HSLToColor(hsl) & 0x00FFFFFF);
  }

  public static @ColorInt int lightColor(final Context context) {
    return ContextCompat.getColor(context, lightColorId());
  }

  public static @ColorRes int lightColorId() {
    return R.color.kds_white;
  }

  /**
   * Lighten the argb color by a percentage.
   *
   * @param color   the argb color to lighten.
   * @param percent percentage to lighten by, between 0.0 and 1.0.
   */
  public static @ColorInt int lighten(@ColorInt final int color, @FloatRange(from=0.0, to=1.0) final float percent) {
    final float[] hsl = new float[3];
    ColorUtils.colorToHSL(color, hsl);
    hsl[2] += (1.0f - hsl[2]) * percent;
    // HSLToColor sets alpha to fully opaque, so pluck the alpha from the original color.
    return (color & 0xFF000000) | (ColorUtils.HSLToColor(hsl) & 0x00FFFFFF);
  }

  /**
   * Check whether a color is light.
   *
   * @param color   the argb color to check.
   */
  public static boolean isLight(@ColorInt final int color) {
    return weightedLightness(color) >= KICKSTARTER_LIGHTNESS_THRESHOLD;
  }

  /**
   * Check whether a color is dark.
   *
   * @param color   the argb color to check.
   */
  public static boolean isDark(@ColorInt final int color) {
    return !isLight(color);
  }

  /*
   * Return a value between 0.0 and 1.0 representing the perceived lightness of the color.
   * More info here: https://robots.thoughtbot.com/closer-look-color-lightness
   */
  private static double weightedLightness(@ColorInt final int color) {
    return ((Color.red(color) * 212.6 + Color.green(color) * 715.2 + Color.blue(color) * 72.2) / 1000) / 255;
  }
}
