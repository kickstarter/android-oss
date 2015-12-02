package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.services.apiresponses.ErrorEnvelope;

import rx.Observable;
import rx.subjects.PublishSubject;

final class NeverApiErrorTransformer<T> implements Observable.Transformer<T, T> {
  @Nullable private final PublishSubject<ErrorEnvelope> errors;

  protected NeverApiErrorTransformer() {
    this.errors = null;
  }

  protected NeverApiErrorTransformer(@Nullable final PublishSubject<ErrorEnvelope> errors) {
    this.errors = errors;
  }

  @Override
  public @NonNull Observable<T> call(@NonNull final Observable<T> source) {
    return source
      .doOnError(e -> {
        final ErrorEnvelope env = ErrorEnvelope.fromThrowable(e);
        if (env != null && errors != null) {
          errors.onNext(env);
        }
      })
      .onErrorResumeNext(e -> {
        if (ErrorEnvelope.fromThrowable(e) == null) {
          return Observable.error(e);
        } else {
          return Observable.empty();
        }
      });
  }
}
