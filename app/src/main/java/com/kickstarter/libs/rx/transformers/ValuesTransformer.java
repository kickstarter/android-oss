package com.kickstarter.libs.rx.transformers;

import androidx.annotation.NonNull;

import rx.Notification;
import rx.Observable;

public final class ValuesTransformer<T> implements Observable.Transformer<Notification<T>, T> {

  @Override
  public @NonNull Observable<T> call(final @NonNull Observable<Notification<T>> source) {
    return source
      .filter(Notification::isOnNext)
      .map(Notification::getValue);
  }
}

