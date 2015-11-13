package com.kickstarter.presenters.outputs;

import com.kickstarter.models.Activity;

import java.util.List;

import rx.Observable;

public interface ActivityFeedPresenterOutputs {
  // Fetching from the API?
  Observable<Boolean> isFetchingActivities();
  // New page of activities
  Observable<List<Activity>> activities();
}
