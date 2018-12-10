package com.kickstarter.mock.factories;

import android.support.annotation.NonNull;

import com.kickstarter.services.apiresponses.MessageThreadsEnvelope;

import java.util.Collections;

public final class MessageThreadsEnvelopeFactory {
  private MessageThreadsEnvelopeFactory() {}

  public static @NonNull MessageThreadsEnvelope messageThreadsEnvelope() {
    return MessageThreadsEnvelope.builder()
      .urls(MessageThreadsEnvelope.UrlsEnvelope.builder()
        .api(MessageThreadsEnvelope.UrlsEnvelope.ApiEnvelope.builder()
          .moreMessageThreads("http://kck.str/message_threads/more")
          .build())
        .build())
      .messageThreads(Collections.singletonList(MessageThreadFactory.messageThread()))
      .build();
  }
}
