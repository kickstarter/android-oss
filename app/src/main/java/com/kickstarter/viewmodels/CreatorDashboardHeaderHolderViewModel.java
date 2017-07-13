package com.kickstarter.viewmodels;


import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectStats;
import com.kickstarter.ui.viewholders.CreatorDashboardHeaderViewHolder;
import com.kickstarter.viewmodels.inputs.CreatorDashboardHeaderHolderViewModelInputs;
import com.kickstarter.viewmodels.outputs.CreatorDashboardHeaderHolderViewModelOutputs;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public interface CreatorDashboardHeaderHolderViewModel {

  final class ViewModel extends ActivityViewModel<CreatorDashboardHeaderViewHolder> implements
    CreatorDashboardHeaderHolderViewModelInputs, CreatorDashboardHeaderHolderViewModelOutputs {


    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      projectAndStats
        .map(ps -> ps.first)
        .subscribe(this.currentProject);

      projectAndStats
        .map(ps -> ps.first)
        .map(Project::percentageFunded)
        .map(NumberUtils::flooredPercentage)
        .compose(bindToLifecycle())
        .subscribe(this.percentageFunded);

      projectAndStats
        .map(ps -> ps.first)
        .map(Project::backersCount)
        .map(NumberUtils::format)
        .compose(bindToLifecycle())
        .subscribe(projectBackersCountText);

      projectAndStats
        .map(ps -> ps.first)
        .map(Project::name)
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(projectNameTextViewText);

      projectAndStats
        .map(ps -> ps.first)
        .map(ProjectUtils::deadlineCountdownValue)
        .map(NumberUtils::format)
        .subscribe(timeRemainingText);

      this.currentProject()
        .compose(Transformers.takeWhen(projectViewClicked))
        .map(p -> Pair.create(p, RefTag.dashboard()))
        .subscribe(this.startProjectActivity);
    }


    public final CreatorDashboardHeaderHolderViewModelInputs inputs = this;
    public final CreatorDashboardHeaderHolderViewModelOutputs outputs = this;

    private final PublishSubject<Pair<Project, ProjectStats>> projectAndStats = PublishSubject.create();
    private final PublishSubject<Void> projectViewClicked = PublishSubject.create();

    private final BehaviorSubject<String> percentageFunded = BehaviorSubject.create();
    private final BehaviorSubject<Project> currentProject = BehaviorSubject.create();
    private final BehaviorSubject<String> projectBackersCountText = BehaviorSubject.create();
    private final BehaviorSubject<String> projectNameTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<Pair<Project, RefTag>> startProjectActivity = BehaviorSubject.create();
    private final BehaviorSubject<String> timeRemainingText = BehaviorSubject.create();

    @Override
    public void projectViewClicked() {
      this.projectViewClicked.onNext(null);
    }

    @Override
    public void projectAndStats(final Project project, final ProjectStats projectStats) {
      this.projectAndStats.onNext(Pair.create(project, projectStats));
    }

    @Override public @NonNull Observable<String> percentageFunded() {
      return this.percentageFunded;
    }
    @Override public @NonNull Observable<Project> currentProject() {
      return this.currentProject;
    }
    @Override public @NonNull Observable<String> projectBackersCountText() {
      return this.projectBackersCountText;
    }
    @Override public @NonNull Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }
    @Override public @NonNull Observable<Pair<Project, RefTag>> startProjectActivity() {
      return this.startProjectActivity;
    }
    @Override public @NonNull Observable<String> timeRemainingText() {
      return this.timeRemainingText;
    }
  }
}
