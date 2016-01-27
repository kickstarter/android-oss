package com.kickstarter.libs.utils;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.kickstarter.R;

public final class TransitionUtils {
  /**
   * Slides a new activity in from the right, fades and slides the existing activity out to the left.
   *
   * @param activity The activity that started the new intent.
   */
  public static void slideInFromRight(final @NonNull Activity activity) {
    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
