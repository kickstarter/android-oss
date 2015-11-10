package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;

import rx.Observable;

public final class WaitUntilTransformer<T, R> implements Observable.Transformer<T, T> {
  @NonNull private final Observable<R> until;

  public WaitUntilTransformer(@NonNull final Observable<R> until) {
    this.until = until;
  }

  @Override
  public Observable<T> call(@NonNull final Observable<T> source) {
    return until.take(1).flatMap(__ -> source);
  }
}
