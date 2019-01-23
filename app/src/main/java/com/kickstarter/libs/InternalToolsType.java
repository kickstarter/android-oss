package com.kickstarter.libs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface InternalToolsType {
  void maybeStartInternalToolsActivity(final @NonNull BaseActivity baseActivity);
  @Nullable String basicAuthorizationHeader();
}
