package com.kickstarter.libs;

import android.support.annotation.NonNull;

import java.util.Map;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class MockTrackingClient extends TrackingClientType {

  public static class Event {
    private final String name;
    private final Map<String, Object> properties;
    public Event(final String name, final Map<String, Object> properties) {
      this.name = name;
      this.properties = properties;
    }
  }

  public final @NonNull PublishSubject<Event> events = PublishSubject.create();
  public final @NonNull Observable<String> eventNames = this.events.map(e -> e.name);

  @Override
  public void track(final @NonNull String eventName, final @NonNull Map<String, Object> properties) {
    this.events.onNext(new Event(eventName, properties));
  }
}
