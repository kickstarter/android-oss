package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;
import com.kickstarter.ui.viewholders.SearchTermViewHolder;

import java.util.Collections;
import java.util.List;

public final class SearchAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectSearchResultViewHolder.Delegate {}

  public SearchAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  public void loadPopularProjects(final @NonNull List<Project> newProjects) {
    data().clear();
    if (newProjects.size() == 0) {
      data().add(Collections.emptyList());
    } else {
      data().add(Collections.singletonList(null));
    }
    data().add(newProjects);
    notifyDataSetChanged();
  }

  public void loadSearchProjects(final @NonNull List<Project> newProjects) {
    data().clear();
    data().add(Collections.emptyList());
    data().add(newProjects);
    notifyDataSetChanged();
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.search_term_view;
    }
    return R.layout.project_search_result_view;
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    if (layout == R.layout.search_term_view) {
      return new SearchTermViewHolder(view);
    }
    return new ProjectSearchResultViewHolder(view, delegate);
  }
}
