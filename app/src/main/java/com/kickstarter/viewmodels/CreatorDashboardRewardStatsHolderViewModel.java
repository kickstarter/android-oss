package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ComparatorUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsViewHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;

public interface CreatorDashboardRewardStatsHolderViewModel {

  interface Inputs {
    void pledgedColumnTitleClicked();
    void projectAndRewardStatsInput(Pair<Project, List<ProjectStatsEnvelope.RewardStats>> projectAndRewardStatsEnvelope);
  }

  interface Outputs {
    Observable<Pair<Project, List<ProjectStatsEnvelope.RewardStats>>> projectAndRewardStats();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardRewardStatsViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<List<ProjectStatsEnvelope.RewardStats>> sortedRewardStats = this.projectAndRewardStatsInput
        .map(PairUtils::second)
        .map(this::sortRewardStats);

      this.projectAndRewardStats = this.projectAndRewardStatsInput
        .map(PairUtils::first)
        .compose(combineLatestPair(sortedRewardStats));
    }

    final private class OrderByBackersRewardStatsComparator implements Comparator<ProjectStatsEnvelope.RewardStats> {
      @Override
      public int compare(final ProjectStatsEnvelope.RewardStats o1, final ProjectStatsEnvelope.RewardStats o2) {
        return new ComparatorUtils.DescendingOrderIntegerComparator().compare(o1.backersCount(), o2.backersCount());
      }
    }

    private @NonNull List<ProjectStatsEnvelope.RewardStats> sortRewardStats(final @NonNull List<ProjectStatsEnvelope.RewardStats> rewardStatsList) {
      final OrderByBackersRewardStatsComparator rewardStatsComparator = new OrderByBackersRewardStatsComparator();
      final Set<ProjectStatsEnvelope.RewardStats> rewardStatsTreeSet = new TreeSet<>(rewardStatsComparator);
      rewardStatsTreeSet.addAll(rewardStatsList);

      return new ArrayList<>(rewardStatsTreeSet);
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Void> pledgedColumnTitleClicked = PublishSubject.create();
    private final PublishSubject<Pair<Project, List<ProjectStatsEnvelope.RewardStats>>> projectAndRewardStatsInput = PublishSubject.create();

    private final Observable<Pair<Project, List<ProjectStatsEnvelope.RewardStats>>> projectAndRewardStats;

    @Override
    public void pledgedColumnTitleClicked() {
      this.pledgedColumnTitleClicked.onNext(null);
    }
    @Override
    public void projectAndRewardStatsInput(final @NonNull Pair<Project, List<ProjectStatsEnvelope.RewardStats>> projectAndRewardStats) {
      this.projectAndRewardStatsInput.onNext(projectAndRewardStats);
    }

    @Override public @NonNull Observable<Pair<Project, List<ProjectStatsEnvelope.RewardStats>>> projectAndRewardStats() {
      return this.projectAndRewardStats;
    }
  }
}
