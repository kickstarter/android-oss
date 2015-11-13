package com.kickstarter.libs.rx.transformers;

import android.support.annotation.NonNull;

import rx.Observable;

public final class IgnoreValuesTransformer <S> implements Observable.Transformer<S, Void> {
  @Override
  @NonNull public Observable<Void> call(@NonNull final Observable<S> source) {
    return source.map(__ -> null);
  }
}
