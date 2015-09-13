package com.kickstarter.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

public class DiscoveryFilterViewHolder extends RecyclerView.ViewHolder {
  protected View view;

  public DiscoveryFilterViewHolder(final View view) {
    super(view);
    this.view = view;
    ButterKnife.bind(this, view);
  }

  public void onBind() {
  }
}
