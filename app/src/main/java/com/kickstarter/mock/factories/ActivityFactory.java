package com.kickstarter.mock.factories;

import com.kickstarter.models.Activity;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;

public final class ActivityFactory {
  private ActivityFactory() {}

  public static Activity activity() {
    return Activity.builder()
      .category(Activity.CATEGORY_WATCH)
      .createdAt(new DateTime(123))
      .id(IdFactory.id())
      .updatedAt(new DateTime(456))
      .project(ProjectFactory.project())
      .user(UserFactory.user())
      .build();
  }

  public static @NonNull Activity friendBackingActivity() {
    return activity().toBuilder()
      .category(Activity.CATEGORY_BACKING)
      .build();
  }

  public static @NonNull Activity projectStateChangedActivity() {
    return activity().toBuilder()
      .category(Activity.CATEGORY_FAILURE)
      .project(ProjectFactory.failedProject())
      .build();
  }

  public static @NonNull Activity projectStateChangedPositiveActivity() {
    return activity().toBuilder()
      .category(Activity.CATEGORY_SUCCESS)
      .project(ProjectFactory.successfulProject())
      .build();
  }

  public static @NonNull Activity updateActivity() {
    return activity().toBuilder()
      .category(Activity.CATEGORY_UPDATE)
      .project(ProjectFactory.project())
      .user(UserFactory.user())
      .build();
  }
}
