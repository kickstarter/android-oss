package com.kickstarter.libs;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.subjects.PublishSubject;

public class Presenter<ViewType> {
  protected final PublishSubject<ViewType> viewSubject = PublishSubject.create();
  protected final List<Subscription> subscriptions = new ArrayList<>();

  protected void onCreate(Bundle savedInstanceState) {
    viewSubject.onNext(null);
  }

  protected void onResume(ViewType view) {
    viewSubject.onNext(view);
  }

  protected void onPause() {
    viewSubject.onNext(null);
  }

  protected void onDestroy() {
    for (Subscription subscription : subscriptions) {
      subscription.unsubscribe();
    }

    viewSubject.onCompleted();
  }

  public void save(Bundle state) {
    // TODO
  }
}
