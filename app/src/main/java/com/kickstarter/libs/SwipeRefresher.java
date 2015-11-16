package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.kickstarter.R;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func0;

public final class SwipeRefresher {
  public SwipeRefresher(@NonNull final BaseActivity<? extends Presenter> activity,
    @NonNull final SwipeRefreshLayout layout,
    @NonNull final Action0 refreshAction,
    @NonNull final Func0<Observable<Boolean>> isRefreshing) {
    layout.setColorSchemeResources(R.color.green, R.color.green_darken_10, R.color.green_darken_20, R.color.green_darken_10);

    RxSwipeRefreshLayout.refreshes(layout)
      .compose(activity.bindToLifecycle())
      .subscribe(__ -> refreshAction.call());

    isRefreshing.call()
      .filter(fetching -> !fetching)
      .compose(activity.bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(layout::setRefreshing);
  }
}
