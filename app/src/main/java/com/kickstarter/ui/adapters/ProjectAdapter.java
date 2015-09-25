package com.kickstarter.ui.adapters;

import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.ui.viewholders.KsrViewHolder;
import com.kickstarter.ui.viewholders.ProjectViewHolder;
import com.kickstarter.ui.viewholders.RewardViewHolder;

import java.util.Collections;
import java.util.List;

public class ProjectAdapter extends KsrAdapter {
  private final Delegate delegate;

  public interface Delegate extends ProjectViewHolder.Delegate {}

  public ProjectAdapter(final Project project, final List<Pair<Project, Reward>> projectAndRewards, final Delegate delegate) {
    this.delegate = delegate;

    data().add(Collections.singletonList(project));
    data().add(projectAndRewards);
  }

  protected int layout(final SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.project_main_layout;
    }
    else {
      return R.layout.reward_card_view;
    }
  }

  protected KsrViewHolder viewHolder(final int layout, final View view) {
    if (layout == R.layout.reward_card_view) {
      return new RewardViewHolder(view);
    }
    return new ProjectViewHolder(view, delegate);
  }
}
