package com.kickstarter.libs.rx.transformers;

import androidx.annotation.NonNull;

import rx.Notification;
import rx.Observable;

public final class ErrorsTransformer<T> implements Observable.Transformer<Notification<T>, Throwable> {

  @Override
  public @NonNull Observable<Throwable> call(final @NonNull Observable<Notification<T>> source) {
    return source
      .filter(Notification::hasThrowable)
      .map(Notification::getThrowable);
  }
}

