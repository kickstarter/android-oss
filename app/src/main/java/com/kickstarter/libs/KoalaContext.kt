package com.kickstarter.libs

class KoalaContext {

  /**
   * Determines the place from which the comments dialog was presented.
   *
   * PROJECT_ACTIVITY: The creator's project activity screen.
   * PROJECT_COMMENTS: The comments screen for a project.
   * UPDATE_COMMENTS:  The comments screen for an update.
   */
  enum class CommentDialog(val trackingString: String) {
    PROJECT_ACTIVITY("project_activity"),
    PROJECT_COMMENTS("project_comments"),
    UPDATE_COMMENTS("update_comments")
  }

  /**
   * Determines the place from which the comments were presented.
   *
   * PROJECT: The comments for a project.
   * UPDATE:  The comments for an update.
   */
  enum class Comments(val trackingString: String) {
    PROJECT("project"),
    UPDATE("update")
  }

  /**
   * Determines the place from which the Update was presented.
   *
   * UPDATES:           The updates index.
   * ACTIVITY:          The activity feed.
   * ACTIVITY_SAMPLE:   The activity sample.
   */
  enum class Update(val trackingString: String) {
    UPDATES("updates"),
    ACTIVITY("activity"),
    ACTIVITY_SAMPLE("activity_sample")
  }
}
