package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public final class AnimationUtils {
  private AnimationUtils() {}

  public static @NonNull Animation loadingIndicatorOnPageFinished() {
    final AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
    animation.setDuration(300L);
    animation.setFillAfter(true);
    return animation;
  }

  public static @NonNull Animation loadingIndicatorOnPageStarted() {
    final AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
    animation.setDuration(300L);
    animation.setFillAfter(true);
    return animation;
  }
}
