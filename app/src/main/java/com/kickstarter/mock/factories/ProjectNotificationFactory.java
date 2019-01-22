package com.kickstarter.mock.factories;

import com.kickstarter.models.ProjectNotification;

import androidx.annotation.NonNull;

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
      .id(IdFactory.id())
      .email(true)
      .mobile(true)
      .project(project())
      .urls(urls())
      .build();
  }

  private static @NonNull ProjectNotification.Project project() {
    return ProjectNotification.Project.builder().id(IdFactory.id()).name("SKULL GRAPHIC TEE").build();
  }

  private static @NonNull ProjectNotification.Urls urls() {
    final ProjectNotification.Urls.Api api = ProjectNotification.Urls.Api.builder().notification("/url").build();
    return ProjectNotification.Urls.builder().api(api).build();
  }
}
