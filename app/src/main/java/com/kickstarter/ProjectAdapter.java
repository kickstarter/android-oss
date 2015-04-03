package com.kickstarter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
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

    Uri uri = Uri.parse(project.photo().full());
    projectViewHolder.vPhoto.setImageURI(uri);
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
    protected SimpleDraweeView vPhoto;

    public ProjectViewHolder(View v) {
      super(v);
      vName = (TextView) v.findViewById(R.id.name);
      vPhoto = (SimpleDraweeView) v.findViewById(R.id.photo);
    }
  }
}
