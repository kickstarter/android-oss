package com.kickstarter.ui.adapters;


import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardHeaderViewHolder;
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.Collections;

public class CreatorDashboardAdapter extends KSAdapter {

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.dashboard_funding_view;
    } else {
      return R.layout.dashboard_reward_stats_view;
    }
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    if (layout == R.layout.dashboard_funding_view) {
      return new CreatorDashboardHeaderViewHolder(view);
    } else {
      return new CreatorDashboardRewardStatsViewHolder(view);
    }
  }

  public void takeProjectAndStats(final @NonNull Pair<Project, ProjectStatsEnvelope> projectAndStatsEnvelope) {
    sections().clear();
    sections().add(Collections.singletonList(projectAndStatsEnvelope));

    sections().add(
      Collections.singletonList(
        Pair.create(projectAndStatsEnvelope.first, projectAndStatsEnvelope.second.rewardDistribution())
      )
    );
    notifyDataSetChanged();
  }
}
