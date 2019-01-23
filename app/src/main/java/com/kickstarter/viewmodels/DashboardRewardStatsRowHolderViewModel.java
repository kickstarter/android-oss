package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsRowViewHolder;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.PublishSubject;

public interface DashboardRewardStatsRowHolderViewModel {

  interface Inputs {
    /** Current project and reward stat. */
    void projectAndRewardStats(Pair<Project, ProjectStatsEnvelope.RewardStats> projectAndRewardStats);
  }

  interface Outputs {
    /** Emits percent of the total that came from this reward. */
    Observable<String> percentageOfTotalPledged();

    /** Emits project and the amount pledged for this reward. */
    Observable<Pair<Project, Float>> projectAndRewardPledged();

    /** Emits string number of backers. */
    Observable<String> rewardBackerCount();

    /** Emits minimum for reward. */
    Observable<Pair<Project, Integer>> projectAndRewardMinimum();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardRewardStatsRowViewHolder> implements Inputs, Outputs {
    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<ProjectStatsEnvelope.RewardStats> rewardStats = this.projectAndRewardStats
        .map(PairUtils::second);

      this.rewardBackerCount = rewardStats
        .map(ProjectStatsEnvelope.RewardStats::backersCount)
        .map(NumberUtils::format);

      this.projectAndRewardPledged = this.projectAndRewardStats
        .map(pr -> Pair.create(pr.first, pr.second.pledged()));

      this.rewardMinimum = this.projectAndRewardStats
        .map(pr -> Pair.create(pr.first, pr.second.minimum()));

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
    private final Observable<Pair<Project, Float>> projectAndRewardPledged;
    private final Observable<String> rewardBackerCount;
    private final Observable<Pair<Project, Integer>> rewardMinimum;

    @Override
    public void projectAndRewardStats(final @NonNull Pair<Project, ProjectStatsEnvelope.RewardStats> projectAndRewardStats) {
      this.projectAndRewardStats.onNext(projectAndRewardStats);
    }

    @Override public @NonNull Observable<String> percentageOfTotalPledged() {
      return this.percentageOfTotalPledged;
    }
    @Override public @NonNull Observable<Pair<Project, Float>> projectAndRewardPledged() {
      return this.projectAndRewardPledged;
    }
    @Override public @NonNull Observable<String> rewardBackerCount() {
      return this.rewardBackerCount;
    }
    @Override public @NonNull Observable<Pair<Project, Integer>> projectAndRewardMinimum() {
      return this.rewardMinimum;
    }
  }
}
