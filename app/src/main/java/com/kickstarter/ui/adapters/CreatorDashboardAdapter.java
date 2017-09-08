package com.kickstarter.ui.adapters;


import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardHeaderViewHolder;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerBreakDownViewHolder;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerStatsViewHolder;
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.Collections;

public class CreatorDashboardAdapter extends KSAdapter {

  private final @Nullable Delegate delegate;

  public interface Delegate extends CreatorDashboardHeaderViewHolder.Delegate {}

  public CreatorDashboardAdapter(final @Nullable Delegate delegate) {
    this.delegate = delegate;
  }

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    if (sectionRow.section() == 0) {
      return R.layout.dashboard_funding_view;
    } else if (sectionRow.section() == 1) {
      return R.layout.dashboard_reward_stats_view;
    } else if (sectionRow.section() == 2) {
      return R.layout.dashboard_referrer_breakdown_view;
    } else {
      return R.layout.dashboard_referrer_stats_view;
    }
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    if (layout == R.layout.dashboard_funding_view) {
      return new CreatorDashboardHeaderViewHolder(view, this.delegate);
    } else if (layout == R.layout.dashboard_reward_stats_view) {
      return new CreatorDashboardRewardStatsViewHolder(view);
    } else if (layout == R.layout.dashboard_referrer_breakdown_view) {
      return new CreatorDashboardReferrerBreakDownViewHolder(view);
    } else {
      return new CreatorDashboardReferrerStatsViewHolder(view);
    }
  }

  public void takeProjectAndStats(final @NonNull Pair<Project, ProjectStatsEnvelope> projectAndStatsEnvelope) {
    sections().clear();
    sections().add(Collections.singletonList(projectAndStatsEnvelope));

    // add reward stats sections
    sections().add(
      Collections.singletonList(
        Pair.create(projectAndStatsEnvelope.first, projectAndStatsEnvelope.second.rewardDistribution())
      )
    );
    // add referral stats sections
    sections().add(
      Collections.singletonList(
        Pair.create(projectAndStatsEnvelope.first, projectAndStatsEnvelope.second.referralDistribution())
      )
    );

    notifyDataSetChanged();
  }
}
