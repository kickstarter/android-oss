package com.kickstarter.ui.adapters;

import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.KsrViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;

import java.util.List;

public class DiscoveryAdapter extends KsrAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectCardViewHolder.Delegate {}

  public DiscoveryAdapter(final List<Project> projects, final Delegate delegate) {
    data().add(projects);
    this.delegate = delegate;
  }

  protected int layout(final SectionRow sectionRow) {
    return R.layout.project_card_view;
  }

  protected KsrViewHolder viewHolder(final int layout, final View view) {
    return new ProjectCardViewHolder(view, delegate);
  }
}
