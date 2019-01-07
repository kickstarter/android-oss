package com.kickstarter.libs.utils;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import rx.functions.Action1;

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

  /**
   * Set toggle state without any animations.
   */
  public static Action1<Boolean> setCheckedWithoutAnimation(final @NonNull SwitchCompat switchCompat) {
    return (checked) -> setCheckedWithoutAnimation(switchCompat, checked);
  }
}
