package com.kickstarter.mock.factories;

import com.kickstarter.models.DeprecatedComment;

import org.joda.time.DateTime;

public final class DeprecatedCommentFactory {
  private DeprecatedCommentFactory() {}

  public static DeprecatedComment comment() {
    return DeprecatedComment.builder()
      .author(UserFactory.user())
      .body("Some comment")
      .createdAt(DateTime.now())
      .deletedAt(DateTime.parse("1970-01-01T00:00:00Z"))
      .id(IdFactory.id())
      .build();
  }
}
