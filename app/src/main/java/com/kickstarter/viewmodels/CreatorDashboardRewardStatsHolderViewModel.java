package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardRewardStatsViewHolder;
import com.kickstarter.viewmodels.inputs.CreatorDashboardRewardStatsViewModelInputs;
import com.kickstarter.viewmodels.outputs.CreatorDashboardRewardStatsViewModelOutputs;

import rx.subjects.PublishSubject;

public interface CreatorDashboardRewardStatsHolderViewModel {

  final class ViewModel extends ActivityViewModel<CreatorDashboardRewardStatsViewHolder> implements
    CreatorDashboardRewardStatsViewModelInputs, CreatorDashboardRewardStatsViewModelOutputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);
    }

    private final PublishSubject<Pair<Project, ProjectStatsEnvelope>> projectAndStats = PublishSubject.create();

    @Override
    public void projectAndStats(final Project project, final ProjectStatsEnvelope projectStatsEnvelope) {
      this.projectAndStats.onNext(Pair.create(project, projectStatsEnvelope));
    }
  }
}
