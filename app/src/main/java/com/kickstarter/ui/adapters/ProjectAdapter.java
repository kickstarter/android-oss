package com.kickstarter.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.databinding.EmptyViewBinding;
import com.kickstarter.databinding.ProjectMainLayoutBinding;
import com.kickstarter.ui.data.ProjectData;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectViewHolder;

import java.util.Collections;

public final class ProjectAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectViewHolder.Delegate {}

  public ProjectAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.project_main_layout;
    } else {
      return R.layout.empty_view;
    }
  }

  /**
   * Populate adapter data when we know we're working with a ProjectData object.
   */
  public void takeProject(final @NonNull ProjectData projectData) {
    sections().clear();
    sections().add(Collections.singletonList(projectData));
    notifyDataSetChanged();
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull ViewGroup viewGroup) {
    if (layout == R.layout.project_main_layout) {
      return new ProjectViewHolder(ProjectMainLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false), this.delegate);
    }
    return new EmptyViewHolder(EmptyViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
  }
}
