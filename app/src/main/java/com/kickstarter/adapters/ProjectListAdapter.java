package com.kickstarter.adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.activities.DiscoveryActivity;
import com.kickstarter.activities.ProjectDetailActivity;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.DiscoveryPresenter;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProjectListAdapter extends RecyclerView.Adapter<ProjectListAdapter.ViewHolder> {
  private List<Project> projects;
  private DiscoveryPresenter presenter;

  public ProjectListAdapter(List<Project> projects, DiscoveryPresenter presenter) {
    this.projects = projects;
    this.presenter = presenter;
  }

  // Attach data to the view
  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
    final Project project = projects.get(i);
    viewHolder.project = project;

    viewHolder.category.setText(project.category().name());
    viewHolder.location.setText(project.location().name());
    viewHolder.name.setText(project.name());
    Picasso.with(viewHolder.view.getContext()).load(project.photo().full()).into(viewHolder.photo);
  }

  // Create the view
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    View view = LayoutInflater.
      from(viewGroup.getContext()).
      inflate(R.layout.project_card_view, viewGroup, false);

    return new ViewHolder(view, presenter);
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    protected @InjectView(R.id.category) TextView category;
    protected @InjectView(R.id.location) TextView location;
    protected @InjectView(R.id.name) TextView name;
    protected @InjectView(R.id.photo) ImageView photo;
    protected View view;
    protected Project project;
    protected DiscoveryPresenter presenter;

    public ViewHolder(View view, DiscoveryPresenter presenter) {
      super(view);
      this.view = view;
      this.presenter = presenter;
      ButterKnife.inject(this, view);

      view.setOnClickListener((View v) -> {
        this.presenter.onProjectClicked(project, this);
      });
    }

    public TextView category() {
      return category;
    }

    public TextView location() {
      return location;
    }

    public TextView name() {
      return name;
    }

    public ImageView photo() {
      return photo;
    }
  }


  @Override
  public int getItemCount() {
    return projects.size();
  }

  public static class ProjectViewHolder extends RecyclerView.ViewHolder {
    protected TextView vCategory;
    protected TextView vLocation;
    protected TextView vName;
    protected ImageView vPhoto;
    public View view;
    public Project currentProject;

    public ProjectViewHolder(View view) {
      super(view);
      vCategory = (TextView) view.findViewById(R.id.category);
      vLocation = (TextView) view.findViewById(R.id.location);
      vName = (TextView) view.findViewById(R.id.name);
      vPhoto = (ImageView) view.findViewById(R.id.photo);

      this.view = view;
      view.setOnClickListener((View v) -> {
        Intent intent = new Intent(v.getContext(), ProjectDetailActivity.class);
        intent.putExtra("project", currentProject);
        Pair<View, String> p1 = Pair.create(vCategory, "category");
        Pair<View, String> p2 = Pair.create(vPhoto, "photo");
        // The cast here is evil, figure out how to fix it
        ActivityOptionsCompat options = ActivityOptionsCompat.
          makeSceneTransitionAnimation((DiscoveryActivity) v.getContext(), p1, p2);
        v.getContext().startActivity(intent, options.toBundle());
      });
    }
  }
}
