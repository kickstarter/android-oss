package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.ui.adapters.data.CommentFeedData;

import java.util.List;

public final class CommentUtils {
  private CommentUtils() {
    throw new AssertionError();
  }

  /**
   * Converts all the disparate data representing the comment feed into a `CommentFeedData` object that can be used to
   * populate a view.
   */
  public static @NonNull CommentFeedData deriveCommentFeedData(final @NonNull Project project,
    final @Nullable List<Comment> comments, final @Nullable User user) {

    return CommentFeedData.builder()
      .project(project)
      .comments(comments)
      .user(user)
      .build();
  }

  public static boolean isUserAuthor(final @NonNull Comment comment, final @Nullable User user) {
    return user != null && comment.author().id() == user.id();
  }
}
