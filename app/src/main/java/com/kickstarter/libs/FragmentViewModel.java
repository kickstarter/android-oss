package com.kickstarter.libs;

import android.content.Context;
import android.os.Bundle;

import com.kickstarter.libs.utils.ObjectUtils;
import com.trello.rxlifecycle.FragmentEvent;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * A view model bound to the lifecycle and arguments of a Fragment, from ATTACH to DETACH.
 */
public class FragmentViewModel<ViewType extends FragmentLifecycleType> {

  private final PublishSubject<ViewType> viewChange = PublishSubject.create();
  private final Observable<ViewType> view = this.viewChange.filter(ObjectUtils::isNotNull);

  private final PublishSubject<Bundle> arguments = PublishSubject.create();
  protected final Koala koala;

  public FragmentViewModel(final @NonNull Environment environment) {
    this.koala = environment.koala();
  }

  @CallSuper
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    Timber.d("onCreate %s", this.toString());
    dropView();
  }

  /**
   * Takes bundle arguments from the view.
   */
  public void arguments(final @Nullable Bundle bundle) {
    this.arguments.onNext(bundle);
  }

  protected @NonNull Observable<Bundle> arguments() {
    return this.arguments;
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
    dropView();
  }

  @CallSuper
  protected void onDetach() {
    Timber.d("onDetach %s", this.toString());
    this.viewChange.onCompleted();
  }

  private void onTakeView(final @NonNull ViewType view) {
    Timber.d("onTakeView %s %s", this.toString(), view.toString());
    this.viewChange.onNext(view);
  }

  private void dropView() {
    Timber.d("dropView %s", this.toString());
    this.viewChange.onNext(null);
  }

  protected final @NonNull Observable<ViewType> view() {
    return this.view;
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
      this.view.switchMap(FragmentLifecycleType::lifecycle)
        .filter(FragmentEvent.DETACH::equals)
    );
  }
}
