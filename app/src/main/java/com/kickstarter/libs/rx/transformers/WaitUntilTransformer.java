package com.kickstarter.libs.rx.transformers;

import androidx.annotation.NonNull;

import rx.Observable;

public final class WaitUntilTransformer<T, R> implements Observable.Transformer<T, T> {
  @NonNull private final Observable<R> until;

  public WaitUntilTransformer(final @NonNull Observable<R> until) {
    this.until = until;
  }

  @Override
  public Observable<T> call(final @NonNull Observable<T> source) {
    return this.until.take(1).flatMap(__ -> source);
  }
}
