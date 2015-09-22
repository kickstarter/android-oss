package com.kickstarter.ui.adapters;

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
  final Delegate delegate;

  public interface Delegate extends CategoryPromoViewHolder.Delegate, ProjectCardMiniViewHolder.Delegate {}

  public ThanksAdapter(final List<Project> projects, final Category category, final Delegate delegate) {
    this.delegate = delegate;
    data().add(projects);
    data().add(Collections.singletonList(category));
  }

  protected int layout(final SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.project_card_mini_view;
    } else {
      return R.layout.category_promo_view;
    }
  }

  protected KsrViewHolder viewHolder(final int layout, final View view) {
    if (layout == R.layout.project_card_mini_view) {
      return new ProjectCardMiniViewHolder(view, delegate);
    }
    return new CategoryPromoViewHolder(view, delegate);
  }
}
