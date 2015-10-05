package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.CategoryPromoViewHolder;
import com.kickstarter.ui.viewholders.KsrViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardMiniViewHolder;

import java.util.Collections;
import java.util.List;

public class ThanksAdapter extends KsrAdapter {
  private final Delegate delegate;

  public interface Delegate extends CategoryPromoViewHolder.Delegate, ProjectCardMiniViewHolder.Delegate {}

  public ThanksAdapter(@NonNull final List<Project> projects, @NonNull final Category category,
    @NonNull final Delegate delegate) {
    this.delegate = delegate;
    data().add(projects);
    data().add(Collections.singletonList(category));
  }

  protected @LayoutRes int layout(@NonNull final SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.project_card_mini_view;
    } else {
      return R.layout.category_promo_view;
    }
  }

  protected KsrViewHolder viewHolder(@LayoutRes final int layout, @NonNull final View view) {
    if (layout == R.layout.project_card_mini_view) {
      return new ProjectCardMiniViewHolder(view, delegate);
    }
    return new CategoryPromoViewHolder(view, delegate);
  }
}
