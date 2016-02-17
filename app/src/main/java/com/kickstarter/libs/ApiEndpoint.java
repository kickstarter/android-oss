package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public enum ApiEndpoint {
  PRODUCTION("Production", "https://***REMOVED***"),
  STAGING("Staging", "https://***REMOVED***"),
  LOCAL("Local", "http://api.ksr.10.0.3.2.xip.io"),
  CUSTOM("Custom", null);

  private String name;
  private String url;

  ApiEndpoint(final @NonNull String name, final @Nullable String url) {
    this.name = name;
    this.url = url;
  }

  public @NonNull String url() {
    return url;
  }

  @Override public String toString() {
    return name;
  }

  public static ApiEndpoint from(final @NonNull String url) {
    for (final ApiEndpoint value : values()) {
      if (value.url != null && value.url.equals(url)) {
        return value;
      }
    }
    ApiEndpoint endpoint = CUSTOM;
    endpoint.url = url;
    return endpoint;
  }
}
