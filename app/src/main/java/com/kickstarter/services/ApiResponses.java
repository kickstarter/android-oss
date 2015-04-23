package com.kickstarter.services;

import com.kickstarter.models.Project;
import java.util.List;

public class ApiResponses {
  /**
   * A lightweight class whose schema resembles that
   * of the API response for discovery endpoints.
   */
  public static class DiscoverEnvelope {
    public final List<Project> projects;
    public final UrlsEnvelope urls;
    private DiscoverEnvelope(List<Project> projects, UrlsEnvelope urls) {
      this.projects = projects;
      this.urls = urls;
    }

    public static class UrlsEnvelope {
      public final ApiEnvelope api;
      private UrlsEnvelope(ApiEnvelope api) { this.api = api; }

      public static class ApiEnvelope {
        public final String more_projects;
        private ApiEnvelope(String more_projects) { this.more_projects = more_projects; }
      }
    }
  }
}
