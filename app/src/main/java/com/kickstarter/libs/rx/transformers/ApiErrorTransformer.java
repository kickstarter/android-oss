package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.services.apiresponses.ErrorEnvelope;

import rx.Observable;
import rx.subjects.PublishSubject;

/*
  Prevents an observable from erroring by chaining `onErrorResumeNext`,
  and any errors that occur of type ApiError will be piped into the
  supplied errors publish subject. `null` values will never be
  sent to the publish subject.
 */
public class ApiErrorTransformer<T> implements Observable.Transformer<T, T> {
  @Nullable final private PublishSubject<ErrorEnvelope> errors;

  public ApiErrorTransformer(@Nullable final PublishSubject<ErrorEnvelope> errors) {
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
