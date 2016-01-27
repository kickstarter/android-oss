package com.kickstarter.viewmodels.inputs;

import com.kickstarter.ui.adapters.DiscoveryAdapter;
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter;

public interface DiscoveryViewModelInputs extends DiscoveryAdapter.Delegate, DiscoveryDrawerAdapter.Delegate {
  void nextPage();

  /**
   * Call when you want to open or close the drawer.
   */
  void openDrawer(final boolean open);
}
