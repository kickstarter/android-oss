package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.FeaturedSearchResultViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;

import java.util.Collections;
import java.util.List;

public final class SearchAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectSearchResultViewHolder.Delegate {}

  public SearchAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  public void loadPopularProjects(final @NonNull List<Project> newProjects) {
    clearSections();
    if (newProjects.size() == 0) {
      addSection(Collections.emptyList());
    } else {
      addSection(Collections.singletonList(null));
    }
    addSection(newProjects);
    notifyDataSetChanged();
  }

  public void loadSearchProjects(final @NonNull List<Project> newProjects) {
    clearSections();
    addSection(getFeatureProject(newProjects));
    addSection(getProjectList(newProjects));
    notifyDataSetChanged();
  }

  private @NonNull List<Project> getProjectList(@NonNull List<Project> newProjects) {
    return newProjects.size() > 1
      ? newProjects.subList(1,newProjects.size()-1)
      : Collections.emptyList();
  }

  private @NonNull List<Project> getFeatureProject(@NonNull List<Project> newProjects) {
    return newProjects.size() > 0
      ? newProjects.subList(0,1)
      : Collections.emptyList();
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.featured_search_result_view;
    }
    return R.layout.project_search_result_view;
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    if (layout == R.layout.featured_search_result_view) {
      return new FeaturedSearchResultViewHolder(view);
    }
    return new ProjectSearchResultViewHolder(view, delegate);
  }
}
