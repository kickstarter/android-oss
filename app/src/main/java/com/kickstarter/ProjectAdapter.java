package com.kickstarter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kickstarter.models.Project;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
  private List<Project> projects;

  public ProjectAdapter(List<Project> projects) {
    this.projects = projects;
  }

  @Override
  public int getItemCount() {
    return projects.size();
  }

  @Override
  public void onBindViewHolder(ProjectViewHolder projectViewHolder, int i) {
    Project project = projects.get(i);
    projectViewHolder.vName.setText(project.name());
  }


  @Override
  public ProjectViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    View itemView = LayoutInflater.
      from(viewGroup.getContext()).
      inflate(R.layout.project_layout, viewGroup, false);

    return new ProjectViewHolder(itemView);
  }

  public static class ProjectViewHolder extends RecyclerView.ViewHolder {
    protected TextView vName;

    public ProjectViewHolder(View v) {
      super(v);
      vName = (TextView) v.findViewById(R.id.txtName);
    }
  }
}
