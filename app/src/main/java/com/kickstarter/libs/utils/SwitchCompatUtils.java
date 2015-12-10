package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.View;

public final class SwitchCompatUtils {

  /**
   * Hide toggle animation when switch state is programmatically set to an initial emitted value.
   * http://stackoverflow.com/questions/27139262/change-switch-state-without-animation
   */
  public static void hackToggleAnimation(final @NonNull SwitchCompat switchCompat, final boolean checked) {
    switchCompat.setVisibility(View.INVISIBLE);
    switchCompat.setChecked(checked);
    switchCompat.setVisibility(View.VISIBLE);
  }
}
