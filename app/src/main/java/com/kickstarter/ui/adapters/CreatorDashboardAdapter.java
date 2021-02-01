package com.kickstarter.ui.adapters;


import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.databinding.DashboardFundingViewBinding;
import com.kickstarter.databinding.DashboardReferrerBreakdownLayoutBinding;
import com.kickstarter.databinding.DashboardReferrerStatsViewBinding;
import com.kickstarter.databinding.DashboardRewardStatsViewBinding;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.adapters.data.ProjectDashboardData;
import com.kickstarter.ui.viewholders.CreatorDashboardHeaderViewHolder;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerBreakdownViewHolder;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerStatsViewHolder;
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.Collections;

public class CreatorDashboardAdapter extends KSAdapter {
  private static final int SECTION_FUNDING_VIEW = 0;
  private static final int SECTION_REWARD_STATS_VIEW = 1;
  private static final int SECTION_REFERRER_BREAKDOWN_LAYOUT = 2;
  private static final int SECTION_REFERRER_STATS_VIEW = 3;

  private final @Nullable Delegate delegate;

  public interface Delegate extends CreatorDashboardHeaderViewHolder.Delegate {}

  public CreatorDashboardAdapter(final @Nullable Delegate delegate) {
    this.delegate = delegate;

    insertSection(SECTION_FUNDING_VIEW, Collections.emptyList());
    insertSection(SECTION_REWARD_STATS_VIEW, Collections.emptyList());
    insertSection(SECTION_REFERRER_BREAKDOWN_LAYOUT, Collections.emptyList());
    insertSection(SECTION_REFERRER_STATS_VIEW, Collections.emptyList());
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == SECTION_FUNDING_VIEW) {
      return R.layout.dashboard_funding_view;
    } else if (sectionRow.section() == SECTION_REWARD_STATS_VIEW) {
      return R.layout.dashboard_reward_stats_view;
    } else if (sectionRow.section() == SECTION_REFERRER_BREAKDOWN_LAYOUT) {
      return R.layout.dashboard_referrer_breakdown_layout;
    } else {
      return R.layout.dashboard_referrer_stats_view;
    }
  }

  @NonNull
  @Override
  protected KSViewHolder viewHolder(int layout, @NonNull ViewGroup viewGroup) {
    if (layout == R.layout.dashboard_funding_view) {
      return new CreatorDashboardHeaderViewHolder(DashboardFundingViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false), this.delegate);
    } else if (layout == R.layout.dashboard_reward_stats_view) {
      return new CreatorDashboardRewardStatsViewHolder(DashboardRewardStatsViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    } else if (layout == R.layout.dashboard_referrer_breakdown_layout) {
      return new CreatorDashboardReferrerBreakdownViewHolder(DashboardReferrerBreakdownLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    } else {
      return new CreatorDashboardReferrerStatsViewHolder(DashboardReferrerStatsViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }
  }

  //ProjectContextViewHolder(ProjectContextViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false)

  public void takeProjectDashboardData(final @NonNull ProjectDashboardData projectDashboardData) {
    setSection(SECTION_FUNDING_VIEW, Collections.singletonList(projectDashboardData));

    // add reward stats sections
    final Project project = projectDashboardData.getProject();
    final ProjectStatsEnvelope projectStatsEnvelope = projectDashboardData.getProjectStatsEnvelope();
    setSection(
      SECTION_REWARD_STATS_VIEW,
      Collections.singletonList(
        Pair.create(project, projectStatsEnvelope.rewardDistribution())
      )
    );

    setSection(
      SECTION_REFERRER_BREAKDOWN_LAYOUT,
      Collections.singletonList(
        Pair.create(project, projectStatsEnvelope)
      )
    );

    setSection(
      SECTION_REFERRER_STATS_VIEW,
      Collections.singletonList(
        Pair.create(project, projectStatsEnvelope.referralDistribution())
      )
    );

    notifyDataSetChanged();
  }
}
