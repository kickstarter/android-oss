package com.kickstarter.ui.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProjectContextViewHolder extends RecyclerView.ViewHolder{
  protected View view;
  protected Project project;

  public @InjectView(R.id.context_photo) ImageView projectContextImageView;
  public @InjectView(R.id.project_name) TextView projectNameTextView;
  public @InjectView(R.id.creator_name) TextView creatorNameTextView;

  public ProjectContextViewHolder(final View view) {
    super(view);
    this.view = view;
    ((KsrApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.inject(this, view);
  }

  public void onBind(final Project project) {
    this.project = project;

    Picasso.with(view.getContext()).load(project.photo().full()).into(projectContextImageView);
    projectNameTextView.setText(project.name());
    creatorNameTextView.setText(project.creator().name());
  }
}
