package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.viewholders.KsrViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardMiniViewHolder;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;
import com.kickstarter.ui.viewholders.SearchTermViewHolder;

import java.util.Collections;
import java.util.List;

public class SearchAdapter extends KsrAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectSearchResultViewHolder.Delegate {}

  public SearchAdapter(@NonNull final Delegate delegate) {
    this.delegate = delegate;
  }

  public void loadProjectsAndParams(@NonNull final List<Project> newProjects, @NonNull final DiscoveryParams params) {
    clear();
    data().add(Collections.singletonList(params));
    data().add(newProjects);
    notifyDataSetChanged();
  }

  public void clear() {
    data().clear();
  }

  protected @LayoutRes int layout(@NonNull final SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.search_term_view;
    }
    return R.layout.project_search_result_view;
  }

  protected KsrViewHolder viewHolder(@LayoutRes final int layout, @NonNull final View view) {
    if (layout == R.layout.search_term_view) {
      return new SearchTermViewHolder(view);
    }
    return new ProjectSearchResultViewHolder(view, delegate);
  }
}
