package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsViewHolder;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface CreatorDashboardRewardStatsHolderViewModel {

  interface Inputs {
    void projectAndRewardStatsInput(Pair<Project, List<ProjectStatsEnvelope.RewardStats>> projectAndRewardStatsEnvelope);
  }

  interface Outputs {
    Observable<Pair<Project, List<ProjectStatsEnvelope.RewardStats>>> projectAndRewardStats();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardRewardStatsViewHolder> implements
    Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);
      this.projectAndRewardStats = projectAndRewardStatsInput;
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Pair<Project, List<ProjectStatsEnvelope.RewardStats>>> projectAndRewardStatsInput = PublishSubject.create();
    private final Observable<Pair<Project, List<ProjectStatsEnvelope.RewardStats>>> projectAndRewardStats;

    @Override
    public void projectAndRewardStatsInput(final @NonNull Pair<Project, List<ProjectStatsEnvelope.RewardStats>> projectAndRewardStats) {
      this.projectAndRewardStatsInput.onNext(projectAndRewardStats);
    }
    @Override public @NonNull Observable<Pair<Project, List<ProjectStatsEnvelope.RewardStats>>> projectAndRewardStats() {
      return this.projectAndRewardStats;
    }
  }
}
