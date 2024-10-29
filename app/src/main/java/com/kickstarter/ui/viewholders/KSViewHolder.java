package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Environment;
import timber.log.Timber;

public abstract class KSViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

  private final View view;

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
   * @param data dataModel to be bind to the viewHolder
   * @throws Exception Raised when binding is unsuccessful.
   */
  abstract public void bindData(final @Nullable Object data) throws Exception;


  /**
   * Called when the ViewHolder is being detached. Subclasses should override if they need to do any work
   * when the ViewHolder is being de-allocated.
   */
  public void destroy() {}

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
