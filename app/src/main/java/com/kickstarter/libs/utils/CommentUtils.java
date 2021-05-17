package com.kickstarter.libs.utils;

import com.kickstarter.models.DeprecatedComment;
import com.kickstarter.models.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class CommentUtils {
  private CommentUtils() {
    throw new AssertionError();
  }

  public static boolean isUserAuthor(final @NonNull DeprecatedComment comment, final @Nullable User user) {
    return user != null && comment.author().id() == user.id();
  }

  public static boolean isDeleted(final @NonNull DeprecatedComment comment) {
    return !DateTimeUtils.isEpoch(comment.deletedAt());
  }
}
