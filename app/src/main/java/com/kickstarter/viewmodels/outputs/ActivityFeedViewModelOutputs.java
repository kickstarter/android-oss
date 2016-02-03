package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Activity;

import java.util.List;

import rx.Observable;

public interface ActivityFeedViewModelOutputs {
  /**
   * Emits a list of activities representing the user's activity feed.
   */
  Observable<List<Activity>> activities();

  /**
   * Emits a boolean that determines if a logged-out, empty state should be displayed.
   */
  Observable<Boolean> loggedOutEmptyStateIsVisible();

  /**
   * Emits a logged-in user with zero activities in order to display an empty state.
   */
  Observable<Boolean> loggedInEmptyStateIsVisible();

  /**
   * Emits a boolean indicating whether activities are being fetched from the API.
   */
  Observable<Boolean> isFetchingActivities();
}
