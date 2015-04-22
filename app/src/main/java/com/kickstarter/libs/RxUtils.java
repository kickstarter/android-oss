package com.kickstarter.libs;

import android.util.Pair;

import rx.Observable;

public class RxUtils {
  private RxUtils() {
    throw new AssertionError();
  }

  public static <F, S> Observable<Pair<F, S>> combineLatestPair(final Observable<F> ob1, final Observable<S> ob2) {
    return Observable.combineLatest(ob1, ob2, Pair::new);
  }
}
