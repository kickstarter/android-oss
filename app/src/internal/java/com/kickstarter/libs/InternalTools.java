package com.kickstarter.libs;

import androidx.annotation.NonNull;

public final class InternalTools implements InternalToolsType {

  @Override
  public @NonNull String basicAuthorizationHeader() {
    // TODO: Make this more private?
    return "Basic ZnV6enk6d3V6enk=";
  }
}
