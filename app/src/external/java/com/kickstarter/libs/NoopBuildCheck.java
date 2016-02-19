package com.kickstarter.libs;

import com.kickstarter.viewmodels.DiscoveryViewModel;
import com.kickstarter.services.WebClient;

public class NoopBuildCheck implements BuildCheck {
  @Override
  public void bind(final DiscoveryViewModel viewModel, final WebClientType client) {
    // No-op, distribution through Play Store
  }
}
