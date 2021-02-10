package com.kickstarter.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.databinding.ProjectContextViewBinding;
import com.kickstarter.databinding.ProjectSocialViewBinding;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;
import com.kickstarter.ui.viewholders.ProjectSocialViewHolder;

import java.util.Collections;

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
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull ViewGroup viewGroup) {
    if (layout == R.layout.project_context_view) {
      return new ProjectContextViewHolder(ProjectContextViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false), this.delegate);
    } else {
      return new ProjectSocialViewHolder(ProjectSocialViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }
  }
}
