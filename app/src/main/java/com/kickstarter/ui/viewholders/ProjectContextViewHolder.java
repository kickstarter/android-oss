package com.kickstarter.ui.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProjectContextViewHolder extends KsrViewHolder {
  private Project project;
  private final Delegate delegate;

  public @Bind(R.id.context_photo) ImageView projectContextImageView;
  public @Bind(R.id.project_name) TextView projectNameTextView;
  public @Bind(R.id.creator_name) TextView creatorNameTextView;

  public interface Delegate {
    void contextClick(final ProjectContextViewHolder viewHolder, final Project project);
  }

  public ProjectContextViewHolder(final View view, final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  public void onBind(final Object datum) {
    this.project = (Project) datum;

    Picasso.with(view.getContext()).load(project.photo().full()).into(projectContextImageView);
    projectNameTextView.setText(project.name());
    creatorNameTextView.setText(project.creator().name());
  }

  @Override
  public void onClick(final View view) {
    delegate.contextClick(this, project);
  }
}
