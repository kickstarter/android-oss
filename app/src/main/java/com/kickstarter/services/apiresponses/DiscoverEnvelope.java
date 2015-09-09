package com.kickstarter.services.apiresponses;

import com.kickstarter.models.Project;

import java.util.List;

public class DiscoverEnvelope {
  public final List<Project> projects;
  public final UrlsEnvelope urls;

  private DiscoverEnvelope(final List<Project> projects, final UrlsEnvelope urls) {
    this.projects = projects;
    this.urls = urls;
  }

  public static class UrlsEnvelope {
    public final ApiEnvelope api;
    private UrlsEnvelope(final ApiEnvelope api) {
      this.api = api;
    }

    public static class ApiEnvelope {
      public final String moreProjects;
      private ApiEnvelope(final String moreProjects) {
        this.moreProjects = moreProjects;
      }
    }
  }
}
