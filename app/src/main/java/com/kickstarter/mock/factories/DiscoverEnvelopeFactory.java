package com.kickstarter.mock.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;

import java.util.List;

public final class DiscoverEnvelopeFactory {
  private DiscoverEnvelopeFactory() {}

  public static @NonNull DiscoverEnvelope discoverEnvelope(final @NonNull List<Project> projects) {
    return DiscoverEnvelope.builder()
      .projects(projects)
      .urls(
        DiscoverEnvelope.UrlsEnvelope.builder()
          .api(DiscoverEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("").build())
          .build()
      )
      .build();
  }
}
