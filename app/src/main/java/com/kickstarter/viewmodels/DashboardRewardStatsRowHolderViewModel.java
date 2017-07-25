package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsRowViewHolder;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface DashboardRewardStatsRowHolderViewModel {

  interface Inputs {
    void projectAndRewardStats(Pair<Project, ProjectStatsEnvelope.RewardStats> projectAndRewardStats);
  }

  interface Outputs {
    /* percent of the total that came from this reward */
    Observable<String> percentageOfTotalPledged();

    /* project and the amount pledged for this reward */
    Observable<Pair<Project, Float>> projectAndPledgedForReward();

    /* string number of backers */
    Observable<String> rewardBackerCount();

    /* minimum for reward */
    Observable<String> rewardMinimum();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardRewardStatsRowViewHolder> implements
    Inputs, Outputs {
    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.rewardStats = projectAndRewardStats
        .map(PairUtils::second);

      this.rewardBackerCount = this.rewardStats
        .map(ProjectStatsEnvelope.RewardStats::backersCount)
        .map(NumberUtils::format);

      this.projectAndPledgedForReward = projectAndRewardStats
        .map(pr -> Pair.create(pr.first, (float) (pr.second.pledged())));

      this.rewardMinimum = this.rewardStats
        .map(ProjectStatsEnvelope.RewardStats::minimum)
        .map(NumberUtils::format);

      this.percentageOfTotalPledged = this.projectAndRewardStats
        .map(projectRewardStats -> {
          final Project p = projectRewardStats.first;
          final ProjectStatsEnvelope.RewardStats rs = projectRewardStats.second;
          return NumberUtils.flooredPercentage((rs.pledged() / p.pledged()) * 100);
        })
        .map(StringUtils::wrapInParentheses);
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Pair<Project, ProjectStatsEnvelope.RewardStats>> projectAndRewardStats = PublishSubject.create();

    private final Observable<String> percentageOfTotalPledged;
    private final Observable<Pair<Project, Float>> projectAndPledgedForReward;
    private final Observable<String> rewardBackerCount;
    private final Observable<String> rewardMinimum;
    private final Observable<ProjectStatsEnvelope.RewardStats> rewardStats;

    @Override
    public void projectAndRewardStats(final @NonNull Pair<Project, ProjectStatsEnvelope.RewardStats> projectAndRewardStats) {
      this.projectAndRewardStats.onNext(projectAndRewardStats);
    }

    @Override public @NonNull Observable<Pair<Project, Float>> projectAndPledgedForReward() {
      return this.projectAndPledgedForReward;
    }
    @Override public @NonNull Observable<String> rewardBackerCount() {
      return this.rewardBackerCount;
    }
    @Override public @NonNull Observable<String> percentageOfTotalPledged() {
      return this.percentageOfTotalPledged;
    }
    @Override public @NonNull Observable<String> rewardMinimum() {
      return this.rewardMinimum;
    }
  }
}
