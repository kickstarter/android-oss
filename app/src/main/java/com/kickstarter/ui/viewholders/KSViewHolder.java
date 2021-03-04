package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ActivityLifecycleType;
import com.kickstarter.libs.Environment;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.RxLifecycle;

import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public abstract class KSViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
  ActivityLifecycleType {

  private final View view;
  private final @NonNull PublishSubject<ActivityEvent> lifecycle = PublishSubject.create();

  public KSViewHolder(final @NonNull View view) {
    super(view);
    this.view = view;

    view.setOnClickListener(this);
  }

  /**
   * No-op click implementation. Subclasses should override this method to implement click handling.
   */
  @Override
  public void onClick(final @NonNull View view) {
    Timber.d("Default KSViewHolder projectClicked event");
  }

  /**
   * Populate a view with data that was bound in `bindData`.
   *
   * @deprecated Prefer creating subscriptions to a viewmodel in the constructor, then using #{link #bindData} to
   *             send new data to the viewmodel.
   */
  @Deprecated
  public void onBind() {}

  /**
   * Implementations of this should inspect `data` to set instance variables in the view holder that
   * `onBind` can then use without worrying about type safety.
   *
   * @throws Exception Raised when binding is unsuccessful.
   */
  abstract public void bindData(final @Nullable Object data) throws Exception;

  @Override
  public @NonNull Observable<ActivityEvent> lifecycle() {
    return this.lifecycle;
  }

  /**
   * This method is intended to be called only from `KSAdapter` in order for it to inform the view holder
   * of its lifecycle.
   */
  public void lifecycleEvent(final @NonNull ActivityEvent event) {
    this.lifecycle.onNext(event);

    if (ActivityEvent.DESTROY.equals(event)) {
      destroy();
    }
  }

  /**
   * Completes an observable when an {@link ActivityEvent} occurs in the activity's lifecycle.
   */
  public final @NonNull <T> Observable.Transformer<T, T> bindUntilEvent(final @NonNull ActivityEvent event) {
    return RxLifecycle.bindUntilActivityEvent(this.lifecycle, event);
  }

  /**
   * Completes an observable when the lifecycle event opposing the current lifecyle event is emitted.
   * For example, if a subscription is made during {@link ActivityEvent#CREATE}, the observable will be completed
   * in {@link ActivityEvent#DESTROY}.
   */
  public final @NonNull <T> Observable.Transformer<T, T> bindToLifecycle() {
    return RxLifecycle.bindActivity(this.lifecycle);
  }

  /**
   * Called when the ViewHolder is being detached. Subclasses should override if they need to do any work
   * when the ViewHolder is being de-allocated.
   */
  protected void destroy() {}

  protected @NonNull View view() {
    return this.view;
  }

  protected @NonNull Context context() {
    return this.view.getContext();
  }

  protected @NonNull Environment environment() {
    return ((KSApplication) context().getApplicationContext()).component().environment();
  }
}
