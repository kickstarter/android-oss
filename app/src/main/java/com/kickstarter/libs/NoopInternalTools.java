package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class NoopInternalTools implements InternalToolsType {
  @Override
  public void maybeStartInternalToolsActivity(final @NonNull BaseActivity baseActivity) {}

  @Override
  public @Nullable String basicAuthorizationHeader() {
    return null;
  }
}
