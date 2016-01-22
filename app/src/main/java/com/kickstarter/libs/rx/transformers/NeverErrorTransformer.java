package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Observable;
import rx.functions.Action1;

public final class NeverErrorTransformer<T> implements Observable.Transformer<T, T> {
  private final @Nullable Action1<Throwable> errorAction;

  protected NeverErrorTransformer() {
    this.errorAction = null;
  }

  protected NeverErrorTransformer(final @Nullable Action1<Throwable> errorAction) {
    this.errorAction = errorAction;
  }

  @Override
  @NonNull public Observable<T> call(final @NonNull Observable<T> source) {
    return source
      .doOnError(e -> {
        if (errorAction != null) {
          errorAction.call(e);
        }
      })
      .onErrorResumeNext(Observable.empty());
  }
}
