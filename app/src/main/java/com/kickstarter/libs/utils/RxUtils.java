package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;
import android.util.Pair;

import rx.Observable;

public class RxUtils {
  private RxUtils() {
    throw new AssertionError();
  }

  // Returns an observable that pings with the latest value of ob1 and ob2 whenever either pings.
  @Deprecated
  public static <F, S> Observable<Pair<F, S>> combineLatestPair(@NonNull final Observable<F> ob1,
    @NonNull final Observable<S> ob2) {
    return Observable.combineLatest(ob1, ob2, Pair::new);
  }

  // Returns an observable that pings with the latest value of ob1 and ob2 whenever ob2 pings.
  @Deprecated
  public static <F, S> Observable<Pair<F, S>> takePairWhen(@NonNull final Observable<F> ob1,
    @NonNull final Observable<S> ob2) {
    return ob2.withLatestFrom(ob1, (x, y) -> new Pair<>(y, x));
  }

  // Returns an observable that pings with the latest value of ob1 whenever ob2 pings.
  @Deprecated
  public static <F, S> Observable<F> takeWhen(@NonNull final Observable<F> ob1, @NonNull final Observable<S> ob2) {
    return ob2.withLatestFrom(ob1, (x, y) -> y);
  }
}
