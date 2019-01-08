package com.kickstarter.mock.factories;

import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;

import java.util.List;

import androidx.annotation.NonNull;

final public class ProjectsEnvelopeFactory {
  private ProjectsEnvelopeFactory() {}

  public static @NonNull ProjectsEnvelope projectsEnvelope(final @NonNull List<Project> projects) {
    return ProjectsEnvelope.builder()
      .projects(projects)
      .urls(
        ProjectsEnvelope.UrlsEnvelope.builder()
          .api(ProjectsEnvelope.UrlsEnvelope.ApiEnvelope.builder().moreProjects("").build())
        .build()
      )
      .build();
  }
}
