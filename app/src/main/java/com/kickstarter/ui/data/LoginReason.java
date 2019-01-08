package com.kickstarter.ui.data;

import androidx.annotation.NonNull;

public enum LoginReason {
  DEFAULT,
  ACTIVITY_FEED,
  CHANGE_PASSWORD,
  COMMENT_FEED,
  BACK_PROJECT,
  MESSAGE_CREATOR,
  RESET_PASSWORD,
  STAR_PROJECT;

  public boolean isDefaultFlow() {
    return this == DEFAULT;
  }

  public boolean isContextualFlow() {
    return !isDefaultFlow();
  }

  /**
   * Tracking string for Koala.
   */
  public @NonNull String trackingString() {
    switch (this) {
      case BACK_PROJECT:
        return "pledge";
      case MESSAGE_CREATOR:
        return "new_message";
      case STAR_PROJECT:
        return "star";
      default:
        return "generic";
    }
  }
}
