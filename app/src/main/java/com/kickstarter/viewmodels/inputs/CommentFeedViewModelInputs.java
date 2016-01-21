package com.kickstarter.viewmodels.inputs;

public interface CommentFeedViewModelInputs {
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
