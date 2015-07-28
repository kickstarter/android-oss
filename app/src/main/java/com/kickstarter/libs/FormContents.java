package com.kickstarter.libs;

public class FormContents {
  public final String serialized;
  public final String method;
  public final String encodingType;
  public final String authenticityToken;

  public FormContents(final String serialized, final String method, final String encodingType, final String authenticityToken) {
    this.serialized = serialized;
    this.method = method;
    this.encodingType = encodingType;
    this.authenticityToken = authenticityToken;
  }
}

