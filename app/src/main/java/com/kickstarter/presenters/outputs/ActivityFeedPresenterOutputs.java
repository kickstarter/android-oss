package com.kickstarter.presenters.outputs;

import com.kickstarter.models.Activity;

import java.util.List;

import rx.Observable;

public interface ActivityFeedPresenterOutputs {
  /**
   * Emits a boolean indicating whether activities are being fetched from the API.
   */
  Observable<Boolean> isFetchingActivities();

  /**
   * Emits a list of activities representing the user's activity feed.
   */
  Observable<List<Activity>> activities();
}
