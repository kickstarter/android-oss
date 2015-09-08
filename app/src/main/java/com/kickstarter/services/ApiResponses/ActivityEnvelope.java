package com.kickstarter.services.ApiResponses;

import com.kickstarter.models.Activity;

import java.util.List;

public class ActivityEnvelope {
  public final List<Activity> activities;
  public final UrlsEnvelope urls;

  private ActivityEnvelope(final List<Activity> activities, final UrlsEnvelope urls) {
    this.activities = activities;
    this.urls = urls;
  }

  public static class UrlsEnvelope {
    public final ApiEnvelope api;
    private UrlsEnvelope(final ApiEnvelope api) {
      this.api = api;
    }

    public static class ApiEnvelope {
      public final String moreActivities;
      public final String newerActivities;

      private ApiEnvelope(final String moreActivities, final String newerActivities) {
        this.moreActivities = moreActivities;
        this.newerActivities = newerActivities;
      }
    }
  }
}
