package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import timber.log.Timber;

public abstract class KSViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
  protected final View view;

  public KSViewHolder(@NonNull final View view) {
    super(view);
    this.view = view;

    view.setOnClickListener(this);
  }

  /**
   * No-op click implementation. Subclasses should override this method to implement click handling.
   */
  @Override
  public void onClick(@NonNull final View view) {
    Timber.d("Default KSViewHolder onClick event");
  }

  /**
   * Populate a view with data that was bound in `bindData`.
   */
  abstract public void onBind();

  /**
   * Implementations of this should inspect `data` to set instance variables in the view holder that
   * `onBind` can then use without worrying about type safety.
   *
   * @return Return a `boolean` that indicates if this binding happened successfully.
   */
  abstract public void bindData(final @Nullable Object data) throws Exception;
}
