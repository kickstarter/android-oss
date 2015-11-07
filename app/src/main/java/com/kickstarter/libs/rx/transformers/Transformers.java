package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;

import com.kickstarter.services.apiresponses.ErrorEnvelope;

import rx.Observable;
import rx.subjects.PublishSubject;

final public class Transformers {

  /**
   * Prevents an observable from erroring by chaining `onErrorResumeNext`.
   */
  public static <T> NeverErrorTransformer<T> neverError() {
    return new NeverErrorTransformer<>();
  }

  /**
   * Prevents an observable from erroring by chaining `onErrorResumeNext`,
   * and any errors that occur will be piped into the supplied errors publish
   * subject. `null` values will never be sent to the publish subject.
 */
  public static <T> NeverErrorTransformer<T> pipeErrorsTo(@NonNull final PublishSubject<Throwable> errors) {
    return new NeverErrorTransformer<>(errors);
  }

  /**
   * Prevents an observable from erroring on any `ApiError` exceptions.
   */
  public static <T> NeverApiErrorTransformer<T> neverApiError() {
    return new NeverApiErrorTransformer<>();
  }

  /**
   * Prevents an observable from erroring on any `ApiError` exceptions,
   * and any errors that do occur will be piped into the supplied
   * errors publish subject. `null` values will never be sent to
   * the publish subject.
   */
  public static <T> NeverApiErrorTransformer<T> pipeApiErrorsTo(@NonNull final PublishSubject<ErrorEnvelope> errors) {
    return new NeverApiErrorTransformer<>(errors);
  }

  /**
   * Emits the latest value of the source observable whenever the `when`
   * observable emits.
   */
  public static <S, T> TakeWhenTransformer<S, T> takeWhen(@NonNull final Observable<T> when) {
    return new TakeWhenTransformer<>(when);
  }

  /**
   * Emits the latest value of the source `when` observable whenever the
   * `when` observable emits.
   */
  public static <S, T> TakePairWhenTransformer<S, T> takePairWhen(@NonNull final Observable<T> when) {
    return new TakePairWhenTransformer<>(when);
  }

  /**
   * Zips two observables up into an observable of pairs.
   */
  public static <S, T> ZipPairTransformer<S, T> zipPair(@NonNull final Observable<T> second) {
    return new ZipPairTransformer<>(second);
  }

  /**
   * Emits the latest values from two observables whenever either emits.
   */
  public static <S, T> CombineLatestPairTransformer<S, T> combineLatestPair(@NonNull final Observable<T> second) {
    return new CombineLatestPairTransformer<>(second);
  }
}
