package com.kickstarter.ui.adapters;

import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerStatsRowViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import rx.Observable;

public class CreatorDashboardReferrerStatsAdapter extends KSAdapter {

  protected @LayoutRes int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.dashboard_referrer_stats_row_view;
  }

  protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view) {
    return new CreatorDashboardReferrerStatsRowViewHolder(view);
  }

  public void takeProjectAndReferrerStats(final @NonNull Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>> projectAndReferrerStats) {
    sections().clear();

    addSection(Observable.from(projectAndReferrerStats.second)
      .map(referrerStats -> Pair.create(projectAndReferrerStats.first, referrerStats))
      .toList().toBlocking().single());

    notifyDataSetChanged();
  }
}
