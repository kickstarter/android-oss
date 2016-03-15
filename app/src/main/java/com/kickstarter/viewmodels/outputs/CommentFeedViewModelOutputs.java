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
   * Emits when a comment has been successfully posted.
   */
  Observable<Void> commentIsPosted();

  /**
   * Emits a boolean indicating when the post button should be enabled.
   */
  Observable<Boolean> postButtonIsEnabled();

  /**
   * Emits a boolean indicating whether comments are being fetched from the API.
   */
  Observable<Boolean> isFetchingComments();

  /**
   * Emits the string that should be displayed in the comment dialog when it is shown.
   */
  Observable<String> initialCommentBody();

  /**
   * Emits a boolean that determines if the comment button should be visible.
   */
  Observable<Boolean> showCommentButton();

  /**
   * Emits a project and boolean to determine when the comment dialog should be displayed.
   */
  Observable<Pair<Project, Boolean>> showCommentDialog();
}
