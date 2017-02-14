package com.kickstarter.libs;

public final class KoalaContext {
  private KoalaContext() {}

  /**
   * Determines the place from which the comments dialog was presented.
   *
   * PROJECT_ACTIVITY: The creator's project activity screen.
   * PROJECT_COMMENTS: The comments screen for a project.
   * UPDATE_COMMENTS:  The comments screen for an update.
   */
  public static class CommentDialog {
    public static final String PROJECT_ACTIVITY = "project_activity";
    public static final String PROJECT_COMMENTS = "project_comments";
    public static final String UPDATE_COMMENTS = "update_comments";
  }

  /**
   * Determines the place from which the comments were presented.
   *
   * PROJECT: The comments for a project.
   * UPDATE:  The comments for an update.
   */
  public static class Comments {
    public static final String PROJECT = "project";
    public static final String UPDATE = "update";
  }

  /**
   * Determines the place from which the Update was presented.
   *
   * UPDATES:           The updates index.
   * ACTIVITY:          The activity feed.
   * ACTIVITY_SAMPLE:   The activity sample.
   */
  public static class Update {
    public static final String UPDATES = "updates";
    public static final String ACTIVITY = "activity";
    public static final String ACTIVITY_SAMPLE = "activity_sample";
  }
}
