package com.kickstarter.libs;

import com.kickstarter.services.KickstarterClient;

public class NoopBuildCheck implements BuildCheck {
  @Override public void bind(final Presenter presenter, final KickstarterClient client) {
    // No-op, distribution through Play Store
  }
}
