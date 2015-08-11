package com.kickstarter.ui.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.kickstarter.ui.adapter_delegates.ProjectCardMiniDelegate;

import java.util.List;

public class ProjectCardMiniAdapter extends RecyclerView.Adapter {
  final int PROJECT_CARD_MINI_VIEW_TYPE = 1;

  private final ProjectCardMiniDelegate projectCardMiniDelegate;
  private List items;

  public ProjectCardMiniAdapter(final Activity activity, final List items) {
    this.items = items;
    this.projectCardMiniDelegate = new ProjectCardMiniDelegate(activity, PROJECT_CARD_MINI_VIEW_TYPE);
  }

  @Override
  public int getItemViewType(final int position) {
    if (projectCardMiniDelegate.isForViewType(items, position)) {
      return projectCardMiniDelegate.viewType();
    }

    throw new IllegalArgumentException("No delegate found");
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int viewType) {
    if (projectCardMiniDelegate.viewType() == viewType) {
      return projectCardMiniDelegate.onCreateViewHolder(viewGroup);
    }

    throw new IllegalArgumentException("No delegate found");
  }

  @Override
  public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int i) {
    final int viewType = viewHolder.getItemViewType();
    if (projectCardMiniDelegate.viewType() == viewType) {
      projectCardMiniDelegate.onBindViewHolder(items, i, viewHolder);
    }
  }

  @Override
  public int getItemCount() {
    return items.size();
  }
}
