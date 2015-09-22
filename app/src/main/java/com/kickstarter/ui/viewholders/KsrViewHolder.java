package com.kickstarter.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class KsrViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
  protected final View view;

  public KsrViewHolder(final View view) {
    super(view);
    this.view = view;

    view.setOnClickListener(this);
  }

  @Override
  public void onClick(final View view) {
  }
  abstract public void onBind(final Object datum);
}
