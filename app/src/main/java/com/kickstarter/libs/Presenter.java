package com.kickstarter.libs;

import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class Presenter<ViewType> {
  private ViewType view;
  protected final PublishSubject<ViewType> viewChange = PublishSubject.create();
  protected final Observable<ViewType> viewSubject = viewChange.filter(v -> v != null);
  private final List<Subscription> subscriptions = new ArrayList<>();

  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    Timber.d("onCreate %s", this.toString());
    dropView();
  }

  protected void onResume(final ViewType view) {
    Timber.d("onResume %s", this.toString());
    onTakeView(view);
  }

  protected void onPause() {
    Timber.d("onPause %s", this.toString());
    dropView();
  }

  protected void onDestroy() {
    Timber.d("onDestroy %s", this.toString());
    for (final Subscription subscription : subscriptions) {
      subscription.unsubscribe();
    }
    viewChange.onCompleted();
  }

  protected void onTakeView(final ViewType view) {
    Timber.d("onTakeView %s %s", this.toString(), view.toString());
    this.view = view;
    viewChange.onNext(view);
  }

  protected void dropView() {
    Timber.d("dropView %s", this.toString());
    this.view = null;
    viewChange.onNext(null);
  }

  protected final ViewType view() {
    return this.view;
  }

  protected final boolean hasView() {
    return this.view != null;
  }

  public final Observable<ViewType> viewSubject() {
    return viewSubject;
  }
  public final PublishSubject<ViewType> viewChange() {
    return viewChange;
  }

  public final void addSubscription (final Subscription subscription) {
    subscriptions.add(subscription);
  }

  public final <T> Subscription subscribeTo(final Observable<T> ob) {
    Subscription s = ob.subscribe();
    subscriptions.add(s);
    return s;
  }

  public final <T> Subscription subscribeTo(final Observable<T> ob, final Action1<? super T> onNext) {
    final Subscription s = ob.subscribe(onNext);
    subscriptions.add(s);
    return s;
  }

  public final <T> Subscription subscribeTo(final Observable<T> ob, final Action1<? super T> onNext, final Action1<Throwable> onError) {
    final Subscription s = ob.subscribe(onNext, onError);
    subscriptions.add(s);
    return s;
  }

  public final <T> Subscription subscribeTo(final Observable<T> ob, final Action1<? super T> onNext, final Action1<Throwable> onError, final Action0 onComplete) {
    final Subscription s = ob.subscribe(onNext, onError, onComplete);
    subscriptions.add(s);
    return s;
  }

  public final void save(final Bundle state) {
    Timber.d("save %s", this.toString());
    // TODO
  }
}
