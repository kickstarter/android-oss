package com.kickstarter.viewmodels.inputs;

import android.support.annotation.NonNull;

import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.adapters.DiscoveryAdapter;
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter;

public interface DiscoveryViewModelInputs extends DiscoveryAdapter.Delegate, DiscoveryDrawerAdapter.Delegate {
  void nextPage();
  void initializer(final @NonNull DiscoveryParams params);
}
