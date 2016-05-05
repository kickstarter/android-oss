package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.models.Comment;
import com.kickstarter.models.User;

public final class CommentUtils {
  private CommentUtils() {
    throw new AssertionError();
  }

  public static boolean isUserAuthor(final @NonNull Comment comment, final @Nullable User user) {
    return user != null && comment.author().id() == user.id();
  }

  public static boolean isDeleted(final @NonNull Comment comment) {
    return !DateTimeUtils.isEpoch(comment.deletedAt());
  }
}
