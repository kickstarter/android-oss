package com.kickstarter.ui.adapter_delegates;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.ui.view_holders.ProjectContextViewHolder;

public class ProjectContextAdapterDelegate {
  private int viewType;
  private Project project;

  public ProjectContextAdapterDelegate(int viewType, Project project) {
    this.viewType = viewType;
    this.project = project;
  }

  public int viewType() {
    return viewType;
  }

  public boolean isForViewType(int position) {
    return (position == 0);  // we want the header to be the first item
  }

  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
    LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
    return new ProjectContextViewHolder(inflater.inflate(R.layout.project_context_view, viewGroup, false));
  }

  public void onBindViewHolder(RecyclerView.ViewHolder holder) {
    ProjectContextViewHolder contextHolder = (ProjectContextViewHolder) holder;
    contextHolder.onBind(project);
  }
}
