package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;

import com.kickstarter.services.apiresponses.ErrorEnvelope;

import rx.subjects.PublishSubject;

final public class Transformers {

  /*
   * Prevents an observable from erroring by chaining `onErrorResumeNext`.
   */
  public static <T> NeverErrorTransformer<T> neverError() {
    return new NeverErrorTransformer<>();
  }

  /*
   * Prevents an observable from erroring by chaining `onErrorResumeNext`,
   * and any errors that occur of type ApiError will be piped into the
   * supplied errors publish subject. `null` values will never be
   * sent to the publish subject.
 */
  public static <T> NeverErrorTransformer<T> pipeErrorsTo(@NonNull final PublishSubject<ErrorEnvelope> errors) {
    return new NeverErrorTransformer<>(errors);
  }
}
