package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;
import android.util.Pair;

import rx.Observable;

final class CombineLatestPairTransformer <S, T> implements Observable.Transformer<S, Pair<S, T>> {
  @NonNull private final Observable<T> second;

  public CombineLatestPairTransformer(@NonNull final Observable<T> second) {
    this.second = second;
  }

  @Override
  @NonNull public Observable<Pair<S, T>> call(@NonNull final Observable<S> first) {
    return Observable.combineLatest(first, second, Pair::new);
  }
}
