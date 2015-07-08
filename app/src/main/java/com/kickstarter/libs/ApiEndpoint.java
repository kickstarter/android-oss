package com.kickstarter.libs;

public enum ApiEndpoint {
  PRODUCTION("Production", "https://***REMOVED***"),
  STAGING("Staging", "https://***REMOVED***"),
  LOCAL("Local", "http://api.ksr.10.0.3.2.xip.io"),
  CUSTOM("Custom", null);

  public String name;
  public String url;

  ApiEndpoint(final String name, final String url) {
    this.name = name;
    this.url = url;
  }

  @Override public String toString() {
    return name;
  }

  public static ApiEndpoint from(final String url) {
    for (ApiEndpoint value : values()) {
      if (value.url != null && value.url.equals(url)) {
        return value;
      }
    }
    ApiEndpoint endpoint = CUSTOM;
    endpoint.url = url;
    return endpoint;
  }
}
