package com.kickstarter.libs;

import android.support.annotation.NonNull;

public final class NoopInternalTools implements InternalToolsType {
  @Override
  public void maybeStartInternalToolsActivity(final @NonNull BaseActivity baseActivity) {}
}
