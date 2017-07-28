package com.kickstarter.viewmodels;


import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerStatsViewHolder;

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
      this.projectAndReferrerStats = projectAndReferrerStatsInput;
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>>> projectAndReferrerStatsInput = PublishSubject.create();
    private final Observable<Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>>> projectAndReferrerStats;

    @Override
    public void projectAndReferrerStatsInput(final @NonNull Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>> projectAndReferrerStats) {
      projectAndReferrerStatsInput.onNext(projectAndReferrerStats);
    }
    @Override public @NonNull Observable<Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>>> projectAndReferrerStats() {
      return this.projectAndReferrerStats;
    }
  }
}
