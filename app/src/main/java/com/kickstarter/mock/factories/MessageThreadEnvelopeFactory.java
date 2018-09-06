package com.kickstarter.mock.factories;

import android.support.annotation.NonNull;

import com.kickstarter.services.apiresponses.MessageThreadEnvelope;

import java.util.Collections;

public final class MessageThreadEnvelopeFactory {
  private MessageThreadEnvelopeFactory() {}

  public static @NonNull MessageThreadEnvelope empty() {
    return messageThreadEnvelope()
      .toBuilder()
      .messages(null)
      .build();
  }

  public static @NonNull MessageThreadEnvelope messageThreadEnvelope() {
    return MessageThreadEnvelope.builder()
      .messages(Collections.singletonList(MessageFactory.message()))
      .messageThread(MessageThreadFactory.messageThread())
      .participants(Collections.singletonList(UserFactory.user()))
      .build();
  }
}
