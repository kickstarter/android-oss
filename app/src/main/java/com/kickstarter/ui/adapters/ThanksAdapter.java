package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.ThanksCategoryViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ThanksProjectViewHolder;

import java.util.Collections;
import java.util.List;

public final class ThanksAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends ThanksCategoryViewHolder.Delegate, ThanksProjectViewHolder.Delegate {}

  public ThanksAdapter(final @NonNull List<Project> projects, final @NonNull Category category,
    final @NonNull Delegate delegate) {
    this.delegate = delegate;
    addSection(projects);
    addSection(Collections.singletonList(category));
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.thanks_project_view;
    } else {
      return R.layout.thanks_category_view;
    }
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    if (layout == R.layout.thanks_project_view) {
      return new ThanksProjectViewHolder(view, delegate);
    }
    return new ThanksCategoryViewHolder(view, delegate);
  }
}
