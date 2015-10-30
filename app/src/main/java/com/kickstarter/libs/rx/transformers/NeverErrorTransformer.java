package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;

import rx.Observable;

/*
  Prevents an observable from erroring by chaining `onErrorResumeNext`.
 */
public class NeverErrorTransformer<T> implements Observable.Transformer<T, T> {

  @Override
  @NonNull public Observable<T> call(@NonNull final Observable<T> source) {
    return source.onErrorResumeNext(__ -> Observable.empty());
  }
}
