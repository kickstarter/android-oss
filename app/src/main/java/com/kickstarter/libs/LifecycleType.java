package com.kickstarter.libs;

import com.trello.rxlifecycle.ActivityEvent;

import rx.Observable;

/**
 * A type implements this interface when it can describe its lifecycle in terms of
 * creation, starting, stopping and destroying.
 */
public interface LifecycleType {

  /**
   * An observable that describes the lifecycle of the object, from CREATE to DESTROY.
   */
  Observable<ActivityEvent> lifecycle();
}
