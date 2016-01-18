package com.kickstarter.libs;

import android.support.annotation.NonNull;

public final class NoopInternalTools implements InternalToolsType {
  @Override
  public void maybeStartDebugActivity(final @NonNull BaseActivity baseActivity) {}
}
