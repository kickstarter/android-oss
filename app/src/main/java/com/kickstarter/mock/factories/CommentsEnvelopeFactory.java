package com.kickstarter.mock.factories;

import com.kickstarter.services.apiresponses.CommentsEnvelope;

import java.util.Collections;

import androidx.annotation.NonNull;

public final class CommentsEnvelopeFactory {
  private CommentsEnvelopeFactory() {}

  public static @NonNull CommentsEnvelope commentsEnvelope() {
    return CommentsEnvelope.builder()
      .urls(CommentsEnvelope.UrlsEnvelope.builder()
        .api(CommentsEnvelope.UrlsEnvelope.ApiEnvelope.builder()
          .moreComments("http://kck.str/comments/more")
          .newerComments("http://kck.str/comments/newer")
          .build())
        .build())
      .comments(Collections.singletonList(CommentFactory.comment()))
      .build();
  }
}
