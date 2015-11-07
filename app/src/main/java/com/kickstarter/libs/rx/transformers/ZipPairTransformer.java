package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;
import android.util.Pair;

import rx.Observable;

public class ZipPairTransformer<T, R> implements Observable.Transformer<T, Pair<T, R>> {
  @NonNull final private Observable<R> second;

  public ZipPairTransformer(@NonNull final Observable<R> second) {
    this.second = second;
  }

  @Override
  @NonNull public Observable<Pair<T, R>> call(Observable<T> first) {
    return Observable.zip(first, second, Pair::new);
  }
}
