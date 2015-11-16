package com.kickstarter.presenters.outputs;

import com.kickstarter.models.Activity;

import java.util.List;

import rx.Observable;

public interface ActivityFeedPresenterOutputs {
  // Fetching from the API?
  Observable<Boolean> isFetchingActivities();
  Observable<List<Activity>> activities();
}
