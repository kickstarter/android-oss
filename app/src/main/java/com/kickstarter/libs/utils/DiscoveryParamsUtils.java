package com.kickstarter.libs.utils;

import com.kickstarter.libs.RefTag;
import com.kickstarter.services.DiscoveryParams;

import androidx.annotation.NonNull;

import static com.kickstarter.libs.utils.BooleanUtils.isTrue;
import static com.kickstarter.libs.utils.IntegerUtils.isNonZero;

public final class DiscoveryParamsUtils {
  private DiscoveryParamsUtils() {}

  /**
   * A `ref_tag` representation of some discovery params. This tag can be used to attribute a checkout when a user
   * pledges from discovery using these particular params.
   */
  public static @NonNull RefTag refTag(final @NonNull DiscoveryParams params) {
    if (params.isCategorySet()) {
      final DiscoveryParams.Sort sort = params.sort();
      if (sort != null) {
        return RefTag.category(sort);
      }
      return RefTag.category();
    }

    if (params.location() != null) {
      return RefTag.city();
    }

    final boolean staffPicks = isTrue(params.staffPicks());
    if (staffPicks) {
      final DiscoveryParams.Sort sort = params.sort();
      if (sort != null) {
        return RefTag.recommended(sort);
      }
      return RefTag.recommended();
    }

    final Integer tagId = params.tagId();
    if (tagId != null) {
      return RefTag.collection(tagId);
    }

    if (isNonZero(params.social())) {
      return RefTag.social();
    }

    if (params.term() != null) {
      return RefTag.search();
    }

    return RefTag.discovery();
  }
}
