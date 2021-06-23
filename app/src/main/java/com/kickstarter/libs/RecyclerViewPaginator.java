package com.kickstarter.libs;

import android.util.Pair;

import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.Secrets;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import io.reactivex.disposables.CompositeDisposable;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action0;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;

public final class RecyclerViewPaginator {
  private final @NonNull RecyclerView recyclerView;
  private final @NonNull Action0 nextPage;
  private final Observable<Boolean> isLoading;
  private Subscription subscription;
  private static final int DIRECTION_DOWN = 1;
  private Subscription retrySubscription;
  private final PublishSubject<Void> retryLoadingNextPageSubject =  PublishSubject.create();

  public RecyclerViewPaginator(final @NonNull RecyclerView recyclerView, final @NonNull Action0 nextPage, final @NonNull Observable<Boolean> isLoading) {
    this.recyclerView = recyclerView;
    this.nextPage = nextPage;
    this.isLoading = isLoading;
    start();
  }

  /**
   * Begin listening to the recycler view scroll events to determine
   * when pagination should happen.
   */
  public void start() {
    stop();

    final Observable<Pair<Integer, Integer>> lastVisibleAndCount = RxRecyclerView.scrollEvents(this.recyclerView)
      .filter(__ -> BooleanUtils.isFalse(Secrets.IS_OSS))
      .filter(__ -> this.recyclerView.canScrollVertically(DIRECTION_DOWN))
      .map(__ -> this.recyclerView.getLayoutManager())
      .ofType(LinearLayoutManager.class)
      .map(this::displayedItemFromLinearLayout)
      .filter(item -> item.second != 0)
      .distinctUntilChanged();

    final Observable<Boolean> isNotLoading = this.isLoading
      .distinctUntilChanged()
      .filter(loading -> !loading);

    final Observable<Pair<Integer, Integer>> loadNextPage = lastVisibleAndCount
      .compose(combineLatestPair(isNotLoading))
      .distinctUntilChanged()
      .map(p -> p.first)
      .filter(this::visibleItemIsCloseToBottom);

    this.subscription = loadNextPage
      .subscribe(__ -> this.nextPage.call());

    this.retrySubscription = retryLoadingNextPageSubject
            .subscribe(__ ->
            this.nextPage.call()
    );
  }

  public void reload() {
    retryLoadingNextPageSubject.onNext(null);
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
    if (this.retrySubscription != null) {
      this.retrySubscription.unsubscribe();
      this.retrySubscription = null;
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
