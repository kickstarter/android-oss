package com.kickstarter.libs.rx.transformers;

import android.util.Pair;

import androidx.annotation.NonNull;
import rx.Observable;

public final class ZipPairTransformer<T, R> implements Observable.Transformer<T, Pair<T, R>> {
  @NonNull private final Observable<R> second;

  public ZipPairTransformer(final @NonNull Observable<R> second) {
    this.second = second;
  }

  @Override
  @NonNull public Observable<Pair<T, R>> call(final @NonNull Observable<T> first) {
    return Observable.zip(first, this.second, Pair::new);
  }
}
