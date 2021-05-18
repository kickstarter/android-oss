package com.kickstarter.mock.factories;

import com.kickstarter.services.apiresponses.DeprecatedCommentsEnvelope;

import java.util.Collections;

import androidx.annotation.NonNull;

public final class DeprecatedCommentsEnvelopeFactory {
  private DeprecatedCommentsEnvelopeFactory() {}

  public static @NonNull
    DeprecatedCommentsEnvelope commentsEnvelope() {
    return DeprecatedCommentsEnvelope.builder()
    .urls(DeprecatedCommentsEnvelope.UrlsEnvelope.builder()
      .api(DeprecatedCommentsEnvelope.UrlsEnvelope.ApiEnvelope.builder()
        .moreComments("http://kck.str/comments/more")
        .newerComments("http://kck.str/comments/newer")
        .build())
      .build())
    .comments(Collections.singletonList(DeprecatedCommentFactory.comment()))
    .build();
  }
}
