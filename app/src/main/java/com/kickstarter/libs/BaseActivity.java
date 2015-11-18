package com.kickstarter.libs;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.facebook.appevents.AppEventsLogger;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.libs.utils.BundleUtils;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.RxLifecycle;
import com.trello.rxlifecycle.components.ActivityLifecycleProvider;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class BaseActivity<PresenterType extends Presenter> extends AppCompatActivity implements ActivityLifecycleProvider {
  private final BehaviorSubject<ActivityEvent> lifecycle = BehaviorSubject.create();
  protected PresenterType presenter;
  private static final String PRESENTER_KEY = "presenter";
  private final List<Subscription> subscriptions = new ArrayList<>();

  /**
   * Get presenter.
   *
   * @deprecated TODO: Refactor parent/child presenters for activities and their views.
   */
  @Deprecated
  public PresenterType presenter() {
    return presenter;
  }

  /**
   * Returns an observable of the activity's lifecycle events.
   */
  public final Observable<ActivityEvent> lifecycle() {
    return lifecycle.asObservable();
  }

  /**
   * Completes an observable when an {@link ActivityEvent} occurs in the activity's lifecycle.
   */
  public final <T> Observable.Transformer<T, T> bindUntilEvent(final ActivityEvent event) {
    return RxLifecycle.bindUntilActivityEvent(lifecycle, event);
  }

  /**
   * Completes an observable when the lifecycle event opposing the current lifecyle event is emitted.
   * For example, if a subscription is made during {@link ActivityEvent#CREATE}, the observable will be completed
   * in {@link ActivityEvent#DESTROY}.
   */
  public final <T> Observable.Transformer<T, T> bindToLifecycle() {
    return RxLifecycle.bindActivity(lifecycle);
  }

  @CallSuper
  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Timber.d("onCreate %s", this.toString());

    lifecycle.onNext(ActivityEvent.CREATE);
    fetchPresenter(savedInstanceState);
  }

  @CallSuper
  @Override
  protected void onStart() {
    super.onStart();
    Timber.d("onStart %s", this.toString());
    lifecycle.onNext(ActivityEvent.START);
  }

  @CallSuper
  @Override
  protected void onResume() {
    super.onResume();
    Timber.d("onResume %s", this.toString());
    lifecycle.onNext(ActivityEvent.RESUME);

    // Facebook: logs 'install' and 'app activate' App Events.
    AppEventsLogger.activateApp(this);

    fetchPresenter(null);
    if (presenter != null) {
      presenter.onResume(this);
    }
  }

  @CallSuper
  @Override
  protected void onPause() {
    lifecycle.onNext(ActivityEvent.PAUSE);
    super.onPause();
    Timber.d("onPause %s", this.toString());

    // Facebook: logs 'app deactivate' App Event.
    AppEventsLogger.deactivateApp(this);

    if (presenter != null) {
      presenter.onPause();
    }
  }

  @CallSuper
  @Override
  protected void onStop() {
    lifecycle.onNext(ActivityEvent.STOP);
    super.onStop();
    Timber.d("onStop %s", this.toString());
  }

  @CallSuper
  @Override
  protected void onDestroy() {
    lifecycle.onNext(ActivityEvent.DESTROY);
    super.onDestroy();
    Timber.d("onDestroy %s", this.toString());

    for (final Subscription subscription : subscriptions) {
      subscription.unsubscribe();
    }

    if (isFinishing()) {
      if (presenter != null) {
        Presenters.getInstance().destroy(presenter);
        presenter = null;
      }
    }
  }

  @CallSuper
  @Override
  protected void onSaveInstanceState(@NonNull final Bundle outState) {
    super.onSaveInstanceState(outState);
    Timber.d("onSaveInstanceState %s", this.toString());

    final Bundle presenterEnvelope = new Bundle();
    if (presenter != null) {
      Presenters.getInstance().save(presenter, presenterEnvelope);
    }

    outState.putBundle(PRESENTER_KEY, presenterEnvelope);
  }

  protected final void startActivityWithTransition(@NonNull final Intent intent, @AnimRes final int enterAnim,
    @AnimRes final int exitAnim) {
    startActivity(intent);
    overridePendingTransition(enterAnim, exitAnim);
  }

  protected final void addSubscription(@NonNull final Subscription subscription) {
    subscriptions.add(subscription);
  }

  private void fetchPresenter(@Nullable final Bundle presenterEnvelope) {
    if (presenter == null) {
      final RequiresPresenter annotation = getClass().getAnnotation(RequiresPresenter.class);
      final Class<PresenterType> presenterClass = annotation == null ? null : (Class<PresenterType>) annotation.value();
      if (presenterClass != null) {
        presenter = Presenters.getInstance().fetch(this,
          presenterClass,
          BundleUtils.maybeGetBundle(presenterEnvelope, PRESENTER_KEY));
      }
    }
  }
}
