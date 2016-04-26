package com.kickstarter.libs;

import com.trello.rxlifecycle.FragmentEvent;

import rx.Observable;

/**
 * A type implements this interface when it can describe its lifecycle in terms of attaching, view creation, starting,
 * stopping, destroying, and detaching.
 */
public interface FragmentLifecycleType {

  /**
   * An observable that describes the lifecycle of the object, from ATTACH to DETACH.
   */
  Observable<FragmentEvent> lifecycle();
}
