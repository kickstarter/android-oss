package com.kickstarter.libs.rx.transformers;

import androidx.annotation.NonNull;

import com.kickstarter.libs.utils.extensions.AnyExtKt;

import rx.Observable;

public final class CoalesceTransformer<T> implements Observable.Transformer<T, T> {
  private final T theDefault;

  public CoalesceTransformer(final @NonNull T theDefault) {
    this.theDefault = theDefault;
  }

  @Override
  public @NonNull Observable<T> call(final @NonNull Observable<T> source) {
    return source
      .map(AnyExtKt.coalesceWith(this.theDefault));
  }
}
