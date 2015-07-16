package com.kickstarter.services;

import com.kickstarter.models.Activity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ActivityFeedParams {

  public static EnumSet<Activity.Category> categories() {
    return EnumSet.of(
      Activity.Category.BACKING,
      Activity.Category.CANCELLATION,
      Activity.Category.FAILURE,
      Activity.Category.LAUNCH,
      Activity.Category.SUCCESS,
      Activity.Category.UPDATE,
      Activity.Category.FOLLOW
    );
  }

  public List<String> queryParams() {
    final List<String> params = new ArrayList<>();
    for (Activity.Category category : categories()) {
      params.add(category.toString().toLowerCase());
    }
    return params;
  }
}
