package com.kickstarter.libs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class ViewModel<ViewType> {
  @Inject protected Koala koala;

  protected final PublishSubject<ViewType> viewChange = PublishSubject.create();
  protected final Observable<ViewType> view = viewChange.filter(v -> v != null);
  private final List<Subscription> subscriptions = new ArrayList<>();

  // TODO: Justify BehaviorSubject vs PublishSubject
  protected final BehaviorSubject<Intent> intent = BehaviorSubject.create();
  public void intent(final @NonNull Intent intent) {
    this.intent.onNext(intent);
  }

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
    viewChange.onNext(view);
  }

  private void dropView() {
    Timber.d("dropView %s", this.toString());
    viewChange.onNext(null);
  }

  public final Observable<ViewType> view() {
    return view;
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
