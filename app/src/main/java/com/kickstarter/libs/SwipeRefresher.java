package com.kickstarter.libs;

import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.kickstarter.R;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func0;

public final class SwipeRefresher {
  /**
   *
   * @param activity Activity to bind lifecycle events for.
   * @param layout Layout to subscribe to for refresh events, send signals when no longer refreshing.
   * @param refreshAction Action to call when a refresh event is emitted, likely a viewModel input.
   * @param isRefreshing Observable that emits events when the refreshing status changes.
   */
  public SwipeRefresher(final @NonNull BaseActivity<? extends ActivityViewModel> activity,
    final @NonNull SwipeRefreshLayout layout,
    final @NonNull Action0 refreshAction,
    final @NonNull Func0<Observable<Boolean>> isRefreshing) {

    // Iterate through colors in loading spinner while waiting for refresh
    setColorSchemeResources(layout);

    // Emits when user has signaled to refresh layout
    RxSwipeRefreshLayout.refreshes(layout)
      .compose(activity.bindToLifecycle())
      .subscribe(__ -> refreshAction.call());

    // Emits when the refreshing status changes. Hides loading spinner when feed is no longer refreshing.
    isRefreshing.call()
      .filter(refreshing -> !refreshing)
      .compose(activity.bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(layout::setRefreshing);
  }

  /**
   *
   * @param fragment Fragment to bind lifecycle events for.
   * @param layout Layout to subscribe to for refresh events, send signals when no longer refreshing.
   * @param refreshAction Action to call when a refresh event is emitted, likely a viewModel input.
   * @param isRefreshing Observable that emits events when the refreshing status changes.
   */
  public SwipeRefresher(final @NonNull BaseFragment<? extends FragmentViewModel> fragment,
    final @NonNull SwipeRefreshLayout layout,
    final @NonNull Action0 refreshAction,
    final @NonNull Func0<Observable<Boolean>> isRefreshing) {
    setColorSchemeResources(layout);


    // Emits when user has signaled to refresh layout
    RxSwipeRefreshLayout.refreshes(layout)
      .compose(fragment.bindToLifecycle())
      .subscribe(__ -> refreshAction.call());

    // Emits when the refreshing status changes. Hides loading spinner when feed is no longer refreshing.
    isRefreshing.call()
      .filter(refreshing -> !refreshing)
      .compose(fragment.bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(layout::setRefreshing);
  }

  private void setColorSchemeResources(final @NonNull SwipeRefreshLayout layout) {
    // Iterate through colors in loading spinner while waiting for refresh
    layout.setColorSchemeResources(R.color.kds_create_700,R.color.kds_create_500, R.color.kds_create_300);
  }
}
