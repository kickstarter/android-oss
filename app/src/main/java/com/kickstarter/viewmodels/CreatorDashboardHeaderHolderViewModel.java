package com.kickstarter.viewmodels;


import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectStats;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.viewholders.CreatorDashboardHeaderViewHolder;
import com.kickstarter.viewmodels.inputs.CreatorDashboardHeaderHolderViewModelInputs;
import com.kickstarter.viewmodels.outputs.CreatorDashboardHeaderHolderViewModelOutputs;

import rx.Observable;
import rx.subjects.PublishSubject;

public class CreatorDashboardHeaderHolderViewModel extends ActivityViewModel<CreatorDashboardHeaderViewHolder> implements
  CreatorDashboardHeaderHolderViewModelInputs, CreatorDashboardHeaderHolderViewModelOutputs {

  private final ApiClientType client;

  private final PublishSubject<ProjectStats> projectStats = PublishSubject.create();
  private final Observable<String> projectBackersCountText;

  public final CreatorDashboardHeaderHolderViewModelInputs inputs = this;
  public final CreatorDashboardHeaderHolderViewModelOutputs outputs = this;

  public ViewModel(final @NonNull Environment environment) {
    super(environment);
    this.client = environment.apiClient();
  }
  @Override public void projectAndStats(Project project, ProjectStats projectStats) {
    this.projectStats.onNext(projectStats);
  }

  @Override public @NonNull Observable<String> projectBackersCountText() {
    return this.projectBackersCountText;
  }

}
