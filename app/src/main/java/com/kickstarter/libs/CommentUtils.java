package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.models.Comment;
import com.kickstarter.models.User;

public class CommentUtils {
  private CommentUtils() {
    throw new AssertionError();
  }

  public static boolean isUserAuthor(@NonNull final Comment comment, @Nullable final User user) {
    return user != null && comment.author().id().equals(user.id());
  }
}
