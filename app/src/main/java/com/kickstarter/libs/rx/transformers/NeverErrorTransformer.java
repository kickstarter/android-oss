package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.services.apiresponses.ErrorEnvelope;

import rx.Observable;
import rx.subjects.PublishSubject;

final class NeverErrorTransformer<T> implements Observable.Transformer<T, T> {
  @Nullable final private PublishSubject<ErrorEnvelope> errors;

  protected NeverErrorTransformer() {
    this.errors = null;
  }

  protected NeverErrorTransformer(@Nullable final PublishSubject<ErrorEnvelope> errors) {
    this.errors = errors;
  }

  @Override
  @NonNull public Observable<T> call(@NonNull final Observable<T> source) {
    return source
      .doOnError(e -> {
        final ErrorEnvelope env = ErrorEnvelope.fromThrowable(e);
        if (env != null && errors != null) {
          errors.onNext(env);
        }
      })
      .onErrorResumeNext(__ -> Observable.empty());
  }
}
