package com.kickstarter.services.apiresponses;

import com.kickstarter.models.Project;
import com.kickstarter.models.User;

public class StarEnvelope {
  private User user;
  private Project project;

  public User user() {
    return user;
  }

  public Project project() {
    return project;
  }
}
