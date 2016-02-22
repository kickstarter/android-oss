package com.kickstarter.libs;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;

import com.kickstarter.ApplicationComponent;
import com.kickstarter.KSApplication;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.BundleUtils;
import com.kickstarter.ui.data.ActivityResult;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.RxLifecycle;
import com.trello.rxlifecycle.components.ActivityLifecycleProvider;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class BaseActivity<ViewModelType extends ViewModel> extends AppCompatActivity implements ActivityLifecycleProvider,
  LifecycleType {

  private final PublishSubject<Void> back = PublishSubject.create();
  private final BehaviorSubject<ActivityEvent> lifecycle = BehaviorSubject.create();
  protected ViewModelType viewModel;
  private static final String VIEW_MODEL_KEY = "viewModel";
  private final List<Subscription> subscriptions = new ArrayList<>();

  /**
   * Get viewModel.
   *
   * @deprecated TODO: Refactor parent/child view models for activities and their views.
   */
  @Deprecated
  public ViewModelType viewModel() {
    return viewModel;
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

  /**
   * Sends activity result data to the view model.
   */
  @CallSuper
  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final @Nullable Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    viewModel.activityResult(ActivityResult.create(requestCode, resultCode, intent));
  }

  @CallSuper
  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Timber.d("onCreate %s", this.toString());

    lifecycle.onNext(ActivityEvent.CREATE);

    fetchViewModel(savedInstanceState);

    viewModel.intent(getIntent());
  }

  /**
   * Called when an activity is set to `singleTop` and it is relaunched while at the top of the activity stack.
   */
  @CallSuper
  @Override
  protected void onNewIntent(final Intent intent) {
    super.onNewIntent(intent);
    viewModel.intent(intent);
  }

  @CallSuper
  @Override
  protected void onStart() {
    super.onStart();
    Timber.d("onStart %s", this.toString());
    lifecycle.onNext(ActivityEvent.START);

    back
      .compose(bindUntilEvent(ActivityEvent.STOP))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> goBack());
  }

  @CallSuper
  @Override
  protected void onResume() {
    super.onResume();
    Timber.d("onResume %s", this.toString());
    lifecycle.onNext(ActivityEvent.RESUME);

    fetchViewModel(null);
    if (viewModel != null) {
      viewModel.onResume(this);
    }
  }

  @CallSuper
  @Override
  protected void onPause() {
    lifecycle.onNext(ActivityEvent.PAUSE);
    super.onPause();
    Timber.d("onPause %s", this.toString());

    if (viewModel != null) {
      viewModel.onPause();
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
      if (viewModel != null) {
        ViewModels.getInstance().destroy(viewModel);
        viewModel = null;
      }
    }
  }

  /**
   * @deprecated Use {@link #back()} instead.
   *
   *             In rare situations, onBackPressed can be triggered after {@link #onSaveInstanceState(Bundle)} has been called.
   *             This causes an {@link IllegalStateException} in the fragment manager's `checkStateLoss` method, because the
   *             UI state has changed after being saved. The sequence of events might look like this:
   *
   *             onSaveInstanceState -> onStop -> onBackPressed
   *
   *             To avoid that situation, we need to ignore calls to `onBackPressed` after the activity has been saved. Since
   *             the activity is stopped after `onSaveInstanceState` is called, we can create an observable of back events,
   *             and a subscription that calls super.onBackPressed() only when the activity has not been stopped.
   */
  @CallSuper
  @Override
  @Deprecated
  public void onBackPressed() {
    back();
  }

  /**
   * Call when the user wants triggers a back event, e.g. clicking back in a toolbar or pressing the device back button.
   */
  public void back() {
    back.onNext(null);
  }

  /**
   * Override in subclasses for custom exit transitions. First item in pair is the enter animation,
   * second item in pair is the exit animation.
   */
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return null;
  }

  @CallSuper
  @Override
  protected void onSaveInstanceState(final @NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    Timber.d("onSaveInstanceState %s", this.toString());

    final Bundle viewModelEnvelope = new Bundle();
    if (viewModel != null) {
      ViewModels.getInstance().save(viewModel, viewModelEnvelope);
    }

    outState.putBundle(VIEW_MODEL_KEY, viewModelEnvelope);
  }

  protected final void startActivityWithTransition(final @NonNull Intent intent, final @AnimRes int enterAnim,
    final @AnimRes int exitAnim) {
    startActivity(intent);
    overridePendingTransition(enterAnim, exitAnim);
  }

  /**
   * Returns the {@link KSApplication} instance.
   */
  protected @NonNull KSApplication application() {
    return (KSApplication) getApplication();
  }

  /**
   * Convenience method to return a Dagger component.
   */
  protected @NonNull ApplicationComponent component() {
    return application().component();
  }

  /**
   * @deprecated Use {@link #bindToLifecycle()} or {@link #bindUntilEvent(ActivityEvent)} instead.
   */
  @Deprecated
  protected final void addSubscription(final @NonNull Subscription subscription) {
    subscriptions.add(subscription);
  }

  /**
   * Triggers a back press with an optional transition.
   */
  private void goBack() {
    super.onBackPressed();

    final Pair<Integer, Integer> exitTransitions = exitTransition();
    if (exitTransitions != null) {
      overridePendingTransition(exitTransitions.first, exitTransitions.second);
    }
  }

  private void fetchViewModel(final @Nullable Bundle viewModelEnvelope) {
    if (viewModel == null) {
      final RequiresViewModel annotation = getClass().getAnnotation(RequiresViewModel.class);
      final Class<ViewModelType> viewModelClass = annotation == null ? null : (Class<ViewModelType>) annotation.value();
      if (viewModelClass != null) {
        viewModel = ViewModels.getInstance().fetch(this,
          viewModelClass,
          BundleUtils.maybeGetBundle(viewModelEnvelope, VIEW_MODEL_KEY));
      }
    }
  }
}
