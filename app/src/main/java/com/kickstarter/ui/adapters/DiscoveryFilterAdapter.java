package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.DiscoveryFilterViewHolder;

import java.util.List;

public class DiscoveryFilterAdapter extends RecyclerView.Adapter<DiscoveryFilterViewHolder> {
  private List<Category> categories;
  public DiscoveryFilterAdapter(final List<Category> categories) {
    this.categories = categories;
  }

  @Override
  public DiscoveryFilterViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
    final View view = LayoutInflater
      .from(viewGroup.getContext())
      .inflate(R.layout.category_filter_view, viewGroup, false);

    return new DiscoveryFilterViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final DiscoveryFilterViewHolder viewHolder, final int position) {
    viewHolder.onBind(categories.get(position));
  }

  @Override
  public int getItemCount() {
    return categories.size();
  }
}
