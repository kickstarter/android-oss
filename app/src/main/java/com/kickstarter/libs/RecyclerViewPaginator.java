package com.kickstarter.libs;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Pair;

import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;

import rx.Subscription;
import rx.functions.Action0;

public final class RecyclerViewPaginator {
  private final @NonNull RecyclerView recyclerView;
  private final @NonNull Action0 nextPage;
  private Subscription subscription;
  private static final int DIRECTION_DOWN = 1;

  public RecyclerViewPaginator(final @NonNull RecyclerView recyclerView, final @NonNull Action0 nextPage) {
    this.recyclerView = recyclerView;
    this.nextPage = nextPage;
    start();
  }

  /**
   * Begin listening to the recycler view scroll events to determine
   * when pagination should happen.
   */
  public void start() {
    stop();

    this.subscription = RxRecyclerView.scrollEvents(this.recyclerView)
      .filter(__ -> this.recyclerView.canScrollVertically(DIRECTION_DOWN))
      .map(__ -> this.recyclerView.getLayoutManager())
      .ofType(LinearLayoutManager.class)
      .map(this::displayedItemFromLinearLayout)
      .filter(item -> item.second != 0)
      .filter(this::visibleItemIsCloseToBottom)
      // NB: We think this operation is suffering from back pressure problems due to the volume of scroll events:
      // https://rink.hockeyapp.net/manage/apps/239008/crash_reasons/88318986
      // If it continues to happen we can also try `debounce`.
      .onBackpressureDrop()
      .distinctUntilChanged()
      .subscribe(__ -> this.nextPage.call());
  }

  /**
   * Stop listening to recycler view scroll events and discard the
   * associated resources. This should be done when the object that
   * created `this` is released.
   */
  public void stop() {
    if (this.subscription != null) {
      this.subscription.unsubscribe();
      this.subscription = null;
    }
  }

  /**
   * Returns a (visibleItem, totalItemCount) pair given a linear layout manager.
   */
  private @NonNull Pair<Integer, Integer> displayedItemFromLinearLayout(final @NonNull LinearLayoutManager manager) {
    return new Pair<>(manager.findLastVisibleItemPosition(), manager.getItemCount());
  }

  private boolean visibleItemIsCloseToBottom(final @NonNull Pair<Integer, Integer> visibleItemOfTotal) {
    return visibleItemOfTotal.first == visibleItemOfTotal.second - 1;
  }
}
