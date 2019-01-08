package com.kickstarter.mock.factories;

import com.kickstarter.models.Activity;
import com.kickstarter.services.apiresponses.ActivityEnvelope;

import java.util.List;

import androidx.annotation.NonNull;

public final class ActivityEnvelopeFactory {
  private ActivityEnvelopeFactory() {}

  public static @NonNull ActivityEnvelope activityEnvelope(final @NonNull List<Activity> activities) {
    return ActivityEnvelope.builder()
      .activities(activities)
      .urls(
        ActivityEnvelope.UrlsEnvelope.builder()
          .api(ActivityEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreActivities("").build())
          .build()
      )
      .build();
  }
}
