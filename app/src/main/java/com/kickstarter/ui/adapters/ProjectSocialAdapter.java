package com.kickstarter.ui.adapters;

import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;
import com.kickstarter.ui.viewholders.ProjectSocialViewHolder;

import java.util.Collections;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

public final class ProjectSocialAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectContextViewHolder.Delegate {}

  public ProjectSocialAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  @Override
  protected int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.project_context_view;
    } else {
      return R.layout.project_social_view;
    }
  }

  public void takeProject(final @NonNull Project project) {
    sections().clear();
    addSection(Collections.singletonList(project));
    addSection(project.friends());
    notifyDataSetChanged();
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    if (layout == R.layout.project_context_view) {
      return new ProjectContextViewHolder(view, this.delegate);
    } else {
      return new ProjectSocialViewHolder(view);
    }
  }
}
