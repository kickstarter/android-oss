package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.DiscoveryPresenter;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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
    // The viewHolder is recycled for different projects, so be careful with
    // conditionals - if you mutate a view for one project, you should set it back
    // to the default when the view is recycled.
    final Project project = projects.get(i);
    viewHolder.project = project;

    // TODO: Extract number formatting into helpers
    viewHolder.backers_count.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(project.backersCount()));
    viewHolder.category.setText(project.category().name());
    viewHolder.deadline_countdown.setText(Integer.toString(project.deadlineCountdown()));
    viewHolder.deadline_countdown_unit.setText(project.deadlineCountdownUnit());
    viewHolder.goal.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(project.goal()));
    viewHolder.location.setText(project.location().displayableName());
    viewHolder.pledged.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(project.pledged()));
    viewHolder.name.setText(project.name());
    viewHolder.percentage_funded.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
    Picasso.with(viewHolder.view.getContext()).
      load(project.photo().full()).
      into(viewHolder.photo);

    int potd_visible = project.isPotdToday() ? View.VISIBLE : View.INVISIBLE;
    viewHolder.photo_gradient.setVisibility(potd_visible);
    viewHolder.potd_group.setVisibility(potd_visible);
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
    protected @InjectView(R.id.backers_count) TextView backers_count;
    protected @InjectView(R.id.category) TextView category;
    protected @InjectView(R.id.deadline_countdown) TextView deadline_countdown;
    protected @InjectView(R.id.deadline_countdown_unit) TextView deadline_countdown_unit;
    protected @InjectView(R.id.goal) TextView goal;
    protected @InjectView(R.id.location) TextView location;
    protected @InjectView(R.id.name) TextView name;
    protected @InjectView(R.id.pledged) TextView pledged;
    protected @InjectView(R.id.percentage_funded) ProgressBar percentage_funded;
    protected @InjectView(R.id.photo) ImageView photo;
    protected @InjectView(R.id.photo_gradient) RelativeLayout photo_gradient;
    protected @InjectView(R.id.potd_group) LinearLayout potd_group;
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
  }
}
