package com.kickstarter.factories;

import android.support.annotation.NonNull;

import com.kickstarter.models.ProjectNotification;

public final class ProjectNotificationFactory {
  private ProjectNotificationFactory() {}

  public static @NonNull ProjectNotification disabled() {
    return enabled().toBuilder()
      .email(false)
      .mobile(false)
      .build();
  }

  public static @NonNull ProjectNotification enabled() {
    return ProjectNotification.builder()
      .id(1)
      .email(true)
      .mobile(true)
      .project(project())
      .urls(urls())
      .build();
  }

  private static @NonNull ProjectNotification.Project project() {
    return ProjectNotification.Project.builder().id(1).name("SKULL GRAPHIC TEE").build();
  }

  private static @NonNull ProjectNotification.Urls urls() {
    final ProjectNotification.Urls.Api api = ProjectNotification.Urls.Api.builder().notification("/url").build();
    return ProjectNotification.Urls.builder().api(api).build();
  }
}
