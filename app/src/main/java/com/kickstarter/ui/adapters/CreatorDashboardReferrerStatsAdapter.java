package com.kickstarter.ui.adapters;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.databinding.DashboardReferrerStatsRowViewBinding;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerStatsRowViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.List;

import rx.Observable;

public class CreatorDashboardReferrerStatsAdapter extends KSAdapter {

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.dashboard_referrer_stats_row_view;
  }

  @Override
  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull ViewGroup viewGroup) {
    return new CreatorDashboardReferrerStatsRowViewHolder(DashboardReferrerStatsRowViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
  }

  public void takeProjectAndReferrerStats(final @NonNull Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>> projectAndReferrerStats) {
    sections().clear();

    addSection(Observable.from(projectAndReferrerStats.second)
      .map(referrerStats -> Pair.create(projectAndReferrerStats.first, referrerStats))
      .toList().toBlocking().single());

    notifyDataSetChanged();
  }
}
