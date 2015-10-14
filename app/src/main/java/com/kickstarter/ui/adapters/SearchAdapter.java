package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.KsrViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardMiniViewHolder;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;

import java.util.List;

public class SearchAdapter extends KsrAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectSearchResultViewHolder.Delegate {}

  public SearchAdapter(@NonNull final List<Project> projects, @NonNull final Delegate delegate) {
    this.delegate = delegate;
    data().add(projects);
  }

  protected @LayoutRes int layout(@NonNull final SectionRow sectionRow) {
    return R.layout.project_search_result_view;
  }

  protected KsrViewHolder viewHolder(@LayoutRes final int layout, @NonNull final View view) {
    return new ProjectSearchResultViewHolder(view, delegate);
  }
}
