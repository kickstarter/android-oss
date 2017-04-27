package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.FeaturedSearchResultViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.PopularSearchViewHolder;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;

import java.util.Collections;
import java.util.List;

public final class SearchAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectSearchResultViewHolder.Delegate {}

  private static final int SECTION_POPULAR_TITLE = 0;
  private static final int SECTION_FEATURED_PROJECT = 1;
  private static final int SECTION_PROJECT = 2;

  public SearchAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  public void loadPopularProjects(final @NonNull List<Project> newProjects) {
    clearSections();

    if (newProjects.size() > 0) {
      this.insertSection(SECTION_POPULAR_TITLE, Collections.singletonList(null));
      this.insertSection(SECTION_FEATURED_PROJECT, Collections.singletonList(newProjects.get(0)));
      this.insertSection(SECTION_PROJECT, newProjects.subList(1, newProjects.size() - 1));
    }

    notifyDataSetChanged();
  }

  public void loadSearchProjects(final @NonNull List<Project> newProjects) {
    clearSections();

    if (newProjects.size() > 0) {
      this.insertSection(SECTION_POPULAR_TITLE, Collections.emptyList());
      this.insertSection(SECTION_FEATURED_PROJECT, Collections.singletonList(newProjects.get(0)));
      this.insertSection(SECTION_PROJECT, newProjects.subList(1, newProjects.size() - 1));
    }

    notifyDataSetChanged();
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    switch (sectionRow.section()) {
      case SECTION_POPULAR_TITLE:
        return R.layout.search_popular_title_view;
      case SECTION_FEATURED_PROJECT:
        return R.layout.featured_search_result_view;
      case SECTION_PROJECT:
        return R.layout.project_search_result_view;
      default:
        throw new IllegalStateException("Invalid section row");
    }
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    switch (layout) {
      case R.layout.search_popular_title_view:
        return new PopularSearchViewHolder(view);
      case R.layout.featured_search_result_view:
        return new FeaturedSearchResultViewHolder(view, delegate);
      case R.layout.project_search_result_view:
        return new ProjectSearchResultViewHolder(view, delegate);
      default:
        throw new IllegalStateException("Invalid layout");
    }
  }
}
