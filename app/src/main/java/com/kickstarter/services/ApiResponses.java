package com.kickstarter.services;

import com.kickstarter.models.Project;
import java.util.List;

/**
 * Created by brandon on 2/23/15.
 */
/*package*/ class ApiResponses {
  /**
   * A lightweight class whose scheme resembles that
   * of the API response for discovery endpoints.
   */
  public static class DiscoverEnvelope {
    public final List<Project> projects;
    private DiscoverEnvelope(List<Project> projects) {
      this.projects = projects;
    }
  }
}
