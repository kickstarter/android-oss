package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Project;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class ManageProjectNotificationsViewHolder extends KSViewHolder {
  protected @Bind(R.id.project_name) TextView projectNameTextView;

  public ManageProjectNotificationsViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    final Project project = (Project) datum;
    projectNameTextView.setText(project.name());
  }
}
