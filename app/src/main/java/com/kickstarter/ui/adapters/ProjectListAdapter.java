package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.DiscoveryPresenter;
import com.kickstarter.ui.view_holders.ProjectListViewHolder;

import java.util.List;

public class ProjectListAdapter extends RecyclerView.Adapter<ProjectListViewHolder> {
  private List<Project> projects;
  private DiscoveryPresenter presenter;

  public ProjectListAdapter(List<Project> projects, DiscoveryPresenter presenter) {
    this.projects = projects;
    this.presenter = presenter;
  }

  // Attach data to the view
  @Override
  public void onBindViewHolder(final ProjectListViewHolder viewHolder, final int i) {
    // The viewHolder is recycled for different projects, so be careful with
    // conditionals - if you mutate a view for one project, you should set it back
    // to the default when the view is recycled.
    viewHolder.onBind(projects.get(i));
  }

  @Override
  public ProjectListViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i) {
    final View view = LayoutInflater.
      from(viewGroup.getContext()).
      inflate(R.layout.project_card_view, viewGroup, false);

    return new ProjectListViewHolder(view, presenter);
  }

  @Override
  public int getItemCount() {
    return projects.size();
  }
}
