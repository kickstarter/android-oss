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
      public final String more_activities;
      public final String newer_activities;

      private ApiEnvelope(final String more_activities, final String newer_activities) {
        this.more_activities = more_activities;
        this.newer_activities = newer_activities;
      }
    }
  }
}
