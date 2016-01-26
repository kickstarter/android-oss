package com.kickstarter.libs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.trello.rxlifecycle.ActivityEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class ViewModel<ViewType extends LifecycleType> {
  @Inject protected Koala koala;

  protected final PublishSubject<ViewType> viewChange = PublishSubject.create();
  protected final Observable<ViewType> view = viewChange.filter(v -> v != null);
  private final List<Subscription> subscriptions = new ArrayList<>();

  @CallSuper
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    Timber.d("onCreate %s", this.toString());
    dropView();
  }

  @CallSuper
  protected void onResume(final @NonNull ViewType view) {
    Timber.d("onResume %s", this.toString());
    onTakeView(view);
  }

  @CallSuper
  protected void onPause() {
    Timber.d("onPause %s", this.toString());
    dropView();
  }

  @CallSuper
  protected void onDestroy() {
    Timber.d("onDestroy %s", this.toString());
    for (final Subscription subscription : subscriptions) {
      subscription.unsubscribe();
    }
    viewChange.onCompleted();
  }

  private void onTakeView(final @NonNull ViewType view) {
    Timber.d("onTakeView %s %s", this.toString(), view.toString());
    viewChange.onNext(view);
  }

  private void dropView() {
    Timber.d("dropView %s", this.toString());
    viewChange.onNext(null);
  }

  protected final Observable<ViewType> view() {
    return view;
  }

  @Deprecated
  public final void addSubscription(final @NonNull Subscription subscription) {
    subscriptions.add(subscription);
  }

  @CallSuper
  protected void save(final @NonNull Bundle state) {
    Timber.d("save %s", this.toString());
    // TODO
  }

  /**
   * By composing this transformer with an observable you guarantee that every observable in your view model
   * will be properly completed when the view model completes.
   *
   * It is required that *every* observable in a view model do `.compose(bindToLifecycle())` before calling
   * `subscribe`.
   */
  public @NonNull <T> Observable.Transformer<T, T> bindToLifecycle() {
    return source -> source.takeUntil(
      view.flatMap(v -> v.lifecycle().map(e -> Pair.create(v, e)))
        .filter(ve -> isFinished(ve.first, ve.second))
    );
  }

  /**
   * Determines from a view and lifecycle event if the view's life is over.
   */
  private boolean isFinished(final @NonNull ViewType view, final @NonNull ActivityEvent event) {

    if (view instanceof BaseActivity) {
      return event == ActivityEvent.DESTROY && ((BaseActivity) view).isFinishing();
    }

    return event == ActivityEvent.DESTROY;
  }
}
