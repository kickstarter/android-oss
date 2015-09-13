package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.kickstarter.ui.viewholders.DiscoveryFilterViewHolder;

public class DiscoveryFilterAdapter extends RecyclerView.Adapter<DiscoveryFilterViewHolder> {
  public DiscoveryFilterAdapter() {
  }

  @Override
  public int getItemViewType(final int position) {
    return 0;
  }

  @Override
  public DiscoveryFilterViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
    return new DiscoveryFilterViewHolder(viewGroup);
  }

  @Override
  public void onBindViewHolder(final DiscoveryFilterViewHolder viewHolder, final int position) {
    viewHolder.onBind();
  }

  @Override
  public int getItemCount() {
    return 0;
  }
}
