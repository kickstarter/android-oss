package com.kickstarter.libs.recyclerhelpers;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.libs.utils.extensions.BoolenExtKt;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.subjects.PublishSubject;
import kotlin.Unit;


public final class RecyclerViewPaginatorV2 {
  private final @NonNull RecyclerView recyclerView;
  private final @NonNull Boolean isScrollEnabled;
  private final @NonNull Action nextPage;
  private final Observable<Boolean> isLoading;
  private Disposable subscription;
  private Disposable retrySubscription;
  private final PublishSubject<Unit> retryLoadingNextPageSubject =  PublishSubject.create();

  public RecyclerViewPaginatorV2(final @NonNull RecyclerView recyclerView, final @NonNull Action nextPage, final @NonNull Observable<Boolean> isLoading) {
    this.recyclerView = recyclerView;
    this.nextPage = nextPage;
    this.isLoading = isLoading;
    this.isScrollEnabled =true;
    start();
  }

  public RecyclerViewPaginatorV2(final @NonNull RecyclerView recyclerView, final @NonNull Action nextPage, final @NonNull Observable<Boolean> isLoading, final @NonNull Boolean isScrollEnabled) {
    this.recyclerView = recyclerView;
    this.nextPage = nextPage;
    this.isLoading = isLoading;
    this.isScrollEnabled =isScrollEnabled;
    start();
  }

  /**
   * Begin listening to the recycler view scroll events to determine
   * when pagination should happen.
   */
  public void start() {
    stop();

    final Observable<Pair<Integer, Integer>> lastVisibleAndCount = RxRecyclerView.Companion.scrollEvents(this.recyclerView)
      .filter(__ -> BoolenExtKt.isFalse(Secrets.IS_OSS))
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
      .subscribe(__ ->{
        if(this.isScrollEnabled) {
          this.nextPage.run();
        }
      });

    this.retrySubscription = this.retryLoadingNextPageSubject
            .subscribe(__ ->
            this.nextPage.run()
    );
  }

  public void reload() {
    this.retryLoadingNextPageSubject.onNext(null);
  }

  /**
   * Stop listening to recycler view scroll events and discard the
   * associated resources. This should be done when the object that
   * created `this` is released.
   */
  public void stop() {
    if (this.subscription != null) {
      this.subscription.dispose();
      this.subscription = null;
    }
    if (this.retrySubscription != null) {
      this.retrySubscription.dispose();
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
