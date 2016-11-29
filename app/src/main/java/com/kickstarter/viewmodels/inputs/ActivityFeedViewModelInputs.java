package com.kickstarter.viewmodels.inputs;

import com.kickstarter.ui.adapters.ActivityFeedAdapter;

public interface ActivityFeedViewModelInputs extends ActivityFeedAdapter.Delegate {
  /**
   * Invoke when pagination should happen.
   */
  void nextPage();

  /**
   * Invoke when the feed should be refreshed.
   */
  void refresh();
}
