package com.kickstarter.ui.adapters;


import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardHeaderViewHolder;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerStatsViewHolder;
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CreatorDashboardAdapter extends KSAdapter {

  final public class OrderByBackersRewardStatsComparator implements Comparator<ProjectStatsEnvelope.RewardStats> {
    @Override
    public int compare(final ProjectStatsEnvelope.RewardStats o1, final ProjectStatsEnvelope.RewardStats o2) {
      if (o1.backersCount() < o2.backersCount()) {
        return 1;
      } else if (o1.backersCount() > o2.backersCount()) {
        return -1;
      } else {
        return 0;
      }
    }
  }

  final public class OrderByPledgedReferrerStatsComparator implements Comparator<ProjectStatsEnvelope.ReferrerStats> {
    @Override
    public int compare(final ProjectStatsEnvelope.ReferrerStats o1, final ProjectStatsEnvelope.ReferrerStats o2) {
      if (o1.pledged() < o2.pledged()) {
        return 1;
      } else if (o1.pledged() > o2.pledged()) {
        return -1;
      } else {
        return 0;
      }
    }
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.dashboard_funding_view;
    } else if (sectionRow.section() == 1) {
      return R.layout.dashboard_reward_stats_view;
    } else {
      return R.layout.dashboard_referrer_stats_view;
    }
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    if (layout == R.layout.dashboard_funding_view) {
      return new CreatorDashboardHeaderViewHolder(view);
    } else if (layout == R.layout.dashboard_reward_stats_view) {
      return new CreatorDashboardRewardStatsViewHolder(view);
    } else {
      return new CreatorDashboardReferrerStatsViewHolder(view);
    }
  }

  public void takeProjectAndStats(final @NonNull Pair<Project, ProjectStatsEnvelope> projectAndStatsEnvelope) {
    sections().clear();
    sections().add(Collections.singletonList(projectAndStatsEnvelope));

    // add reward stats sections
    final OrderByBackersRewardStatsComparator rewardStatsComparator = new OrderByBackersRewardStatsComparator();
    final Set<ProjectStatsEnvelope.RewardStats> rewardStatsTreeSet = new TreeSet<>(rewardStatsComparator);
    rewardStatsTreeSet.addAll(projectAndStatsEnvelope.second.rewardDistribution());
    final List<ProjectStatsEnvelope.RewardStats> sortedRewardStatsList = new ArrayList<>(rewardStatsTreeSet);

    sections().add(
      Collections.singletonList(
        Pair.create(projectAndStatsEnvelope.first, sortedRewardStatsList)
      )
    );


    // add referrer stats sections
    final OrderByPledgedReferrerStatsComparator referrerStatsComparator = new OrderByPledgedReferrerStatsComparator();
    final Set<ProjectStatsEnvelope.ReferrerStats> referrerStatsTreeSet = new TreeSet<>(referrerStatsComparator);
    referrerStatsTreeSet.addAll(projectAndStatsEnvelope.second.referralDistribution());
    final List<ProjectStatsEnvelope.ReferrerStats> sortedReferrerStatsList = new ArrayList<>(referrerStatsTreeSet);

    sections().add(
      Collections.singletonList(
        Pair.create(projectAndStatsEnvelope.first, sortedReferrerStatsList)
      )
    );

    notifyDataSetChanged();
  }
}
