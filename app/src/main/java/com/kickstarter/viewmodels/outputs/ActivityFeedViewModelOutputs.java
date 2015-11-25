package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Activity;
import com.kickstarter.models.User;

import java.util.List;

import rx.Observable;

public interface ActivityFeedViewModelOutputs {
  /**
   * Emits a list of activities representing the user's activity feed.
   */
  Observable<List<Activity>> activities();

  /**
   * Emits when there is no logged-in user, and so an empty state should be shown.
   */
  Observable<User> loggedOutEmptyState();

  /**
   * Emits a boolean indicating whether activities are being fetched from the API.
   */
  Observable<Boolean> isFetchingActivities();
}
