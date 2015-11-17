package com.kickstarter.presenters.inputs;

public interface ActivityFeedPresenterInputs {
  /**
   * Invoke when pagination should happen.
   */
  void nextPage();

  /**
   * Invoke when the feed should be refreshed.
   */
  void refresh();
}
