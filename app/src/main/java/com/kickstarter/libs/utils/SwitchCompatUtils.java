package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.View;

public final class SwitchCompatUtils {

  /**
   * Set toggle state without any animations.
   */
  public static void setCheckedWithoutAnimation(final @NonNull SwitchCompat switchCompat, final boolean checked) {
    switchCompat.setChecked(checked);
    switchCompat.clearAnimation();
  }
}
