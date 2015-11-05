package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProjectSearchResultViewHolder extends KSViewHolder {
  private Project project;
  final Delegate delegate;

  @Bind(R.id.creator_name_text_view) TextView creatorNameTextView;
  @Bind(R.id.project_name_text_view) TextView projectNameTextView;
  @Bind(R.id.project_image_view) ImageView projectImageView;

  public interface Delegate {
    void projectSearchResultClick(ProjectSearchResultViewHolder viewHolder, Project project);
  }

  public ProjectSearchResultViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    project = (Project) datum;
    final Context context = view.getContext();

    creatorNameTextView.setText(context.getString(R.string.by_, project.creator().name()));
    projectNameTextView.setText(project.name());
    Picasso.with(context).load(project.photo().small()).into(projectImageView);
  }

  @Override
  public void onClick(@NonNull final View view) {
    delegate.projectSearchResultClick(this, project);
  }
}

