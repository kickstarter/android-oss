package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;

import rx.Observable;

public final class TakeWhenTransformer <S, T> implements Observable.Transformer <S, S> {
  @NonNull private final Observable<T> when;

  public TakeWhenTransformer(final @NonNull Observable<T> when) {
    this.when = when;
  }

  @Override
  @NonNull
  public Observable<S> call(final @NonNull Observable<S> source) {
    return when.withLatestFrom(source, (__, x) -> x);
  }
}
