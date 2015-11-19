package com.kickstarter.presenters.outputs;

import com.kickstarter.models.Activity;
import com.kickstarter.models.User;

import java.util.List;

import rx.Observable;

public interface ActivityFeedPresenterOutputs {
  /**
   * Emits a list of activities representing the user's activity feed.
   */
  Observable<List<Activity>> activities();

  /**
   * Emits when there are no activities to display.
   */
  Observable<User> emptyFeed();

  /**
   * Emits a boolean indicating whether activities are being fetched from the API.
   */
  Observable<Boolean> isFetchingActivities();
}
