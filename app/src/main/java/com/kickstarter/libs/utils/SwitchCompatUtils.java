package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.View;

public final class SwitchCompatUtils {
  private SwitchCompatUtils() {}

  /**
   * Set toggle state without any animations.
   */
  public static void setCheckedWithoutAnimation(final @NonNull SwitchCompat switchCompat, final boolean checked) {
    switchCompat.setVisibility(View.INVISIBLE);
    switchCompat.setChecked(checked);
    switchCompat.setVisibility(View.VISIBLE);
  }
}
