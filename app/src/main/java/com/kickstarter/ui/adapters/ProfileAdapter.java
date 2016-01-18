package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.EmptyProfileViewHolder;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProfileCardViewHolder;

import java.util.Collections;
import java.util.List;

public final class ProfileAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProfileCardViewHolder.Delegate, EmptyProfileViewHolder.Delegate {}

  public ProfileAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  public void takeProjects(final @NonNull List<Project> projects) {
    data().clear();
    if (projects.size() == 0) {
      data().add(Collections.singletonList(Empty.get()));
    } else {
      data().add(projects);
    }
    notifyDataSetChanged();
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (objectFromSectionRow(sectionRow) instanceof Project) {
      return R.layout.profile_card_view;
    } else {
      return R.layout.profile_empty_state_view;
    }
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    switch(layout) {
      case R.layout.profile_empty_state_view:
        return new EmptyProfileViewHolder(view, delegate);
      case R.layout.profile_card_view:
        return new ProfileCardViewHolder(view, delegate);
      default:
        return new EmptyViewHolder(view);
    }
  }
}
