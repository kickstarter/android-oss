package com.kickstarter.adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.kickstarter.R;
import com.kickstarter.activities.DiscoveryActivity;
import com.kickstarter.activities.ProjectActivity;
import com.kickstarter.models.Project;

import java.util.List;

public class DiscoveryAdapter extends RecyclerView.Adapter<DiscoveryAdapter.ProjectViewHolder> {
  private List<Project> projects;

  public DiscoveryAdapter(List<Project> projects) {
    this.projects = projects;
  }

  @Override
  public int getItemCount() {
    return projects.size();
  }

  // Attach data to the view
  @Override
  public void onBindViewHolder(ProjectViewHolder projectViewHolder, int i) {
    Project project = projects.get(i);
    projectViewHolder.currentProject = project;
    projectViewHolder.vCategory.setText(project.category().name());
    projectViewHolder.vLocation.setText(project.location().name());
    projectViewHolder.vName.setText(project.name());

    Uri uri = Uri.parse(project.photo().full());
    projectViewHolder.vPhoto.setImageURI(uri);
  }

  // Create the view
  @Override
  public ProjectViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    View itemView = LayoutInflater.
      from(viewGroup.getContext()).
      inflate(R.layout.project_card_view, viewGroup, false);

    return new ProjectViewHolder(itemView);
  }

  public static class ProjectViewHolder extends RecyclerView.ViewHolder {
    protected TextView vCategory;
    protected TextView vLocation;
    protected TextView vName;
    protected SimpleDraweeView vPhoto;
    public View view;
    public Project currentProject;

    public ProjectViewHolder(View view) {
      super(view);
      vCategory = (TextView) view.findViewById(R.id.category);
      vLocation= (TextView) view.findViewById(R.id.location);
      vName = (TextView) view.findViewById(R.id.name);
      vPhoto = (SimpleDraweeView) view.findViewById(R.id.photo);

      this.view = view;
      view.setOnClickListener((View v) -> {
        Intent intent = new Intent(v.getContext(), ProjectActivity.class);
        intent.putExtra("project", currentProject);
        v.getContext().startActivity(intent);
      });
    }
  }
}
