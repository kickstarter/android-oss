package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;
import android.util.Pair;

import rx.Observable;

public class TakePairWhenTransformer <S, T> implements Observable.Transformer <S, Pair<S, T>> {
  @NonNull private final Observable<T> when;

  public TakePairWhenTransformer(@NonNull final Observable<T> when) {
    this.when = when;
  }

  @Override
  @NonNull
  public Observable<Pair<S, T>> call(@NonNull final Observable<S> source) {
    return when.withLatestFrom(source, (x, y) -> new Pair<>(y, x));
  }
}
