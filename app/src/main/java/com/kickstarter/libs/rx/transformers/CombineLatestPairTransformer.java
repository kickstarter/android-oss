package com.kickstarter.libs.rx.transformers;

import android.util.Pair;

import androidx.annotation.NonNull;
import rx.Observable;

public final class CombineLatestPairTransformer<S, T> implements Observable.Transformer<S, Pair<S, T>> {
  @NonNull private final Observable<T> second;

  public CombineLatestPairTransformer(final @NonNull Observable<T> second) {
    this.second = second;
  }

  @Override
  @NonNull public Observable<Pair<S, T>> call(final @NonNull Observable<S> first) {
    return Observable.combineLatest(first, this.second, Pair::new);
  }
}
