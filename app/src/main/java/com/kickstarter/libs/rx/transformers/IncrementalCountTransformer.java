package com.kickstarter.libs.rx.transformers;

import rx.Observable;

public final class IncrementalCountTransformer <T> implements Observable.Transformer<T, Integer> {
  final int firstPage;

  public IncrementalCountTransformer() {
    firstPage = 1;
  }

  public IncrementalCountTransformer(final int firstPage) {
    this.firstPage = firstPage;
  }

  @Override
  public Observable<Integer> call(Observable<T> source) {
    return source.scan(firstPage-1, (accum, __) -> accum + 1).skip(1);
  }
}
