package com.kickstarter.libs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class ViewModel<ViewType> {
  @Inject protected Koala koala;

  private ViewType view;
  protected final PublishSubject<ViewType> viewChange = PublishSubject.create();
  protected final Observable<ViewType> viewSubject = viewChange.filter(v -> v != null);
  private final List<Subscription> subscriptions = new ArrayList<>();

  @CallSuper
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    Timber.d("onCreate %s", this.toString());
    dropView();
  }

  @CallSuper
  protected void onResume(@NonNull final ViewType view) {
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

  private void onTakeView(@NonNull final ViewType view) {
    Timber.d("onTakeView %s %s", this.toString(), view.toString());
    this.view = view;
    viewChange.onNext(view);
  }

  private void dropView() {
    Timber.d("dropView %s", this.toString());
    this.view = null;
    viewChange.onNext(null);
  }

  /**
   * Get view object attached to view model. View can be null.
   *
   * @deprecated Not Reactive, use observables instead.
   */
  @Deprecated
  protected final @Nullable ViewType view() {
    return this.view;
  }

  public final Observable<ViewType> viewSubject() {
    return viewSubject;
  }

  public final PublishSubject<ViewType> viewChange() {
    return viewChange;
  }

  public final void addSubscription(@NonNull final Subscription subscription) {
    subscriptions.add(subscription);
  }

  @CallSuper
  protected void save(@NonNull final Bundle state) {
    Timber.d("save %s", this.toString());
    // TODO
  }
}
