package com.kickstarter.factories;

import com.kickstarter.models.Comment;

import org.joda.time.DateTime;

public final class CommentFactory {
  private CommentFactory() {}

  public static Comment comment() {
    return Comment.builder()
      .author(UserFactory.user())
      .body("Some comment")
      .createdAt(DateTime.now())
      .id(20160321)
      .build();
  }
}
