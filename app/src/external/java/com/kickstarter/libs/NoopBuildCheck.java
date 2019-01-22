package com.kickstarter.libs;

import androidx.annotation.NonNull;

import com.kickstarter.services.WebClientType;
import com.kickstarter.viewmodels.DiscoveryViewModel;

public class NoopBuildCheck implements BuildCheck {
  @Override
  public void bind(final @NonNull DiscoveryViewModel.ViewModel viewModel, final @NonNull WebClientType client) {
    // No-op, distribution through Play Store
  }
}
