package com.kickstarter.libs;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Pair;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.kickstarter.ApplicationComponent;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.extensions.ActivityExtKt;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.BundleUtils;
import com.kickstarter.services.ConnectivityReceiver;
import com.kickstarter.ui.data.ActivityResult;
import com.qualtrics.digital.Qualtrics;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.RxLifecycle;
import com.trello.rxlifecycle.components.ActivityLifecycleProvider;

import androidx.annotation.AnimRes;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public abstract class BaseActivity<ViewModelType extends ActivityViewModel> extends AppCompatActivity implements ActivityLifecycleProvider,
  ActivityLifecycleType, ConnectivityReceiver.ConnectivityReceiverListener {

  private final PublishSubject<Void> back = PublishSubject.create();
  private final BehaviorSubject<ActivityEvent> lifecycle = BehaviorSubject.create();
  private final IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
  private static final String VIEW_MODEL_KEY = "viewModel";
  private final CompositeSubscription subscriptions = new CompositeSubscription();
  private final ConnectivityReceiver connectivityReceiver = new ConnectivityReceiver();
  protected ViewModelType viewModel;

  /**
   * Get viewModel.
   */
  public ViewModelType viewModel() {
    return this.viewModel;
  }

  /**
   * Returns an observable of the activity's lifecycle events.
   */
  public final Observable<ActivityEvent> lifecycle() {
    return this.lifecycle.asObservable();
  }

  /**
   * Completes an observable when an {@link ActivityEvent} occurs in the activity's lifecycle.
   */
  public final <T> Observable.Transformer<T, T> bindUntilEvent(final ActivityEvent event) {
    return RxLifecycle.bindUntilActivityEvent(this.lifecycle, event);
  }

  /**
   * Completes an observable when the lifecycle event opposing the current lifecyle event is emitted.
   * For example, if a subscription is made during {@link ActivityEvent#CREATE}, the observable will be completed
   * in {@link ActivityEvent#DESTROY}.
   */
  public final <T> Observable.Transformer<T, T> bindToLifecycle() {
    return RxLifecycle.bindActivity(this.lifecycle);
  }

  /**
   * Sends activity result data to the view model.
   */
  @CallSuper
  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final @Nullable Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    this.viewModel.activityResult(ActivityResult.create(requestCode, resultCode, intent));
  }

  @CallSuper
  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Timber.d("onCreate %s", this.toString());

    this.lifecycle.onNext(ActivityEvent.CREATE);

    assignViewModel(savedInstanceState);

    this.viewModel.intent(getIntent());

    Qualtrics.instance().properties.setString("activity", this.getClass().getSimpleName());
    Qualtrics.instance().registerViewVisit(this.getClass().getSimpleName());
  }

  /**
   * Called when an activity is set to `singleTop` and it is relaunched while at the top of the activity stack.
   */
  @CallSuper
  @Override
  protected void onNewIntent(final Intent intent) {
    super.onNewIntent(intent);
    this.viewModel.intent(intent);
  }

  @CallSuper
  @Override
  protected void onStart() {
    super.onStart();
    Timber.d("onStart %s", this.toString());
    this.lifecycle.onNext(ActivityEvent.START);

    this.back
      .compose(bindUntilEvent(ActivityEvent.STOP))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> goBack(), FirebaseCrashlytics.getInstance()::recordException);

    ConnectivityReceiver.setConnectivityReceiverListener(this);
  }

  @CallSuper
  @Override
  protected void onResume() {
    super.onResume();
    Timber.d("onResume %s", this.toString());
    this.lifecycle.onNext(ActivityEvent.RESUME);

    assignViewModel(null);
    if (this.viewModel != null) {
      this.viewModel.onResume(this);
    }

    this.registerReceiver(this.connectivityReceiver, this.filter);
  }

  @CallSuper
  @Override
  protected void onPause() {
    this.lifecycle.onNext(ActivityEvent.PAUSE);
    super.onPause();
    Timber.d("onPause %s", this.toString());

    if (this.viewModel != null) {
      this.viewModel.onPause();
    }

    this.unregisterReceiver(this.connectivityReceiver);
  }

  @CallSuper
  @Override
  protected void onStop() {
    this.lifecycle.onNext(ActivityEvent.STOP);
    super.onStop();
    Timber.d("onStop %s", this.toString());
  }

  @CallSuper
  @Override
  protected void onDestroy() {
    this.lifecycle.onNext(ActivityEvent.DESTROY);
    super.onDestroy();
    Timber.d("onDestroy %s", this.toString());

    this.subscriptions.clear();

    if (isFinishing()) {
      if (this.viewModel != null) {
        ActivityViewModelManager.getInstance().destroy(this.viewModel);
        this.viewModel = null;
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

  /** This is called when a user loses data or Wi-Fi connection.
   * When the user loses connection we will show our network error Snackbar.
   * We're also using (findViewById(android.R.id.content) to get the root view of our activity
   * so whatever Activity the user navigates to while disconnected the error will display.
   */
  @Override
  public void onNetworkConnectionChanged(final boolean isConnected) {
    if (!isConnected) {
      ActivityExtKt.showSnackbar(findViewById(android.R.id.content), getString(R.string.Youre_offline));
    }
  }
  /**
   * Call when the user wants triggers a back event, e.g. clicking back in a toolbar or pressing the device back button.
   */
  public void back() {
    this.back.onNext(null);
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
    if (this.viewModel != null) {
      ActivityViewModelManager.getInstance().save(this.viewModel, viewModelEnvelope);
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
   * Returns the application's {@link Environment}.
   */
  @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
  public @NonNull Environment environment() {
    return component().environment();
  }

  /**
   * @deprecated Use {@link #bindToLifecycle()} or {@link #bindUntilEvent(ActivityEvent)} instead.
   */
  @Deprecated
  protected final void addSubscription(final @NonNull Subscription subscription) {
    this.subscriptions.add(subscription);
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

  private void assignViewModel(final @Nullable Bundle viewModelEnvelope) {
    if (this.viewModel == null) {
      final RequiresActivityViewModel annotation = getClass().getAnnotation(RequiresActivityViewModel.class);
      final Class<ViewModelType> viewModelClass = annotation == null ? null : (Class<ViewModelType>) annotation.value();
      if (viewModelClass != null) {
        this.viewModel = ActivityViewModelManager.getInstance().fetch(this,
          viewModelClass,
          BundleUtils.maybeGetBundle(viewModelEnvelope, VIEW_MODEL_KEY));
      }
    }
  }
}
