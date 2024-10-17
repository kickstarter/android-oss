package com.kickstarter.libs;

import androidx.annotation.Nullable;

public final class NoopInternalTools implements InternalToolsType {
  @Override
  public @Nullable String basicAuthorizationHeader() {
    return null;
  }
}
