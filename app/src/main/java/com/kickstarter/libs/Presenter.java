package com.kickstarter.libs;

import android.os.Bundle;

import rx.subjects.PublishSubject;

public class Presenter<ViewType> {
  protected final PublishSubject<ViewType> viewSubject = PublishSubject.create();

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
    viewSubject.onCompleted();
  }

  public void save(Bundle state) {
    // TODO
  }

}
