package com.kickstarter.viewmodels.inputs;

import com.kickstarter.models.Project;

public interface CommentFeedViewModelInputs {
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

  /**
   * Invoke when the feed should be refreshed.
   */
  void refresh();
}
