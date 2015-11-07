package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Observable;
import rx.subjects.PublishSubject;

final class NeverErrorTransformer<T> implements Observable.Transformer<T, T> {
  @Nullable private final PublishSubject<Throwable> errors;

  protected NeverErrorTransformer() {
    this.errors = null;
  }

  protected NeverErrorTransformer(@Nullable final PublishSubject<Throwable> errors) {
    this.errors = errors;
  }

  @Override
  @NonNull public Observable<T> call(@NonNull final Observable<T> source) {
    return source
      .doOnError(e -> {
        if (errors != null) {
          errors.onNext(e);
        }
      })
      .onErrorResumeNext(__ -> Observable.empty());
  }
}
