package com.kickstarter.libs;

import android.support.annotation.NonNull;

public class FormContents {
  public final String serialized;
  public final String method;
  public final String encodingType;

  public FormContents(final @NonNull String serialized, final @NonNull String method,
    final @NonNull String encodingType) {
    this.serialized = serialized;
    this.method = method;
    this.encodingType = encodingType;
  }
}

