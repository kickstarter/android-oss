package com.kickstarter.viewmodels.inputs;

public interface ActivityFeedViewModelInputs {
  /**
   * Invoke when pagination should happen.
   */
  void nextPage();

  /**
   * Invoke when the feed should be refreshed.
   */
  void refresh();
}
