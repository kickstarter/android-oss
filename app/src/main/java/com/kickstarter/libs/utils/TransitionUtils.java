package com.kickstarter.libs.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.R;

public final class TransitionUtils {
  public static void transition(final @NonNull Activity activity, final @NonNull Pair<Integer, Integer> transitions) {
    activity.overridePendingTransition(transitions.first, transitions.second);
  }

  public static @NonNull Pair<Integer, Integer> slideInFromRight() {
    return Pair.create(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public static @NonNull Pair<Integer, Integer> slideInFromLeft() {
    return Pair.create(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }
}
