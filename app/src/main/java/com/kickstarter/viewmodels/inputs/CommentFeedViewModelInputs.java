package com.kickstarter.viewmodels.inputs;

public interface CommentFeedViewModelInputs {
  /**
   * Invoke with the comment body every time it changes.
   */
  void commentBody(String __);

  /**
   * Call when the comment button is clicked.
   */
  void commentButtonClicked();

  /**
   * Call when the comment dialog should be restored on rotation.
   */
  void dismissCommentDialog();

  /**
   * Invoke when pagination should happen.
   */
  void nextPage();

  /**
   * Invoke when the feed should be refreshed.
   */
  void refresh();
}
