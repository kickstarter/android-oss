package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.models.Project;
import com.kickstarter.ui.adapters.data.CommentFeedData;

import rx.Observable;

public interface CommentFeedViewModelOutputs {
  /**
   * Emits data to display comment feed.
   */
  Observable<CommentFeedData> commentFeedData();

  /**
   * Emits when the comment dialog should be dismissed.
   */
  Observable<Void> dismissCommentDialog();

  /**
   * Emits a boolean indicating when the post button should be enabled.
   */
  Observable<Boolean> enablePostButton();

  /**
   * Emits a boolean indicating whether comments are being fetched from the API.
   */
  Observable<Boolean> isFetchingComments();

  /**
   * Emits the string that should be displayed in the comment dialog when it is shown.
   */
  Observable<String> currentCommentBody();

  /**
   * Emits a boolean that determines if the comment button should be visible.
   */
  Observable<Boolean> showCommentButton();

  /**
   * Emits a project and boolean to determine when the comment dialog should be shown.
   */
  Observable<Pair<Project, Boolean>> showCommentDialog();

  /**
   * Emits when comment posted toast message should be displayed.
   */
  Observable<Void> showCommentPostedToast();
}
