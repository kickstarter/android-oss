package com.kickstarter.libs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class NoopInternalTools implements InternalToolsType {
  @Override
  public void maybeStartInternalToolsActivity(final @NonNull BaseActivity baseActivity) {}

  @Override
  public @Nullable String basicAuthorizationHeader() {
    return null;
  }
}
