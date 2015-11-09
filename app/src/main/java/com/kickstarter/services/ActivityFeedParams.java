package com.kickstarter.services;

import com.kickstarter.models.Activity;

import java.util.Arrays;
import java.util.List;

public final class ActivityFeedParams {
  public ActivityFeedParams() {}

  public List<String> queryParams() {
    return Arrays.asList(
      Activity.CATEGORY_BACKING,
      Activity.CATEGORY_CANCELLATION,
      Activity.CATEGORY_FAILURE,
      Activity.CATEGORY_LAUNCH,
      Activity.CATEGORY_SUCCESS,
      Activity.CATEGORY_UPDATE,
      Activity.CATEGORY_FOLLOW
    );
  }
}
