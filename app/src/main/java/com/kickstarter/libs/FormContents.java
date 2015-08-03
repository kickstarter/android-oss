package com.kickstarter.libs;

public class FormContents {
  public final String serialized;
  public final String method;
  public final String encodingType;

  public FormContents(final String serialized, final String method, final String encodingType) {
    this.serialized = serialized;
    this.method = method;
    this.encodingType = encodingType;
  }
}

