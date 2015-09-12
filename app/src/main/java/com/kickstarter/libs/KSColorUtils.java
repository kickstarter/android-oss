package com.kickstarter.libs;

import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;

public class KSColorUtils {
  private KSColorUtils() {}

  /**
   * Darken the argb color by a percentage.
   *
   * @param color   the argb color to lighten.
   * @param percent percentage to darken by, between 0.0 and 1.0.
   */
  public static int darken(final int color, final float percent) {
    final float[] hsl = new float[3];
    ColorUtils.colorToHSL(color, hsl);
    hsl[2] -= (hsl[2] * percent);
    // HSLToColor sets alpha to fully opaque, so pluck the alpha from the original color.
    return (color & 0xFF000000) | (ColorUtils.HSLToColor(hsl) & 0x00FFFFFF);
  }

  /**
   * Lighten the argb color by a percentage.
   *
   * @param color   the argb color to lighten.
   * @param percent percentage to lighten by, between 0.0 and 1.0.
   */
  public static int lighten(final int color, final float percent) {
    final float[] hsl = new float[3];
    ColorUtils.colorToHSL(color, hsl);
    hsl[2] += (1.0f - hsl[2]) * percent;
    // HSLToColor sets alpha to fully opaque, so pluck the alpha from the original color.
    return (color & 0xFF000000) | (ColorUtils.HSLToColor(hsl) & 0x00FFFFFF);
  }
}
