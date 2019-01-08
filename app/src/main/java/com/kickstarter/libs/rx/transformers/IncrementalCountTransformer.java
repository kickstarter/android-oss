package com.kickstarter.libs.rx.transformers;

import androidx.annotation.NonNull;

import rx.Observable;

public final class IncrementalCountTransformer<T> implements Observable.Transformer<T, Integer> {
  final int firstPage;

  public IncrementalCountTransformer() {
    this.firstPage = 1;
  }

  public IncrementalCountTransformer(final int firstPage) {
    this.firstPage = firstPage;
  }

  @Override
  public Observable<Integer> call(final @NonNull Observable<T> source) {
    return source.scan(this.firstPage-1, (accum, __) -> accum + 1).skip(1);
  }
}
