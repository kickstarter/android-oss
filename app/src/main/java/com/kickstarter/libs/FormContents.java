package com.kickstarter.libs;

import android.support.annotation.NonNull;

public class FormContents {
  public final String serialized;
  public final String method;
  public final String encodingType;

  public FormContents(@NonNull final String serialized, @NonNull final String method,
    @NonNull final String encodingType) {
    this.serialized = serialized;
    this.method = method;
    this.encodingType = encodingType;
  }
}

