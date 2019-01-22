package com.kickstarter.libs.rx.transformers;

import com.kickstarter.libs.utils.ThreadUtils;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class ObserveForUITransformer<T> implements Observable.Transformer<T, T> {
  @Override
  public @NonNull Observable<T> call(final @NonNull Observable<T> source) {

    return source.flatMap(value -> {
      if (ThreadUtils.isMainThread()) {
        return Observable.just(value).observeOn(Schedulers.immediate());
      } else {
        return Observable.just(value).observeOn(AndroidSchedulers.mainThread());
      }
    });
  }
}
