package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;

import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;

public final class BackingUtils {
  private BackingUtils() {}

  public static boolean isBacked(final @NonNull Project project, final @NonNull Reward reward) {
    final Backing backing = project.backing();
    if (backing == null) {
      return false;
    }

    final Long rewardId = backing.rewardId();
    if (rewardId == null) {
      return false;
    }

    return rewardId == reward.id();
  }
}
