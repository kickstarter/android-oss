package com.kickstarter.libs.rx.transformers;

import androidx.annotation.NonNull;

import rx.Observable;

import static com.kickstarter.libs.utils.ObjectUtils.coalesceWith;

public final class CoalesceTransformer<T> implements Observable.Transformer<T, T> {
  private final T theDefault;

  public CoalesceTransformer(final @NonNull T theDefault) {
    this.theDefault = theDefault;
  }

  @Override
  public @NonNull Observable<T> call(final @NonNull Observable<T> source) {
    return source
      .map(coalesceWith(this.theDefault));
  }
}
