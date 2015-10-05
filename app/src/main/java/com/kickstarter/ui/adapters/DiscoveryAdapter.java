package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.KsrViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;

import java.util.List;

public class DiscoveryAdapter extends KsrAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectCardViewHolder.Delegate {}

  public DiscoveryAdapter(@NonNull final List<Project> projects, @NonNull final Delegate delegate) {
    data().add(projects);
    this.delegate = delegate;
  }

  protected @LayoutRes int layout(@NonNull final SectionRow sectionRow) {
    return R.layout.project_card_view;
  }

  protected KsrViewHolder viewHolder(@LayoutRes final int layout, @NonNull final View view) {
    return new ProjectCardViewHolder(view, delegate);
  }
}
