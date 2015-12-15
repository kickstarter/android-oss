package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class ProjectCardMiniViewHolder extends KSViewHolder {
  private Project project;
  final Delegate delegate;

  @Bind(R.id.time_to_go_text_view) TextView timeToGoTextView;
  @Bind(R.id.name) TextView nameTextView;
  @Bind(R.id.photo) ImageView photoImageView;

  public interface Delegate {
    void projectCardMiniClick(ProjectCardMiniViewHolder viewHolder, Project project);
  }

  public ProjectCardMiniViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    project = (Project) datum;

    final Context context = view.getContext();

    nameTextView.setText(project.name());

    if (project.isLive()) {
      timeToGoTextView.setText(ProjectUtils.timeToGo(project, context));
      timeToGoTextView.setVisibility(View.VISIBLE);
    } else {
      timeToGoTextView.setVisibility(View.GONE);
    }

    Picasso.with(context).load(project.photo().med()).into(photoImageView);
  }

  @Override
  public void onClick(@NonNull final View view) {
    delegate.projectCardMiniClick(this, project);
  }
}
