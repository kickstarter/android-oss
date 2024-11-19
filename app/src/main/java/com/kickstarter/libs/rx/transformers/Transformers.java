package com.kickstarter.libs.rx.transformers;

import androidx.annotation.NonNull;

import com.kickstarter.services.ApiException;
import com.kickstarter.services.apiresponses.ErrorEnvelope;

public final class Transformers {
  private Transformers() {}


  /**
   * Emits when an error is thrown in a materialized stream.
   * Adapted to RxJava 2
   */
  public static @NonNull <T> ErrorsTransformerV2<T> errorsV2() {
    return new ErrorsTransformerV2<>();
  }

  /**
   * Prevents an observable from erroring by chaining `onErrorResumeNext`.
   * Adapted to RxJava 2
   */
  public static <T> NeverErrorTransformerV2<T> neverErrorV2() {
    return new NeverErrorTransformerV2<>();
  }

  /**
   * Prevents an observable from erroring on any {@link ApiException} exceptions,
   * and any errors that do occur will be piped into the supplied
   * errors publish subject. `null` values will never be sent to
   * the publish subject.
   *
   * Adapted to RxJava 2
   */
  public static <T> NeverApiErrorTransformerV2<T> pipeApiErrorsToV2(final @NonNull io.reactivex.subjects.PublishSubject<ErrorEnvelope> errorSubject) {
    return new NeverApiErrorTransformerV2<>(errorSubject::onNext);
  }

  /**
   * Emits the latest value of the source observable whenever the `when`
   * observable emits.
   *
   * Adapted to RxJava 2
   */
  public static <S, T> TakeWhenTransformerV2<S, T> takeWhenV2(final @NonNull io.reactivex.Observable<T> when) {
    return new TakeWhenTransformerV2<>(when);
  }

  /**
   * Emits the latest value of the source `when` observable whenever the
   * `when` observable emits.
   */
  public static <S, T> TakePairWhenTransformerV2<S, T> takePairWhenV2(final @NonNull io.reactivex.Observable<T> when) {
    return new TakePairWhenTransformerV2<>(when);
  }
  /**
   * Zips two observables up into an observable of pairs.
   */
  public static <S, T> ZipPairTransformerV2<S, T> zipPairV2(final @NonNull io.reactivex.Observable<T> second) {
    return new ZipPairTransformerV2<>(second);
  }

  /**
   * Emits the latest values from two observables whenever either emits.
   */
  public static <S, T> CombineLatestPairTransformerV2<S, T> combineLatestPair(final @NonNull io.reactivex.Observable<T> second) {
    return new CombineLatestPairTransformerV2<>(second);
  }

  public static @NonNull <S> IgnoreValuesTransformerV2<S> ignoreValuesV2() {
    return new IgnoreValuesTransformerV2<>();
  }

  /**
   * Emits the number of times the source has emitted for every emission of the source. The
   * first emitted value will be `1`.
   */
  public static @NonNull <T> IncrementalCountTransformerV2<T> incrementalCountV2() {
    return new IncrementalCountTransformerV2<>();
  }


  /**
   * Emits an observable of values from a materialized stream.
   * Adapted to RxJava 2
   */
  public static @NonNull <T> ValuesTransformerV2<T> valuesV2() {
    return new ValuesTransformerV2<>();
  }

  public static @NonNull <T> ObserveForUITransformerV2<T> observeForUIV2() {
    return new ObserveForUITransformerV2<>();
  }
}
