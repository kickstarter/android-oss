package com.kickstarter.viewmodels.inputs;

public interface CommentFeedViewModelInputs {
  /**
   * Invoke with the comment body every time it changes.
   */
  void commentBodyInput(String __);

  /**
   * Call when the comment button is clicked.
   */
  void commentButtonClicked();

  /**
   * Call when the comment dialog should be dismissed.
   */
  void commentDialogDismissed();

  /**
   * Call when returning to activity with login success.
   */
  void loginSuccess();

  /**
   * Invoke when pagination should happen.
   */
  void nextPage();

  /**
   * Call when the post comment button is clicked.
   */
  void postCommentClicked();

  /**
   * Invoke when the feed should be refreshed.
   */
  void refresh();
}
