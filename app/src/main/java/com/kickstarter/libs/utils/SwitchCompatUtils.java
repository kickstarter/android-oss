package com.kickstarter.libs.utils;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

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
