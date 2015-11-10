package com.kickstarter.libs;

import com.kickstarter.presenters.DiscoveryPresenter;
import com.kickstarter.services.WebClient;

public class NoopBuildCheck implements BuildCheck {
  @Override
  public void bind(final DiscoveryPresenter presenter, final WebClient client) {
    // No-op, distribution through Play Store
  }
}
