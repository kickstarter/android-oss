package com.kickstarter.libs;

import android.util.Pair;

import rx.Observable;

public class RxUtils {
  private RxUtils() {
    throw new AssertionError();
  }

  // Returns an observable that pings with the latest value of ob1 and ob2 whenever either pings.
  public static <F, S> Observable<Pair<F, S>> combineLatestPair(final Observable<F> ob1, final Observable<S> ob2) {
    return Observable.combineLatest(ob1, ob2, Pair::new);
  }

  // Returns an observable that pings with the latest value of ob1 and ob2 whenever ob2 pings.
  public static <F, S> Observable<Pair<F, S>> takePairWhen(final Observable<F> ob1, final Observable<S> ob2) {
    return ob2.withLatestFrom(ob1, (x, y) -> new Pair<>(y, x));
  }

  // Returns an observable that pings with the latest value of ob1 whenever ob2 pings.
  public static <F, S> Observable<F> takeWhen(final Observable<F> ob1, final Observable<S> ob2) {
    return ob2.withLatestFrom(ob1, (x, y) -> y);
  }
}
