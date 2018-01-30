package com.kickstarter.viewmodels;


import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ComparatorUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerStatsViewHolder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface CreatorDashboardReferrerStatsHolderViewModel {

  interface Inputs {
    void projectAndReferrerStatsInput(Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>> projectAndReferrerStats);
  }

  interface Outputs {
    Observable<Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>>> projectAndReferrerStats();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardReferrerStatsViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<List<ProjectStatsEnvelope.ReferrerStats>> sortedReferrerStats = this.projectAndReferrerStatsInput
        .map(PairUtils::second)
        .map(this::sortReferrerStats);

      this.projectAndReferrerStats = this.projectAndReferrerStatsInput
        .map(PairUtils::first)
        .compose(Transformers.combineLatestPair(sortedReferrerStats));
    }

    final private class OrderByBackersReferrerStatsComparator implements Comparator<ProjectStatsEnvelope.ReferrerStats> {
      @Override
      public int compare(final @NonNull ProjectStatsEnvelope.ReferrerStats o1, final @NonNull ProjectStatsEnvelope.ReferrerStats o2) {
        return new ComparatorUtils.DescendingOrderFloatComparator().compare(o1.pledged(), o2.pledged());
      }
    }

    private @NonNull List<ProjectStatsEnvelope.ReferrerStats> sortReferrerStats(final @NonNull List<ProjectStatsEnvelope.ReferrerStats> referrerStatsList) {
      final OrderByBackersReferrerStatsComparator referrerStatsComparator = new OrderByBackersReferrerStatsComparator();
      Collections.sort(referrerStatsList, referrerStatsComparator);

      return referrerStatsList;
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>>> projectAndReferrerStatsInput = PublishSubject.create();
    private final Observable<Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>>> projectAndReferrerStats;

    @Override
    public void projectAndReferrerStatsInput(final @NonNull Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>> projectAndReferrerStats) {
      this.projectAndReferrerStatsInput.onNext(projectAndReferrerStats);
    }
    @Override public @NonNull Observable<Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>>> projectAndReferrerStats() {
      return this.projectAndReferrerStats;
    }
  }
}
