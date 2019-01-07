package com.kickstarter.libs;

import androidx.annotation.NonNull;

public class FormContents {
  private final String serialized;
  private final String method;
  private final String encodingType;

  public FormContents(final @NonNull String serialized, final @NonNull String method,
    final @NonNull String encodingType) {
    this.serialized = serialized;
    this.method = method;
    this.encodingType = encodingType;
  }
  public final @NonNull String encodingType() {
    return this.encodingType;
  }

  public final @NonNull String method() {
    return this.method;
  }

  public final @NonNull String serialized() {
    return this.serialized;
  }
}

