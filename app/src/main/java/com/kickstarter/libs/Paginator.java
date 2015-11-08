package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;

import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;

import rx.Subscription;
import rx.functions.Action0;

public final class Paginator {
  @NonNull private final RecyclerView recyclerView;
  @NonNull private final Action0 nextPage;
  private Subscription subscription = null;

  public Paginator(@NonNull final RecyclerView recyclerView, @NonNull final Action0 nextPage) {
    this.recyclerView = recyclerView;
    this.nextPage = nextPage;
    start();
  }

  public void start() {
    stop();

    subscription = RxRecyclerView.scrollEvents(recyclerView)
      .map(__ -> recyclerView.getLayoutManager())
      .ofType(LinearLayoutManager.class)
      .map(this::displayedItemFromLinearLayout)
      .distinctUntilChanged()
      .filter(this::visibleItemIsCloseToBottom)
      .subscribe(__ -> nextPage.call());
  }

  public void stop() {
    if (subscription != null) {
      subscription.unsubscribe();
      subscription = null;
    }
  }

  /**
   * Returns a (visibleItem, totalItemCount) pair given a linear layout manager.
   */
  @NonNull private Pair<Integer, Integer> displayedItemFromLinearLayout(@NonNull final LinearLayoutManager manager) {
    final int visibleItemCount = manager.getChildCount();
    final int totalItemCount = manager.getItemCount();
    final int pastVisibleItems = manager.findFirstVisibleItemPosition();
    return new Pair<>(visibleItemCount + pastVisibleItems, totalItemCount);
  }

  private boolean visibleItemIsCloseToBottom(@NonNull final Pair<Integer, Integer> visibleItemOfTotal) {
    return visibleItemOfTotal.first == visibleItemOfTotal.second - 2;
  }
}
