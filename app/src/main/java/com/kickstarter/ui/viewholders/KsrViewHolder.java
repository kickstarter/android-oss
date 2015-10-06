package com.kickstarter.ui.viewholders;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import timber.log.Timber;

public abstract class KsrViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
  protected final View view;

  public KsrViewHolder(@NonNull final View view) {
    super(view);
    this.view = view;

    view.setOnClickListener(this);
  }

  /**
   * No-op click implementation. Subclasses should override this method to implement click handling.
   */
  @Override
  public void onClick(@NonNull final View view) {
    Timber.d("Default KsrViewHolder onClick event");
  }

  /**
   * Populate a view with data. Subclasses should override this method and cast the Object to the
   * appropriate type.
   */
  abstract public void onBind(@NonNull final Object datum);
}
