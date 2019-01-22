package com.kickstarter.libs.rx.transformers;

import androidx.annotation.NonNull;

import rx.Notification;
import rx.Observable;

public final class CompletedTransformer<T> implements Observable.Transformer<Notification<T>, Void> {

  @Override
  public @NonNull Observable<Void> call(final @NonNull Observable<Notification<T>> source) {
    return source
      .filter(Notification::isOnCompleted)
      .map(__ -> null);
  }
}

