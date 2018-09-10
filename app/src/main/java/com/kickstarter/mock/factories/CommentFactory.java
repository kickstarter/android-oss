package com.kickstarter.mock.factories;

import com.kickstarter.models.Comment;

import org.joda.time.DateTime;

public final class CommentFactory {
  private CommentFactory() {}

  public static Comment comment() {
    return Comment.builder()
      .author(UserFactory.user())
      .body("Some comment")
      .createdAt(DateTime.now())
      .deletedAt(DateTime.parse("1970-01-01T00:00:00Z"))
      .id(IdFactory.id())
      .build();
  }
}
