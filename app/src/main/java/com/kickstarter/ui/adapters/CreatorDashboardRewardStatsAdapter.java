package com.kickstarter.ui.adapters;


import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsRowViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import rx.Observable;

public class CreatorDashboardRewardStatsAdapter extends KSAdapter {

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.dashboard_reward_stats_row_view;
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    return new CreatorDashboardRewardStatsRowViewHolder(view);
  }

  public void takeProjectAndStats(final @NonNull Pair<Project, List<ProjectStatsEnvelope.RewardStats>> projectAndRewards) {
    sections().clear();

    addSection(Observable.from(projectAndRewards.second)
      .map(rewardStats -> Pair.create(projectAndRewards.first, rewardStats))
      .toList().toBlocking().single());

    notifyDataSetChanged();
  }
}
