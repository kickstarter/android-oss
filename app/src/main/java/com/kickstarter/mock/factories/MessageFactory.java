package com.kickstarter.mock.factories;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;

import com.kickstarter.models.Message;

public final class MessageFactory {
  private MessageFactory() {}

  public static @NonNull
  Message message() {
    return Message.builder()
      .body("")
      .createdAt(DateTime.now())
      .id(123943059L)
      .recipient(UserFactory.creator())
      .sender(UserFactory.user())
      .build();
  }
}
