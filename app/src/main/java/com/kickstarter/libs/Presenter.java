package com.kickstarter.libs;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;

public class Presenter<ViewType> {
  private ViewType view;
  protected final PublishSubject<ViewType> viewSubject = PublishSubject.create();
  protected final List<Subscription> subscriptions = new ArrayList<>();

  protected void onCreate(Bundle savedInstanceState) {
    onTakeView(null);
  }

  protected void onResume(ViewType view) {
    onTakeView(view);
  }

  protected void onPause() {
    onTakeView(null);
  }

  protected void onDestroy() {
    for (Subscription subscription : subscriptions) {
      subscription.unsubscribe();
    }

    viewSubject.onCompleted();
  }

  protected void onTakeView(ViewType view) {
    this.view = view;
    viewSubject.onNext(view);
  }

  protected ViewType view() {
    return this.view;
  }

  public void save(Bundle state) {
    // TODO
  }
}
