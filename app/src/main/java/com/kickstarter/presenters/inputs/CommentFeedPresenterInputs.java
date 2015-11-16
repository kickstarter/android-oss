package com.kickstarter.presenters.inputs;

import com.kickstarter.models.Project;

public interface CommentFeedPresenterInputs {
  /**
   * Invoke with the project that is unparceled from the intent.
   */
  void initialProject(Project __);

  /**
   * Invoke with the comment body every time it changes.
   */
  void commentBody(String __);

  /**
   * Invoke when pagination should happen.
   */
  void nextPage();
}
