package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface InternalToolsType {
  void maybeStartInternalToolsActivity(final @NonNull BaseActivity baseActivity);
  @Nullable String basicAuthorizationHeader();
}
