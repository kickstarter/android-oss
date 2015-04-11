package com.kickstarter.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
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
    Picasso.with(viewHolder.view.getContext()).
      load(project.photo().full()).
      into(viewHolder.photo);
  }

  // Create the view
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    View view = LayoutInflater.
      from(viewGroup.getContext()).
      inflate(R.layout.project_card_view, viewGroup, false);

    return new ViewHolder(view, presenter);
  }

  @Override
  public int getItemCount() {
    return projects.size();
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
}
