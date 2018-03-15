package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.models.Location;
import com.kickstarter.models.User;

public final class UserUtils {
  private UserUtils() {}

  /**
   * Returns whether the user's location setting is in Germany.
   */
  public static boolean isLocationGermany(final @NonNull User user) {
    final Location location = user.location();
    if (location == null) {
      return false;
    }

    return I18nUtils.isCountryGermany(location.country());
  }

  public static boolean userHasChanged(final @Nullable User u1, final @Nullable User u2) {
    if (ObjectUtils.isNull(u1) && ObjectUtils.isNull(u2)) {
      return false;
    } else if (ObjectUtils.isNotNull(u1) && ObjectUtils.isNotNull(u2)) {
      return u1.id() != u2.id();
    }
    return true;
  }
}
