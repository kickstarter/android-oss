package com.kickstarter.presenters.outputs;

import rx.Observable;

public interface CommentFeedPresenterOutputs {
  /**
   * Emits a boolean that determines if the comment button should be visible.
   */
  Observable<Boolean> showCommentButton();

  /**
   * Emits when a comment has been successfully posted.
   */
  Observable<Void> commentPosted();

  /**
   * Emits a boolean indicating whether comments are being fetched from the API.
   */
  Observable<Boolean> isFetchingComments();

  /**
   * Emits when the comment dialog should be displayed.
   */
  Observable<Void> showCommentDialog();
}
