package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class ProjectContextViewHolder extends KSViewHolder {
  private Project project;
  private final Delegate delegate;

  public @Bind(R.id.context_photo) ImageView projectContextImageView;
  public @Bind(R.id.project_name) TextView projectNameTextView;
  public @Bind(R.id.creator_name) TextView creatorNameTextView;

  public interface Delegate {
    void projectContextClicked(ProjectContextViewHolder viewHolder);
  }

  public ProjectContextViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    this.project = (Project) datum;

    Picasso.with(view.getContext()).load(project.photo().full()).into(projectContextImageView);
    projectNameTextView.setText(project.name());
    creatorNameTextView.setText(project.creator().name());
  }

  @Override
  public void onClick(@NonNull final View view) {
    delegate.projectContextClicked(this);
  }
}
