package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;

import com.kickstarter.models.Project;
import com.kickstarter.models.User;

public final class ProjectUtils {
  private ProjectUtils() {}

  public static boolean userIsCreator(@NonNull final Project project, @NonNull final User user) {
    return project.creator().id() == user.id();
  }
}
