package com.kickstarter.mock.factories;

import com.kickstarter.models.MessageThread;

public final class MessageThreadFactory {
  private MessageThreadFactory() {}

  public static MessageThread messageThread() {
    return MessageThread.builder()
      .closed(false)
      .id(123455)
      .lastMessage(MessageFactory.message())
      .participant(UserFactory.user())
      .project(ProjectFactory.project())
      .unreadMessagesCount(0)
      .build();
  }
}
