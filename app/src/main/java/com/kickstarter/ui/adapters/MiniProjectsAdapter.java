package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.ui.delegates.MiniProjectsDelegate;
import com.kickstarter.ui.view_holders.MiniProjectsViewHolder;

import java.util.List;

public class MiniProjectsAdapter extends RecyclerView.Adapter<MiniProjectsViewHolder> {
  private List<Project> projects;
  private MiniProjectsDelegate presenter;

  public MiniProjectsAdapter(List<Project> projects, MiniProjectsDelegate presenter) {
    this.projects = projects;
    this.presenter = presenter;
  }

  // Attach data to the view
  @Override
  public void onBindViewHolder(final MiniProjectsViewHolder viewHolder, final int i) {
    // The viewHolder is recycled for different projects, so be careful with
    // conditionals - if you mutate a view for one project, you should set it back
    // to the default when the view is recycled.
    viewHolder.onBind(projects.get(i));
  }

  @Override
  public MiniProjectsViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i) {
    final View view = LayoutInflater.
      from(viewGroup.getContext()).
      inflate(R.layout.mini_project_card_view, viewGroup, false);

    return new MiniProjectsViewHolder(view, presenter);
  }

  @Override
  public int getItemCount() {
    return projects.size();
  }
}
