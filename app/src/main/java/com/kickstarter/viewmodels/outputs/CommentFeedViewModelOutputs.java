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
  Observable<Void> commentPosted();

  /**
   * Emits a boolean indicating when the post button should be enabled.
   */
  Observable<Boolean> enablePostButton();

  /**
   * Emits a boolean indicating whether comments are being fetched from the API.
   */
  Observable<Boolean> isFetchingComments();

  Observable<String> initialCommentBody();

  /**
   * Emits a boolean that determines if the comment button should be visible.
   */
  Observable<Boolean> showCommentButton();

  /**
   * Emits the project when the comment dialog should be displayed.
   */
  Observable<Pair<Project, Boolean>> showCommentDialog();
}
