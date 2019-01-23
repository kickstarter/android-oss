package com.kickstarter.libs.rx.transformers;

import androidx.annotation.NonNull;

import rx.Observable;

public final class IgnoreValuesTransformer<S> implements Observable.Transformer<S, Void> {
  @Override
  @NonNull public Observable<Void> call(final @NonNull Observable<S> source) {
    return source.map(__ -> null);
  }
}
