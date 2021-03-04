package com.kickstarter.ui.adapters;


import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.databinding.DashboardRewardStatsRowViewBinding;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsRowViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.List;

import rx.Observable;

public class CreatorDashboardRewardStatsAdapter extends KSAdapter {

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.dashboard_reward_stats_row_view;
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull ViewGroup viewGroup) {
    return new CreatorDashboardRewardStatsRowViewHolder(DashboardRewardStatsRowViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));

  }

  public void takeProjectAndStats(final @NonNull Pair<Project, List<ProjectStatsEnvelope.RewardStats>> projectAndRewards) {
    sections().clear();

    addSection(Observable.from(projectAndRewards.second)
      .map(rewardStats -> Pair.create(projectAndRewards.first, rewardStats))
      .toList().toBlocking().single());

    notifyDataSetChanged();
  }
}
