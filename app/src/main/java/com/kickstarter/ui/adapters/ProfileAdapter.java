package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.ProfileCardViewHolder;

import java.util.List;

public final class ProfileAdapter extends KSAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProfileCardViewHolder.Delegate {}

  public ProfileAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  public void takeProjects(final @NonNull List<Project> projects) {
    clearSections();
    addSection(projects);
    notifyDataSetChanged();
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.profile_card_view;
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    return new ProfileCardViewHolder(view, delegate);
  }
}
